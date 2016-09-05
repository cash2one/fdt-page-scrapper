package com.fdt.doorgen.generator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

import com.fdt.doorgen.generator.categories.Category;
import com.fdt.doorgen.generator.categories.CategoryConfigInfo;
import com.fdt.doorgen.generator.categories.Item;
import com.fdt.doorgen.generator.dao.CategoryDao;
import com.fdt.doorgen.generator.dao.CategoryItemDao;
import com.fdt.doorgen.generator.xmlparser.CustomParser;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

@Configuration
//@EnableAutoConfiguration
@ComponentScan(basePackages = "com.fdt", excludeFilters={
		@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value = com.fdt.scrapper.proxy.ProxyFactory.class),
		@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value = com.fdt.doorgen.key.pooler.articles.ArticlesPosterUpdaterRunner.class)
})
@PropertySource(value="file:${config.file}",ignoreResourceNotFound = true)
public class DoorgenGeneratorRunner {

	public static final String PRETITLES_REGEXP = "onestringpage";
	private static final String PRETITLES_W_IDX_REGEXP = PRETITLES_REGEXP + "(\\d)";
	private static final String PRETITLES_FILE_NAMES_REGEXP =PRETITLES_W_IDX_REGEXP +".txt";

	private static final Logger log = Logger.getLogger(DoorgenGeneratorRunner.class);

	//TODO Read host name from config
	private String connectionString = null;

	private static final String TEMPLATE_FOLDER_LABEL = "template_folder";
	private static final String OUTPUT_FOLDER_LABEL = "output_folder";
	private static final String MENU_FILE_LABEL = "menu_file";
	private static final String CATEGORIES_FOLDER_LABEL = "categories_folder";

	private static final String CONNECTION_STRING_LABEL = "connection_string";

	private File menuFile;

	private File templateFolder;

	private File outputFolder;

	private File categoriesFolder;

	private File mainTmplFile;

	private HashMap<Integer,List<String>> preTitles;
	
	private HashMap<String,String> catUrls;

	private String titleTmpl;

	private String metaDescrTmpl;

	private String metaKeyWordsTmpl;

	private CategoryConfigInfo catConfig;

	@Autowired
	private ConfigManager configManager;

	//--------------CATEGORIES-----------------------
	private HashMap<Category, List<Item>> categoriesList = new HashMap<Category, List<Item>>();
	//-----------------------------------------------

	private Random rnd = new Random();

	private Connection connection;

	private CategoryDao categoryDao;
	private CategoryItemDao categoryItemDao;

	//---------------------REPLACE_LABELS------------------------------------
	private static final String TOP_MENU_LABEL="\\[TOP_MENU\\]";
	private static final String BOTTOM_MENU_LABEL="\\[BOTTOM_MENU\\]";
	//----------------------------------------------------------------------------

