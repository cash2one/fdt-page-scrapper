package com.fdt.doorgen.key.pooler.articles;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.fdt.doorgen.key.pooler.content.ContentStrategy;
import com.fdt.doorgen.key.pooler.dao.ArticleContentDao;
import com.fdt.doorgen.key.pooler.dao.ArticleTemplateDao;
import com.fdt.doorgen.key.pooler.runner.DoorgenPoolerSnippetsRunner;
import com.fdt.doorgen.key.pooler.util.DoorUtils;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

@Configuration
//@EnableAutoConfiguration
@ComponentScan(basePackages = "com.fdt", excludeFilters={
		@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value = com.fdt.scrapper.proxy.ProxyFactory.class)
})
//@PropertySource(value="file:${config.file}",ignoreResourceNotFound = true)
public class ArticlesPosterUpdaterRunner {

	private static final Logger log = Logger.getLogger(DoorgenPoolerSnippetsRunner.class);
	
	public static final String PRETITLES_REGEXP = "onestringpage";
	private static final String PRETITLES_W_IDX_REGEXP = PRETITLES_REGEXP + "(\\d)";
	private static final String PRETITLES_FILE_NAMES_REGEXP =PRETITLES_W_IDX_REGEXP +".txt";

	private String connectionString = null;

	public static ContentStrategy STRATEGY_POLLER;

	private File newArticlesFolderPath;

	private int updateInterval;

	private int[] postInterval;

	private static final String CONNECTION_STRING_LABEL = "connection_string";

	private static final String NEW_ARTICLES_FOLDER_PATH_LABEL = "new_articles_folder_path";

	private static final String UPDATE_INTERVAL_LABEL = "update_interval";

	private static final String POST_INTERVAL_LABEL = "post_interval";

	private static boolean oneDayLoad = false;
	
	private HashMap<Integer,List<String>> preTitles;
	
	@Autowired
	private ConfigManager configManager;

	/**Использовать ли следующую структуру папок
	 *	Главная
	 *		Статья1_папка
	 *			main.txt
	 *			tags.txt 
	 */
	private static boolean useNewFolderStructure = false;

	private Random rnd = new Random();

	private Connection connection;

	private ArticleTemplateDao artTmplDAO; 
	private ArticleContentDao artCntntDAO; 

	/**
	 * args[0] - path to config file
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
		System.out.println(args[0]);
		
		System.setProperty("config.file", args[0]);

		oneDayLoad = Boolean.valueOf(args[1]);

		/*использовать ли новую структуру папок со статьями*/
		useNewFolderStructure = Boolean.valueOf(args[2]);
		

		ApplicationContext ctx = SpringApplication.run(ArticlesPosterUpdaterRunner.class, args);

