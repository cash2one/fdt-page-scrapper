package com.fdt.doorgen.key.pooler;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.doorgen.key.pooler.dao.KeysDao;
import com.fdt.doorgen.key.pooler.dao.PageContentDao;
import com.fdt.doorgen.key.pooler.dao.PagesDao;
import com.fdt.doorgen.key.pooler.dao.SnippetsDao;
import com.fdt.doorgen.key.pooler.util.DoorUtils;
import com.fdt.scrapper.task.ConfigManager;

public class DoorgenPosterRunner {

	private static final Logger log = Logger.getLogger(DoorgenPoolerRunner.class);

	//TODO Read host name from config
	private String connectionString = null;
	private TimeString timeString;
	
	private static final String CONNECTION_STRING_LABEL = "connection_string";
	private static final String TIME_TABLE_LABLE = "time_table";

	Random rnd = new Random();

	private Connection connection;

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
			
			DOMConfigurator.configure("log4j_poster.xml");

			DoorgenPosterRunner taskRunner = null;
			try {
				taskRunner = new DoorgenPosterRunner(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			taskRunner.executeWrapper();
		}
	}

	private void executeWrapper() throws Exception{
		try{
			long curTime = System.currentTimeMillis();
			long startOtDay = DoorUtils.getStartOfDay(curTime);
			
			ArrayList<String> keys = pagesDao.getPages4Post();
			Collections.shuffle(keys);
			
			int postCount = timeString.getRndCnt();
			
			for(int i = 0; i < postCount && keys.size() < i; i++){
				//get normal distribution time value
				long postTime = DoorUtils.getRndNormalDistTime() + startOtDay;
				postTime = DoorUtils.calibratePostDate(postTime, curTime);
				pageCntntDao.postPage(keys.get(i), postTime);
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
	
	public DoorgenPosterRunner(String cfgFilePath) throws Exception
	{
		ConfigManager.getInstance().loadProperties(cfgFilePath);

		this.connectionString = ConfigManager.getInstance().getProperty(CONNECTION_STRING_LABEL);
		
		String timeTableStr = ConfigManager.getInstance().getProperty(TIME_TABLE_LABLE);
		if(timeTableStr != null && !"".equals(timeTableStr.trim())){
			File timeFile = new File(timeTableStr);
			if(timeFile.exists() && timeFile.isFile()){
				timeString = TimeTable.getTimeString(timeFile);
			}else{
				throw new Exception("TimeFile for time table was not found or exist.");
			}
		}else{
			throw new Exception("TimeFile for time table was not specified in the config file.");
		}

		connection = getConnection();
		
		keysDao = new KeysDao();
		pagesDao = new PagesDao(connection);
		snipDao = new SnippetsDao(connection);
		pageCntntDao = new PageContentDao(connection, snipDao);
	}
	
	private Connection getConnection() throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		//TODO Setup the connection with the DB
		Connection connection = (Connection) DriverManager.getConnection(this.connectionString);

		return connection;
	}

}
