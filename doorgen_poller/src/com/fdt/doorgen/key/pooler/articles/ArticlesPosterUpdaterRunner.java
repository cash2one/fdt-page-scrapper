package com.fdt.doorgen.key.pooler.articles;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.doorgen.key.pooler.DoorgenPoolerRunner;
import com.fdt.doorgen.key.pooler.content.ContentStrategy;
import com.fdt.doorgen.key.pooler.dao.ArticleContentDao;
import com.fdt.doorgen.key.pooler.dao.ArticleTemplateDao;
import com.fdt.doorgen.key.pooler.util.DoorUtils;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

public class ArticlesPosterUpdaterRunner {

	private static final Logger log = Logger.getLogger(DoorgenPoolerRunner.class);

	private String connectionString = null;

	public static ContentStrategy STRATEGY_POLLER;
	
	private File newArticlesFolderPath;
	
	private int updateInterval;
	
	private int[] postInterval;

	private static final String CONNECTION_STRING_LABEL = "connection_string";

	private static final String NEW_ARTICLES_FOLDER_PATH_LABEL = "new_articles_folder_path";
	
	private static final String UPDATE_INTERVAL_LABEL = "update_interval";
	
	private static final String POST_INTERVAL_LABEL = "post_interval";
	
	private Random rnd = new Random();

	private Connection connection;

	private ArticleTemplateDao artTmplDAO; 
	private ArticleContentDao artCntntDAO; 

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

			DOMConfigurator.configure("log4j_articles_updater.xml");

			ArticlesPosterUpdaterRunner taskRunner = null;
			try {
				taskRunner = new ArticlesPosterUpdaterRunner(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			try{
				taskRunner.executeWrapper();
			}catch(Throwable e){
				log.error("Error occured during update process execution", e);
			}
		}
	}
	
	public ArticlesPosterUpdaterRunner(String cfgFilePath) throws Exception
	{
		ConfigManager.getInstance().loadProperties(cfgFilePath);

		this.connectionString = ConfigManager.getInstance().getProperty(CONNECTION_STRING_LABEL);

		newArticlesFolderPath = new File(ConfigManager.getInstance().getProperty(NEW_ARTICLES_FOLDER_PATH_LABEL));
		
		updateInterval = Integer.valueOf(ConfigManager.getInstance().getProperty(UPDATE_INTERVAL_LABEL));
		
		this.postInterval = readPostInterval(ConfigManager.getInstance().getProperty(POST_INTERVAL_LABEL));
		
		connection = getConnection();
		connection.setAutoCommit(false);
		
		artTmplDAO = new ArticleTemplateDao(connection);
		artCntntDAO = new ArticleContentDao(connection);
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		//Setup the connection with the DB
		Connection connection = (Connection) DriverManager.getConnection(this.connectionString);

		return connection;
	}

	private void executeWrapper() throws Exception
	{
		int deleted = 0;
		
		try{
			ArrayList<String> addedUrls; 
			addedUrls = uploadNewArticles();
			
			//Post news for newly added articles
			postNewlyAddedArticles();
			
			//Update expired articles
			updateExpiredArticles(updateInterval);
			
			//Delete deprecated articles
			deleted = deleteDeprecatedArticles();

			connection.commit();
			connection.setAutoCommit(true);
			log.info(String.format("%d deprecated records were deleted from table", deleted));
		}
		catch(Exception e){
			connection.rollback();
			connection.setAutoCommit(true);
			throw e;
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

	private ArrayList<String> uploadNewArticles() 
	{
		String tmplContent;
		String keyUrl;
		ArrayList<String> addedUrls = new ArrayList<String>();
		
		//Read and post new articles from input files
		for(File file : newArticlesFolderPath.listFiles()){
			tmplContent= Utils.loadFileAsString(file);
			keyUrl = processNewTemplate(FilenameUtils.removeExtension(file.getName()), tmplContent);
			addedUrls.add(keyUrl);
			//TODO move processed file to temp folder???
		}
		
		return addedUrls;
	}
	
	private void postNewlyAddedArticles() throws SQLException{
		List<List<String>> tmplWoCntntIds =  artTmplDAO.getTmplsWOPostedArticles();
		
		long maxPostDate = artCntntDAO.getMaxPostDate()*1000;
		long startOtDay = DoorUtils.getStartOfDay(maxPostDate);
		
		long postTime;
		
		int idxCnt = 0;
		
		for(List<String> tmpl : tmplWoCntntIds){
			
			int rndDayCnt = postInterval[0] * (idxCnt/postInterval[1]) + rnd.nextInt(postInterval[0]);
			postTime = DoorUtils.getRndNormalDistTime() + startOtDay + DoorUtils.DAY_MIL_SEC_CNT * (rndDayCnt);
			
			//post/add new article content
			artCntntDAO.postArticle(
					Integer.valueOf(tmpl.get(0)), 
					synonymizeText(tmpl.get(1)), 
					false, 
					postTime
			);
			
			idxCnt++;
		}
	}
	
	private void updateExpiredArticles(int updateInterval) throws SQLException{
		//Get all expired articles
		List<List<String>> tmpl4Update = artCntntDAO.getArticles4Update(updateInterval);
		
		long curTime = System.currentTimeMillis();
		long startOtDay = DoorUtils.getStartOfDay(curTime);
		long postTime;
		
		for(List<String> tmpl : tmpl4Update){
			postTime = DoorUtils.getRndNormalDistTime() + startOtDay;
			artCntntDAO.postArticle(
					Integer.valueOf(tmpl.get(0)),
					synonymizeText(tmpl.get(1)), 
					true, 
					postTime
			);
		}
	}
	
	private int deleteDeprecatedArticles(){
		return artCntntDAO.deleteDeprecatedPageContent();
	}
	
	private  String synonymizeText(String text){
		Pattern ptrn = Pattern.compile("(\\{([^\\{\\}]+)\\})");
		Matcher mtch = ptrn.matcher(text);

		while(mtch.find()){
			String[] array = mtch.group(2).split("\\|");
			text = text.replace(mtch.group(1), array[rnd.nextInt(array.length)]);
			mtch = ptrn.matcher(text);
		}
		
		text = text.replaceAll("\r\n", "</br>\r\n");

		return text;
	}
	
	private int[] readPostInterval(String postIntervalStr){
		int[] result = new int[]{1,1};
		
		String[] freqArray = postIntervalStr.split(":");
		
		if(freqArray.length == 1){
			result[0] = Integer.valueOf(freqArray[0]);
			result[1] = Integer.valueOf(freqArray[0]);
		}else if(freqArray.length == 2){
			result[0] = Integer.valueOf(freqArray[0]);
			result[1] = Integer.valueOf(freqArray[1]);
		}
		
		return result;
	}
	
	private String processNewTemplate(String title, String tmplContent)
	{
		//Getting url key for fileName
		String keyUrl = makeUrlKey(title);
		
		int genKey = artTmplDAO.insertTemplate(title, keyUrl, tmplContent);
		
		if(genKey > 0)
		{
			log.info(String.format("Template [id=%d, title='%s', url='%s'] was successfully added", genKey, title, keyUrl));
		}else
		{
			log.info(String.format("Template [title='%s', url='%s'] was updated", title, keyUrl));
		}
		
		return keyUrl;
	}
	
	private String makeUrlKey(String input){
		return input.replaceAll("[^0-9a-zA-z\\s\\-]", " ").replaceAll("\\s+", "-").toLowerCase();
	}

}
