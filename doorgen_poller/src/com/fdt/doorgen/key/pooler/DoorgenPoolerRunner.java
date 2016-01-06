package com.fdt.doorgen.key.pooler;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.doorgen.key.pooler.content.ContentStrategy;
import com.fdt.doorgen.key.pooler.dao.KeysDao;
import com.fdt.doorgen.key.pooler.dao.PageContentDao;
import com.fdt.doorgen.key.pooler.dao.PagesDao;
import com.fdt.doorgen.key.pooler.dao.SnippetsDao;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.scrapper.task.TaskFactory;

/**
 *
 * @author Administrator
 */
public class DoorgenPoolerRunner{

	private static final String LANG_LABEL = "lang";

	private static final String SOURCE_LABEL = "source";
	private static final String FREQUENCY_LABEL = "frequency";

	private static final Logger log = Logger.getLogger(DoorgenPoolerRunner.class);

	private String proxyFilePath;
	private int maxThreadCount;
	private long proxyDelay;

	private String source = null;
	private String lang = null;
	private int[] frequencies = null;

	private int maxPageNum = 50;

	public static Integer MIN_SNIPPET_COUNT_FOR_POST_PAGE = 27;
	
	public static ContentStrategy STRATEGY_POLLER;

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";

	private static final String SNIPPET_SEARCH_PAGE_MAX_LABEL = "snippet_search_page_max";

	private static final String CONNECTION_STRING_LABEL = "connection_string";
	private static final String HOST_NAME_LABEL = "host_name";
	
	private static final String MIN_SNIPPET_COUNT_FOR_POST_PAGE_LABEL="min_snip_cnt_for_poller";
	
	private static final String STRATEGY_NAME_LABEL = "strategy_name";


	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	//TODO Read host name from config
	private String connectionString = null;
	private String hostName = null;

	Random rnd = new Random();

	private TaskFactory taskFactory;
	private ProxyFactory proxyFactory;

	private SaverThread saver;

	private Connection connection;

	private ArrayList<String> keysList = new ArrayList<String>();

	private HashMap<String, Integer> keyMap = new HashMap<String, Integer>();
	
	private KeysDao keysDao;
	private PagesDao pagesDao;
	private SnippetsDao snipDao;
	private PageContentDao pageCntntDao;

	/**
	 * args[0] - path to config file
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		if(args.length < 1){
			System.out.print("Not enought arguments....");
		}else{
			System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
			ConfigManager.getInstance().loadProperties(args[0]);
			System.out.println(args[0]);
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
							ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL),
							ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
							);
				}
			});
			DOMConfigurator.configure("log4j_pooler.xml");

			DoorgenPoolerRunner taskRunner = null;
			try {
				taskRunner = new DoorgenPoolerRunner(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}


			taskRunner.executeWrapper();
		}
	}

	private void executeWrapper() throws Exception{
		try{
			keysList = getKeyList4Update();
			taskFactory.clear();
			taskFactory.loadTaskQueue(keysList, source, frequencies, lang);

			while(keysList.size() > 0)
			{
				execute(hostName);
				keysList = getKeyList4Update();
				taskFactory.clear();
				taskFactory.loadTaskQueue(keysList, source, frequencies, lang);
			}
		}finally{
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					log.error(e);
				}
			}
		}
	}
	
	private ArrayList<String> getKeyList4Update() throws ClassNotFoundException, SQLException{
		return keysDao.getKeyList4Update(keyMap, MIN_SNIPPET_COUNT_FOR_POST_PAGE);
	}

	public DoorgenPoolerRunner(String cfgFilePath) throws Exception{

		ConfigManager.getInstance().loadProperties(cfgFilePath);
		this.taskFactory = TaskFactory.getInstance();
		this.proxyFilePath = ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
		this.proxyDelay = Integer.valueOf(ConfigManager.getInstance().getProperty(PROXY_DELAY_LABEL));

		this.source = ConfigManager.getInstance().getProperty(SOURCE_LABEL);
		this.lang = ConfigManager.getInstance().getProperty(LANG_LABEL);

		this.hostName = ConfigManager.getInstance().getProperty(HOST_NAME_LABEL);
		this.connectionString = ConfigManager.getInstance().getProperty(CONNECTION_STRING_LABEL);
		
		if(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_FOR_POST_PAGE_LABEL) != null){
			MIN_SNIPPET_COUNT_FOR_POST_PAGE = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_FOR_POST_PAGE_LABEL));
		}
		
		//getting strategy for poller
		if(ConfigManager.getInstance().getProperty(STRATEGY_NAME_LABEL) != null){
			STRATEGY_POLLER = ContentStrategy.getByName(ConfigManager.getInstance().getProperty(STRATEGY_NAME_LABEL));
		}else{
			STRATEGY_POLLER = ContentStrategy.DEFAULT;
		}

		String freqStr = ConfigManager.getInstance().getProperty(FREQUENCY_LABEL);
		if(freqStr != null && !"".equals(freqStr.trim())){
			String[] freqArray = freqStr.split(":");
			this.frequencies = new int[freqArray.length];
			for(int i = 0; i < freqArray.length; i++){
				this.frequencies[i] = Integer.parseInt(freqArray[i]);
			}
		}else{
			this.frequencies = new int[]{1};	
		}

		//init task factory
		TaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
		taskFactory = TaskFactory.getInstance();
		taskFactory.clear();

		//init proxy factory
		ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(proxyFilePath);

		String pageVaule = ConfigManager.getInstance().getProperty(SNIPPET_SEARCH_PAGE_MAX_LABEL);
		if(pageVaule != null && !"".equals(pageVaule.trim())){
			maxPageNum = Integer.valueOf(pageVaule);
		}
		
		connection = getConnection();
		
		keysDao = new KeysDao(connection);
		pagesDao = new PagesDao(connection);
		snipDao = new SnippetsDao(connection);
		pageCntntDao = new PageContentDao(connection, snipDao);

		//set authentication params
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL), ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
						);
			}
		});

	}

	/*public static void main(String[] args) {
		try{
			PosterTaskRunner taskRunner = new PosterTaskRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
			taskRunner.run();
			System.out.print("Program execution finished successfully");
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}*/


