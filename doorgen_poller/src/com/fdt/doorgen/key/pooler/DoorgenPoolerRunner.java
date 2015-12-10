package com.fdt.doorgen.key.pooler;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

	public static Integer MIN_SNIPPET_COUNT_FOR_POST_PAGE = 27;

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";

	private static final String SNIPPET_SEARCH_PAGE_MAX_LABEL = "snippet_search_page_max";

	private static final String CONNECTION_STRING_LABEL = "connection_string";
	private static final String HOST_NAME_LABEL = "host_name";


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

	HashMap<String, Integer> keyMap = new HashMap<String, Integer>();

	/**
	 * args[0] - language
	 * args[1] - path to config file
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
			DOMConfigurator.configure("log4j.xml");

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
			connection = getConnection();
			
			keysList = getKeyList();
			taskFactory.clear();
			taskFactory.loadTaskQueue(keysList, source, frequencies, lang);
			
			while(keysList.size() > 0)
			{
				execute();
				keysList = getKeyList();
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


	public void execute() throws ClassNotFoundException, SQLException{
		synchronized (this) {
			//run saver thread
			saver = new SaverThread(taskFactory,new IResultProcessor() {

				@Override
				public void processResult(SnippetTask task) {
					pollPagesTable(task);
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

	private ArrayList<String> getKeyList() throws ClassNotFoundException, SQLException{
		ArrayList<String> keyList = new ArrayList<String>();


		//Select key for witch snippet count less than 4-6 or page does not exist for current key
		PreparedStatement prStmt = connection.prepareStatement(
				" SELECT k.key_value, 0 FROM door_keys k LEFT JOIN pages p ON k.id=p.key_id WHERE p.key_id IS NULL " +
						" union " +
						" SELECT k.key_value, COUNT(k.key_value) " +  
						" FROM door_keys k INNER JOIN snippets s " + 
						" ON k.id=s.key_id " + 
						" WHERE k.id NOT IN (SELECT k.key_value FROM door_keys k LEFT JOIN pages p ON k.id=p.key_id WHERE p.key_id IS NULL) " + 
						" GROUP BY k.key_value HAVING COUNT(k.key_value) < " + MIN_SNIPPET_COUNT_FOR_POST_PAGE);
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

		Collections.shuffle(keyList);

		return keyList;
	}


	private void pollPagesTable(SnippetTask task){
		int[] result = null;
		if(keyMap.get(task.getKeyWordsOrig()) == 0){
			insertPage(task);
		}

		//TODO Insert images

		//insert snippets
		result = insertSnippets(task);

		//TODO Insert random content
		if(result != null && result.length > 0)
		{
			result = null;
			result = populateContent(task.getKeyWordsOrig());
		}
		else{
			return;
		}

		//TODO Update page post_dt ??? HERE???
		if(result != null && result.length > 0){

		}
	}

	private int insertPage(SnippetTask task){
		PreparedStatement prStmt = null;
		Random rnd = new Random();
		int count = -1;
		try {
			StringBuffer title = new StringBuffer();
			title.append(task.getKeyWordsOrig().substring(0, 1).toUpperCase())
			.append(task.getKeyWordsOrig().substring(1).toLowerCase())
			.append(" | јбсолютный лидер в сфере кредитовани€ | ")
			.append( hostName);

			prStmt = connection.prepareStatement(" INSERT INTO pages (key_id, title, meta_keywords, meta_description, upd_dt, post_dt) " +
					" SELECT k.id, ?, ?, ?, now(), now() + INTERVAL 10 YEAR" + 
					" FROM door_keys k " + 
					" WHERE k.key_value = ? ");

			prStmt.setString(1, title.toString());
			prStmt.setString(2, title.toString());
			prStmt.setString(3, cleanString( task.getSnipResult().get(rnd.nextInt(task.getSnipResult().size())).getContent()) );
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
			batchStatement = connection.prepareStatement("INSERT INTO snippets (key_id,title,description,upd_dt) " +
					"SELECT k.id, ?, ?, now() " + 
					"FROM door_keys k " + 
					"WHERE k.key_value = ?");
			for(Snippet snippet : task.getSnipResult()){
				batchStatement.setString(1, getFirstSmblUpper(cleanString(snippet.getTitle())));
				batchStatement.setString(2, cleanString(snippet.getContent()));
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


	private ArrayList<Integer> getInsertedSnpId(String key){
		ArrayList<Integer> result = new ArrayList<Integer>();
		PreparedStatement prpStmt = null;
		ResultSet rs = null;
		try {
			//TODO Insert snippets & page_content tables.
			prpStmt = connection.prepareStatement(" SELECT s.id FROM snippets s, door_keys k " +
					" WHERE k.key_value = ? AND k.id = s.key_id ");
			prpStmt.setString(1, key);

			rs = prpStmt.executeQuery();

			if(rs != null){
				while(rs.next()){
					result.add(rs.getInt(1));
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if(prpStmt != null){
				try {
					prpStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	private int[] populateContent(String key){
		int blockCnt = 3;
		int blockSize = 3;
		ArrayList<Integer> snpIds = getInsertedSnpId(key);
		int[] result = null;

		int snpCnt = snpIds.size();
		PreparedStatement batchStatement = null;

		int rndBatchSnpCnt[] = getRndBlocksSize(blockCnt, blockSize);
		//если количество сниппетов не достаточно, то контент не будет сгенерирован
		if(arraySum(rndBatchSnpCnt) > snpCnt){
			return new int[]{};
		}


		ArrayList<Integer> rndSeq = getRandomSequense(snpCnt);

		try {
			//TODO Insert snippets & page_content tables.
			batchStatement = connection.prepareStatement("INSERT INTO page_content (page_id, snippet_id, snippets_index, main_flg, upd_dt) " +
					" SELECT p.id, ?, ?, ?, now() " + 
					" FROM pages p, door_keys k" + 
					" WHERE p.key_id = k.id AND k.key_value = ? ");


			for(int i = 0; i < 3; i++)
			{
				for(int j = 1; j <= rndBatchSnpCnt[i]; j++)
				{
					//get discription count
					int descCnt = 1+rnd.nextInt(3);
					boolean ifMainNotInserted = true;

					for(int k = 0; k < descCnt; k++)
					{
						batchStatement.setInt(1, snpIds.get(rndSeq.remove(0)));
						batchStatement.setInt(2, i*3 + j);
						batchStatement.setBoolean(3, ifMainNotInserted || false);
						batchStatement.setString(4, key);
						ifMainNotInserted = false;
						batchStatement.addBatch();
					}
				}
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

	private ArrayList<Integer> getRandomSequense(int seqSize){
		ArrayList<Integer> rndSeq = new ArrayList<Integer>();
		for(int i = 0; i < seqSize; i++){
			rndSeq.add(i);
		}

		Collections.shuffle(rndSeq);

		return rndSeq;
	}

	private int[] getRndBlocksSize(int blockCnt, int blockSize){
		Random rnd = new Random();
		int[] result = new int[blockCnt];

		for(int i = 0; i < blockCnt; i++){
			result[i] = 1 + rnd.nextInt(blockSize);
		}

		return result;
	}

	private int arraySum(int[] array){
		int sum = 0;
		for(Integer elem : array){
			sum += elem;
		}

		return sum;
	}

	private String getFirstSmblUpper(String input){
		StringBuffer output = new StringBuffer(input.substring(1).toLowerCase());
		output.insert(0, input.substring(0, 1).toUpperCase());

		return output.toString();
	}

	private String cleanString(String input){
		StringBuffer output = new StringBuffer(input);

		return output.toString().replaceAll("[^0-9a-zA-Zа-€ј-я\\s\\%\\$\\-]+", "").replaceAll("\\s+", " ");
	}
}
