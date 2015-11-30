package com.fdt.doorpooler.scrapper;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;
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

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";

	private static final String SNIPPET_SEARCH_PAGE_MAX_LABEL = "snippet_search_page_max";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	//TODO Read host name from config
	private String hostName = "vtopax.ru";

	Random rnd = new Random();

	private TaskFactory taskFactory;
	private ProxyFactory proxyFactory;

	private SaverThread saver;

	private Connection connection;
	
	HashMap<String, Integer> keyMap = new HashMap<String, Integer>();

	/**
	 * args[0] - language
	 * args[1] - path to config file
	 */
	public static void main(String[] args){
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
			DOMConfigurator.configure("log4j.xml");

			DoorgenPoolerRunner taskRunner = null;
			try {
				taskRunner = new DoorgenPoolerRunner(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			taskRunner.execute();
		}
	}

	public DoorgenPoolerRunner(String cfgFilePath) throws Exception{

		ConfigManager.getInstance().loadProperties(cfgFilePath);
		this.taskFactory = TaskFactory.getInstance();
		this.proxyFilePath = ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
		this.proxyDelay = Integer.valueOf(ConfigManager.getInstance().getProperty(PROXY_DELAY_LABEL));

		this.source = ConfigManager.getInstance().getProperty(SOURCE_LABEL);
		this.lang = ConfigManager.getInstance().getProperty(LANG_LABEL);

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

		connection = getConnection();

		//init task factory
		TaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
		taskFactory = TaskFactory.getInstance();
		taskFactory.clear();

		//TODO Get all key and save them into the file

		ArrayList<String> keysList = getKeyList();
		taskFactory.loadTaskQueue(keysList, source, frequencies, lang);

		//init proxy factory
		ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(proxyFilePath);

		//run saver thread
		saver = new SaverThread(taskFactory,new IResultProcessor() {

			@Override
			public void processResult(SnippetTask task) {
				pollPagesTable(task);
			}

		});
		saver.start();


		String pageVaule = ConfigManager.getInstance().getProperty(SNIPPET_SEARCH_PAGE_MAX_LABEL);
		if(pageVaule != null && !"".equals(pageVaule.trim())){
			maxPageNum = Integer.valueOf(pageVaule);
		}

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


	public void execute(){
		try{
			synchronized (this) {
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

	private Connection getConnection() throws SQLException, ClassNotFoundException{
		Class.forName("com.mysql.jdbc.Driver");
		//TODO Setup the connection with the DB
		Connection connection = (Connection) DriverManager.getConnection("jdbc:mysql://192.240.96.165/vtopax?user=vtopax&password=lol200");

		return connection;
	}

	private ArrayList<String> getKeyList() throws ClassNotFoundException, SQLException{
		ArrayList<String> keyList = new ArrayList<String>();
		

		//TODO Select key for witch snippet count less than 4-6
		PreparedStatement prStmt = connection.prepareStatement(
				" SELECT k.key_value, 0 FROM vtopax.keys k LEFT JOIN pages p ON k.id=p.key_id WHERE p.key_id IS NULL " +
				" union " +
				" SELECT k.key_value, COUNT(k.key_value) " +  
				" FROM vtopax.keys k INNER JOIN snippets s " + 
				" ON k.id=s.key_id " + 
				" WHERE k.id NOT IN (SELECT k.key_value FROM vtopax.keys k LEFT JOIN pages p ON k.id=p.key_id WHERE p.key_id IS NULL) " + 
				" GROUP BY k.key_value HAVING COUNT(k.key_value) < 9");
		ResultSet rs = prStmt.executeQuery();

		if(rs == null){
			return keyList;
		}

		while(rs.next()){
			if(!"/".equals(rs.getString(1))){
				keyList.add(rs.getString(1));
				//saving count of snippets
				keyMap.put(rs.getString(1), rs.getInt(2));
			}
		}

		return keyList;
	}


	private void pollPagesTable(SnippetTask task){
		if(keyMap.get(task.getKeyWordsOrig()) == 0){
			insertPage(task);
		}
		insertSnippets(task);
		
		//TODO Insert images
	}
	
	private int insertPage(SnippetTask task){
		PreparedStatement prStmt = null;
		Random rnd = new Random();
		int count = -1;
		try {
			StringBuffer title = new StringBuffer();
			title.append(task.getKeyWordsOrig().substring(0, 1).toUpperCase())
			.append(task.getKeyWordsOrig().substring(1).toLowerCase())
			.append(" | Абсолютный лидер в сфере кредитования | ")
			.append( hostName);

			prStmt = connection.prepareStatement(" INSERT INTO `pages` (`key_id`,`title`,`meta_keywords`,`meta_description`,`upd_dt`, `post_dt`) " +
					" SELECT k.id, ?, ?, ?, now(), now() + INTERVAL 10 YEAR " + 
					" FROM `keys` k " + 
					" WHERE k.key_value = ? ");

			prStmt.setString(1, title.toString());
			prStmt.setString(2, title.toString());
			prStmt.setString(3, task.getSnipResult().get(rnd.nextInt(task.getSnipResult().size())).getContent());
			prStmt.setString(4, task.getKeyWordsOrig());

			count = prStmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(prStmt != null){
				try {
					prStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return count;
	}
	
	private int[] insertSnippets(SnippetTask task){
		PreparedStatement batchStatement = null;
		int[] result = null;
		try {
			//TODO Insert snippets & page_content tables.
			batchStatement = connection.prepareStatement("INSERT INTO `snippets` (`key_id`,`title`,`description`,`upd_dt`) " +
					"SELECT k.id, ?, ?, now() " + 
					"FROM `keys` k " + 
					"WHERE k.key_value = ?");
			for(Snippet snippet : task.getSnipResult()){
				batchStatement.setString(1,snippet.getTitle());
				batchStatement.setString(2,snippet.getContent());
				batchStatement.setString(3, task.getKeyWordsOrig());
				batchStatement.addBatch();
			}
			
			if(batchStatement != null){
				result = batchStatement.executeBatch(); // Execute every 1000 items.
            }

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(batchStatement != null){
				try {
					batchStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
}