		System.out.println("Let's inspect the beans provided by Spring Boot:");

		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			System.out.println(beanName);
		}

		DOMConfigurator.configure("log4j_articles_updater.xml");
		ArticlesPosterUpdaterRunner taskRunner = ctx.getBean(ArticlesPosterUpdaterRunner.class);

		taskRunner.executeWrapper();
	}

	public ArticlesPosterUpdaterRunner() throws Exception
	{
		super();
	}
	
	@PostConstruct
	private void init() throws ClassNotFoundException, SQLException{
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
		String titleTmpl;
		String descriptionTmpl;
		String keyWordsTmpl;
		ArrayList<String> addedUrls = new ArrayList<String>();

		//Read and post new articles from input files
		if( !useNewFolderStructure ){
			for(File file : newArticlesFolderPath.listFiles()){
				tmplContent= Utils.loadFileAsString(file);
				keyUrl = processNewTemplate(FilenameUtils.removeExtension(file.getName()), FilenameUtils.removeExtension(file.getName()), tmplContent);
				addedUrls.add(keyUrl);
				//TODO move processed file to temp folder???
			}
		}else{
			//пропускаем первые 3 строки, т.к. там хранятся мета данные
			List<Integer> skipList = Arrays.asList(new Integer[]{});
			//берём только папки, в названии которых указаны названия статей
			
			preTitles = Utils.loadPreTtls(newArticlesFolderPath, PRETITLES_FILE_NAMES_REGEXP);
			
			//Load meta
			List<String> metaData = Utils.loadFileAsStrList(new File(newArticlesFolderPath,"articles_metacategories.txt"));
			
			for(File file : newArticlesFolderPath.listFiles()){
				if(file.isDirectory()){
					tmplContent= Utils.loadFileAsString(new File(file,"main.txt"),skipList);
					String articleName = file.getName();
					
					titleTmpl = Utils.fillTemplate(metaData.get(0), articleName, preTitles, PRETITLES_REGEXP);
					descriptionTmpl = Utils.fillTemplate(metaData.get(1), articleName, preTitles, PRETITLES_REGEXP);
					keyWordsTmpl = Utils.fillTemplate(metaData.get(2), articleName, preTitles, PRETITLES_REGEXP);

					//Load tags
					StringBuffer tagListStr = new StringBuffer();
					File tagFile = new File(file,"tags.txt");
					if(tagFile.exists()){
						List<String> tags = Utils.loadFileAsStrList(tagFile);

						tagListStr.append("<table cellpadding=\"5\" width=\"100%\"><tbody>");

						for(int i = 0; i < tags.size(); i++){
							if(i % 3 == 0){
								tagListStr.append("<tr>");
							}else if(i % 3 == 3){
								tagListStr.append("</tr><tr>");
							}

							tagListStr.append("<td style=\"line-height:20px; font-size:15px;\" align=\"left\" valign=\"top\"><a href=\"\" title=\"" + tags.get(i) + "\" >#" + tags.get(i) + "</a><br></td>");

						}

						tagListStr.append("</tr></tbody></table>");
					}

					tmplContent = tmplContent + tagListStr.toString();

					keyUrl = processNewTemplate(articleName, titleTmpl, tmplContent, descriptionTmpl, keyWordsTmpl);
					addedUrls.add(keyUrl);
				}
			}
		}

		return addedUrls;
	}

	private void postNewlyAddedArticles() throws SQLException{
		List<List<String>> tmplWoCntntIds =  artTmplDAO.getTmplsWOPostedArticles();

		long maxPostDate = artCntntDAO.getMaxPostDate()*1000;
		long startOtDay = DoorUtils.getStartOfDay(maxPostDate);

		long postTime;

		int idxCnt = 0;

		Collections.shuffle(tmplWoCntntIds);

		for(List<String> tmpl : tmplWoCntntIds){
			//0 - количество дней, 1 - количество новостей
			//получаем в какой день постить новость
			int rndDayCnt = postInterval[0] * (idxCnt/postInterval[1]) + rnd.nextInt(postInterval[0]);
			//получаем время, в которое постить новость
			postTime = DoorUtils.getRndNormalDistTime() + startOtDay + DoorUtils.DAY_MIL_SEC_CNT * (rndDayCnt);

			//post/add new article content
			artCntntDAO.postArticle(
					Integer.valueOf(tmpl.get(0)), 
					Utils.synonymizeText(tmpl.get(1)), 
					false, 
					postTime
					);

			idxCnt++;

			
			// Если нам надо загрузить один день
			if(oneDayLoad && idxCnt/(postInterval[1]/postInterval[0]) >= 1){
				break;
			}
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
					Utils.synonymizeText(tmpl.get(1)), 
					true, 
					postTime
					);
		}
	}

	private int deleteDeprecatedArticles(){
		return artCntntDAO.deleteDeprecatedPageContent();
	}

	/**
	 * 
	 * @param postIntervalStr
	 * @return переодичность с которой новости будут появляться, int[0]:int[1]  
		формат такой, первое число int[0] - кол-во дней
		второе int[1] - количество новостей которые будут появляться за int[0] дней
	 */
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
	
	private String processNewTemplate(String titleOrig, String title, String tmplContent)
	{
		return processNewTemplate(titleOrig, title, tmplContent, "", "");
	}

	
	private String processNewTemplate(String titleOrig, String title, String tmplContent, String description, String keywords)
	{
		//Getting url key for fileName
		String keyUrl = makeUrlKey(titleOrig);

		int genKey = artTmplDAO.insertTemplate(titleOrig, title, keyUrl, tmplContent, description, keywords);

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
		return input.replaceAll("[^0-9a-zA-z\\s\\-]", " ").trim().replaceAll("\\s+", "-").toLowerCase();
	}

}
