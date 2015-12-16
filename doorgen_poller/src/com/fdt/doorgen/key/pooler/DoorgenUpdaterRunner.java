package com.fdt.doorgen.key.pooler;

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

//TODO 5. Удаляем уже не используемые новости
public class DoorgenUpdaterRunner {

	private static final Logger log = Logger.getLogger(DoorgenPoolerRunner.class);

	//TODO Read host name from config
	private String connectionString = null;
	
	private static final String CONNECTION_STRING_LABEL = "connection_string";

	private Random rnd = new Random();

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

			DoorgenUpdaterRunner taskRunner = null;
			try {
				taskRunner = new DoorgenUpdaterRunner(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			taskRunner.executeWrapper();
		}
	}

	
	private void executeWrapper() throws Exception{
		try{
			int updDateDiff = 3 + rnd.nextInt(3);
			ArrayList<String> keys = pagesDao.getPages4Update(updDateDiff);
			//
			Collections.shuffle(keys);
			
			long curTime = System.currentTimeMillis();
			long startOtDay = DoorUtils.getStartOfDay(curTime);
			long postTime = -1;
			
			for(int i = 0; i < keys.size(); i++){
				//get normal distribution time value
				//TODO Update page
				postTime = DoorUtils.getRndNormalDistTime() + startOtDay;
				postTime = DoorUtils.calibratePostDate(postTime, curTime);
				int pcId = pageCntntDao.insertPageContent(keys.get(i),postTime);
				if(pcId > 0){
					pageCntntDao.populateContent(keys.get(i), pcId);
					pageCntntDao.updPagesAsUpdated(keys.get(i));
				}else{
					throw new Exception("Page content record was not added for key: " + keys.get(i));
				}
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
	
	public DoorgenUpdaterRunner(String cfgFilePath) throws Exception
	{
		ConfigManager.getInstance().loadProperties(cfgFilePath);

		this.connectionString = ConfigManager.getInstance().getProperty(CONNECTION_STRING_LABEL);
		
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
