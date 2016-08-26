package com.fdt.doorgen.key.pooler.runner;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.fdt.doorgen.key.pooler.content.ContentStrategy;
import com.fdt.doorgen.key.pooler.content.Pooler;
import com.fdt.doorgen.key.pooler.dao.KeysDao;
import com.fdt.doorgen.key.pooler.dao.PageContentDao;
import com.fdt.doorgen.key.pooler.dao.PagesDao;
import com.fdt.doorgen.key.pooler.dao.SnippetsDao;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

/**
 *
 * @author Administrator
 */
public class DoorgenPoolerFilesRunner implements Pooler{

	private static final Logger log = Logger.getLogger(DoorgenPoolerFilesRunner.class);

	public static Integer MIN_SNIPPET_COUNT_FOR_POST_PAGE = 27;

	public static ContentStrategy STRATEGY_POLLER;

	private static final String CONNECTION_STRING_LABEL = "connection_string";
	private static final String GLOBAL_TITLE_LABEL = "global_title";
	private static final String HOST_NAME_LABEL = "host_name";

	private static final String INPUT_FILES_FOLDER_PATH_LABEL = "input_files_folder_path";

	private static final String MIN_SNIPPET_COUNT_FOR_POST_PAGE_LABEL="min_snip_cnt_for_poller";

	private static final String STRATEGY_NAME_LABEL = "strategy_name";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	//TODO Read host name from config
	private String connectionString = null;
	private String hostName = null;
	private String globalTitle = null;

	private File inputFilesFolder = null;

	Random rnd = new Random();

	private Connection connection;

	private ArrayList<String> keysList = new ArrayList<String>();

	private HashMap<Integer, Integer> keyMap = new HashMap<Integer, Integer>();

	private KeysDao keysDao;
	private PagesDao pagesDao;
	private SnippetsDao snipDao;
	private PageContentDao pageCntntDao;
	
	public DoorgenPoolerFilesRunner() {
		try{
			initPooler();
		}catch(Exception e){
			//TODO Log error messages
			log.fatal("Error during snippet pooler initialization", e);
		}
	}

	public void initPooler() throws Exception
	{
		this.hostName = ConfigManager.getInstance().getProperty(HOST_NAME_LABEL);
		this.globalTitle = ConfigManager.getInstance().getProperty(GLOBAL_TITLE_LABEL);
		this.connectionString = ConfigManager.getInstance().getProperty(CONNECTION_STRING_LABEL);

		this.inputFilesFolder = new File(ConfigManager.getInstance().getProperty(INPUT_FILES_FOLDER_PATH_LABEL));

		if(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_FOR_POST_PAGE_LABEL) != null){
			MIN_SNIPPET_COUNT_FOR_POST_PAGE = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_FOR_POST_PAGE_LABEL));
		}

		//getting strategy for poller
		if(ConfigManager.getInstance().getProperty(STRATEGY_NAME_LABEL) != null){
			STRATEGY_POLLER = ContentStrategy.getByName(ConfigManager.getInstance().getProperty(STRATEGY_NAME_LABEL));
		}else{
			throw new Exception(String.format("Strategy poller '%s' was not found", ConfigManager.getInstance().getProperty(STRATEGY_NAME_LABEL)));
		}

		connection = getConnection();

		keysDao = new KeysDao(connection);
		pagesDao = new PagesDao(connection);
		snipDao = new SnippetsDao(connection);
		pageCntntDao = new PageContentDao(connection, snipDao);
	}
	

	private Connection getConnection() throws SQLException, ClassNotFoundException{
		Class.forName("com.mysql.jdbc.Driver");
		//TODO Setup the connection with the DB
		Connection connection = (Connection) DriverManager.getConnection(this.connectionString);

		return connection;
	}

	public void executePooler() throws Exception{
		try{
			
			for(File file : this.inputFilesFolder.listFiles()){
				//TODO Add keys if not exist
				String key = FilenameUtils.removeExtension(file.getName());
				int keyId = keysDao.insertKey(key);
				List<String> sents = getSencsFromFile(file);
				
				int[] result = null;
				if(keyId < 1){
					keyId = Integer.valueOf((String)keysDao.getKeysIdByKeyValue(key).get(0).get(0));
				}
				
				snipDao.deleteAllSnippets4Key(keyId);
				result = snipDao.insertSnippets(sents, keyId);
			}
			
			//pollContent(MIN_SNIPPET_COUNT_FOR_POST_PAGE);
			pollContent(0);
			keysList = getKeyList4Polling();

			while(keysList.size() > 0)
			{
				pollContent(0);
				keysList = getKeyList4Polling();
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
	
	private ArrayList<String> getKeyList4Polling() throws ClassNotFoundException, SQLException
	{
		return keysDao.getKeyList4Polling(keyMap, MIN_SNIPPET_COUNT_FOR_POST_PAGE, STRATEGY_POLLER);
	}
	
	
	private void pollContent(int minSnpCnt) throws SQLException{
		// Polling keys that have snippets but don't have pages
		List<List<String>> keysWOPages = keysDao.getKeysWithoutPagesAndPageContent(minSnpCnt);

		for(List<String> row : keysWOPages){
			int keyId = Integer.valueOf(row.get(0));
			String keyValue = row.get(1);
			String descr = snipDao.getSnipDescrByKeyId(keyId).get(0).get(0);
			pollKeyContent(keyId, keyValue, descr, hostName, globalTitle);
		}
	}

	private void pollKeyContent(int keyId, String keyValue, String descr, String hostName, String globalTitle){

		int id = pagesDao.insertPage(keyId, keyValue, descr, hostName, globalTitle);

		int pcId = pageCntntDao.insertPageContent(keyId);

		int ids[] = pageCntntDao.populateContent(keyId, pcId, -1, STRATEGY_POLLER);
	}
	
	private List<String> getSencsFromFile(File file){
		List<String> sents = new ArrayList<String>();
		
		//TODO Get file content
		sents = Utils.loadFileAsStrList(file, false);
		//TODO Filter file content
		
		//TODO Structure file content
		
		//TODO Ограничить количество предложений
		
		return sents;
	}

}