	/**
	 * args[0] - path to config file
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		ApplicationContext ctx = SpringApplication.run(DoorgenGeneratorRunner.class, args);

		System.out.println("Let's inspect the beans provided by Spring Boot:");

		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			System.out.println(beanName);
		}

		DOMConfigurator.configure("log4j.xml");
		DoorgenGeneratorRunner taskRunner = ctx.getBean(DoorgenGeneratorRunner.class);

		taskRunner.executeGenerator();
	}

	public DoorgenGeneratorRunner()
	{
		super();
	}

	@PostConstruct
	private void init() throws ClassNotFoundException, SQLException{
		this.connectionString = ConfigManager.getInstance().getProperty(CONNECTION_STRING_LABEL);

		this.menuFile = new File(ConfigManager.getInstance().getProperty(MENU_FILE_LABEL));

		this.templateFolder = new File(ConfigManager.getInstance().getProperty(TEMPLATE_FOLDER_LABEL));

		this.outputFolder = new File(ConfigManager.getInstance().getProperty(OUTPUT_FOLDER_LABEL));

		this.mainTmplFile = new File(this.outputFolder, "tmpl_main.html");

		this.categoriesFolder = new File(ConfigManager.getInstance().getProperty(CATEGORIES_FOLDER_LABEL));

		catConfig = new CategoryConfigInfo(ConfigManager.getInstance());

		connection = getConnection();

		categoryDao = new CategoryDao(connection);
		categoryItemDao = new CategoryItemDao(connection);
	}

	private void executeGenerator() throws Exception{
		try{
			generateSiteStructure();
			//Загрузаем темплейты для тайтла, дискрипшна и кейвордов
			loadPageMetaData(categoriesFolder.getParentFile());
			//Загружаем данные для претайтлов
			preTitles = Utils.loadPreTtls(categoriesFolder.getParentFile(), PRETITLES_FILE_NAMES_REGEXP);
			//Загружаем список урлов для категорий
			loadCatUrls(categoriesFolder.getParentFile());
			//TODO Load categories and their configs
			//TODO Uncomment
			loadAndGenerateCategoriesContent();

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

	private void loadAndGenerateCategoriesContent() {
		//Clean data base
		categoryDao.deleteAllCategories();
		categoryItemDao.deleteAllItems();

		//Load data structure
		for(File catFolder : this.categoriesFolder.listFiles()){
			if(catFolder.isDirectory()){
				//TODO Загружаем тэги, которые будут отображаться внизу страницы категорий
				List<Item> tagsItems = loadTagItems(catFolder);
				//TODO Load category
				Category category = Category.parseCategory(catFolder, tagsItems, titleTmpl, metaDescrTmpl, metaKeyWordsTmpl, preTitles, catUrls);

				categoriesList.put(category, tagsItems);
			}
		}

		int[] addedElements = null;
		//Save data to DB
		addedElements = categoryDao.insertCategories(categoriesList.keySet());
		addedElements = categoryItemDao.insertItems(categoriesList);

		//TODO Make changes in index.php

	}

	private List<Item> loadTagItems(File catFolder){
		List<Item> catItems = new ArrayList<Item>();

		File tagFile = new File(catFolder, "tags.txt");

		if(tagFile.exists()){

			List<String> keys = Utils.loadFileAsStrList(new File(catFolder, "tags.txt"));

			for(String key : keys){
				catItems.add(Item.parseItem(key));
			}
		}

		return catItems;
	}

	private void loadPageMetaData(File mainFolder){

		List<String> keys = Utils.loadFileAsStrList(new File(mainFolder, "metacategories.txt"));

		titleTmpl = keys.get(0);
		metaDescrTmpl = keys.get(1);
		metaKeyWordsTmpl = keys.get(2);
	}

	private void loadCatUrls(File mainFolder){

		catUrls = new HashMap<String,String>();
		
		for(String catUrl : Utils.loadFileAsStrList(new File(mainFolder,"categories_urls.txt"))){
			String[] urlPar = catUrl.split("~", 2);
			catUrls.put(urlPar[0].trim(), urlPar[1].trim());
		}

	}

	private void generateSiteStructure() throws IOException{
		//Clean output folder
		cleanFolders();

		//Copy file from input to output folder
		copyFilesFolders();

		//Generate menu header and footer and update template files
		populateMenu(this.menuFile);

		//TODO Make changes in index.php - Add mapping of menu links and files
	}

	private void saveMenuMap(Set<MenuItem> allMenuItems){
		File menuMapFolder = new File(this.outputFolder,"menu_map");
		if(menuMapFolder.exists() && menuMapFolder.isDirectory()){
			menuMapFolder.delete();
		}

		menuMapFolder.mkdir();

		//TODO create map file
		File menuMapFile = new File(menuMapFolder,"menu_map.ini");
		menuMapFile.delete();

		for(MenuItem mi : allMenuItems){
			Utils.appendStringToFile(String.format("%s=%s^%s^%s^%s^%s", mi.getHref(),  mi.getContentFile(), mi.getPageTitle(), mi.getPageMetaKeywords(), mi.getPageMetaDescription(), mi.getTmplLabel()),menuMapFile);
		}

	}

	private void cleanFolders() {
		if(this.outputFolder.exists() && this.outputFolder.isDirectory()){
			this.outputFolder.delete();
		}

		this.outputFolder.mkdir();
	}

	private void copyFilesFolders() throws IOException {
		FileUtils.copyDirectory(this.templateFolder, this.outputFolder);
	}

	private void populateMenu(File menuFile){

		CustomParser parser = new CustomParser();
		List<MenuItem> topMenuItems = parser.parseMenu(menuFile, "/menu/header/menu_item");
		List<MenuItem> bottomMenuItems = parser.parseMenu(menuFile, "/menu/footer/menu_item");

		StringBuffer topMenu = new StringBuffer();
		StringBuffer bottomMenu = new StringBuffer();

		for(MenuItem item : topMenuItems){
			topMenu.append(item.toString()).append("\r\n");
		}

		for(MenuItem item : bottomMenuItems){
			bottomMenu.append(item.toString()).append("\r\n");
		}

		//TODO Replace content in file

		Utils.replaceStrInFile(TOP_MENU_LABEL, topMenu.toString(), mainTmplFile.getPath());
		Utils.replaceStrInFile(BOTTOM_MENU_LABEL, bottomMenu.toString(), mainTmplFile.getPath());

		//save menu_map.ini file
		Set<MenuItem> allMenuItems = new HashSet<MenuItem>();
		allMenuItems.addAll(topMenuItems);
		allMenuItems.addAll(bottomMenuItems);

		saveMenuMap(allMenuItems);
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		//Setup the connection with the DB
		Connection connection = (Connection) DriverManager.getConnection(this.connectionString);

		return connection;
	}

}