	public void execute(final String hostName) throws ClassNotFoundException, SQLException{
		synchronized (this) {
			//run saver thread
			saver = new SaverThread(taskFactory,new IResultProcessor() {

				public void processResult(SnippetTask task) {
					pollPagesTable(task, hostName);
				}

			});
			saver.start();

			ExecutorService executor = Executors.newFixedThreadPool(taskFactory.getMAX_THREAD_COUNT());

			DoorgenPoolerThread newThread = null;
			log.info("Total tasks: "+taskFactory.getTaskQueue().size());

			while(!taskFactory.isTaskFactoryEmpty() || taskFactory.getRunThreadsCount() > 0){
				log.debug("Try to get request from RequestFactory queue.");
				SnippetTaskWrapper task = taskFactory.getTask();
				log.debug("Task: " + task);
				if(task != null){
					log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
					task.getCurrentTask().setPage(rnd.nextInt(maxPageNum));

					newThread = new DoorgenPoolerThread(task, proxyFactory, taskFactory);
					executor.submit(newThread);
					continue;
				}
				try {
					this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
				} catch (InterruptedException e) {
					log.error("InterruptedException occured during RequestRunner process",e);
				}
			}
			executor.shutdown();
			saver.stopped = true;
			while(saver.isAlive()){
				try {
					this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				saver.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			log.info("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.getRunThreadsCount());
			log.info("Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
		}
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException{
		Class.forName("com.mysql.jdbc.Driver");
		//TODO Setup the connection with the DB
		Connection connection = (Connection) DriverManager.getConnection(this.connectionString);

		return connection;
	}

	private void pollPagesTable(SnippetTask task, String hostName){
		int[] result = null;
		int pId = -1;
		String key = task.getKeyWordsOrig();
		if(keyMap.get(task.getKeyWordsOrig()) == 0){
			//if all snipepts were extracted - create page
			pagesDao.insertPage(task, hostName);
		}

		//TODO Insert images

		//insert snippets
		result = snipDao.insertSnippets(task);

		//TODO Insert random content
		if(result != null && result.length > 0)
		{
			result = null;
			int pcId = pageCntntDao.insertPageContent(key);
			result = pageCntntDao.populateContent(key, pcId, STRATEGY_POLLER);
		}
		else{
			return;
		}
	}
}
