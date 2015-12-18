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
	private TimeString timeStr;
	private File timeFile;
	
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
			int posted = pagesDao.getPostedCnt4Day();
			
			if(posted >= timeStr.getMin()){
				log.info("All news were posted for a day. Poster will end work");
				TimeTable.returnTimeSrt(timeStr, timeFile);
				return;
			}
			
			ArrayList<String> keys = pagesDao.getPages4Post();
			Collections.shuffle(keys);
			
			int postCount = timeStr.getRndCnt();
			
			log.info(String.format("%d new will be posted.",postCount));
			
			long curTime = System.currentTimeMillis();
			long startOtDay = DoorUtils.getStartOfDay(curTime);
			
			int postedCnt = 0;
			for(int i = 0; i < postCount && i < keys.size(); i++){
				//get normal distribution time value
				//TODO Add randomly +-2hours for a postTime
				long postTime = DoorUtils.getRndNormalDistTime() + startOtDay;
				postTime = DoorUtils.calibratePostDate(postTime, curTime);
				log.info(String.format("Try to post key %s with post time %s",keys.get(i), postTime));
				postedCnt += pageCntntDao.postPage(keys.get(i), postTime);
			}
			
			//if records were not updated in DB - return time string 
			if(postedCnt == 0){
				TimeTable.returnTimeSrt(timeStr, timeFile);
			}
			log.info(String.format("%d new were posted.",postedCnt));
			System.out.println(String.format("%d new were posted.",postedCnt));
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
			timeFile = new File(timeTableStr);
			if(timeFile.exists() && timeFile.isFile()){
				timeStr = TimeTable.getTimeString(timeFile);
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
