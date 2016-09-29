package com.fdt.doorgen.generator;

import java.io.File;
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
import com.fdt.doorgen.generator.categories.Tag;
import com.fdt.doorgen.generator.categories.Tag.CategoryParentType;
import com.fdt.doorgen.generator.dao.CategoryDao;
import com.fdt.doorgen.generator.dao.CategoryItemDao;
import com.fdt.doorgen.generator.dao.CategoryTagDao;
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
	
	private static final String PATH_2_FILE_W_DEF_TAGS_4_CATS_LABEL = "path_2_file_w_def_tags_4_cat";
	private static final String PATH_2_FILE_W_DEF_TAGS_4_ITEMS_LABEL = "path_2_file_w_def_tags_4_items";

	public static final String CONNECTION_STRING_LABEL = "connection_string";

	private File menuFile;

	private File templateFolder;

	private File outputFolder;

	private File categoriesFolder;

	private File mainTmplFile;
	
	private File path2DefTags4Cats;
	
	private File path2DefTags4Items;

	private HashMap<Integer,List<String>> preTitles;
	
	private HashMap<String,String> catUrls;

	
	//categories
	private String titleTmplCats;
	private String metaDescrTmplCats;
	private String metaKeyWordsTmplCats;
	
	//items
	private String titleTmplItems;
	private String metaDescrTmplItems;
	private String metaKeyWordsTmplItems;
	
	private List<Tag> defTags4Cats;
	
	private List<Tag> defTags4Items;

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
	private CategoryTagDao categoryTagDao;

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
		
		this.path2DefTags4Cats = new File(ConfigManager.getInstance().getProperty(PATH_2_FILE_W_DEF_TAGS_4_CATS_LABEL));
		
		this.path2DefTags4Items = new File(ConfigManager.getInstance().getProperty(PATH_2_FILE_W_DEF_TAGS_4_ITEMS_LABEL));

		catConfig = new CategoryConfigInfo(ConfigManager.getInstance());

		connection = getConnection();

		categoryDao = new CategoryDao(connection);
		categoryItemDao = new CategoryItemDao(connection);
		categoryTagDao = new CategoryTagDao(connection);
	}

	private void executeGenerator() throws Exception{
		try{
			generateSiteStructure();
			//Загрузаем темплейты для тайтла, дискрипшна и кейвордов для categories
			loadPageMetaData4Cats(categoriesFolder.getParentFile());
			//Загрузаем темплейты для тайтла, дискрипшна и кейвордов для items
			loadPageMetaData4Items(categoriesFolder.getParentFile());
			//Загружаем данные для претайтлов
			preTitles = Utils.loadPreTtls(categoriesFolder.getParentFile(), PRETITLES_FILE_NAMES_REGEXP);
			//Загружаем список урлов для категорий
			loadCatUrls(categoriesFolder.getParentFile());
			//load default tags categories
			defTags4Cats = Tag.loadTags(new File(categoriesFolder.getParentFile(),path2DefTags4Cats.getName()), 0, CategoryParentType.CATEGORY, "", preTitles);
			//load default tags categories
			defTags4Items = Tag.loadTags(new File(categoriesFolder.getParentFile(),path2DefTags4Items.getName()), 0, CategoryParentType.ITEM, "", preTitles);
			
			//TODO Load categories, items and their configs
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
		categoryTagDao.deleteAllTags();
		
		//TODO Load default tags
		

		//Load data structure
		for(File catFolder : this.categoriesFolder.listFiles()){
			if(catFolder.isDirectory()){
				//TODO Load category
				Category category = Category.parseCategory(catFolder, titleTmplCats, metaDescrTmplCats, metaKeyWordsTmplCats, preTitles, catUrls);
							
				//load items and save them whit category
				categoriesList.put(category, loadCatItems(catFolder));
			}
		}

		int[] addedElements = null;
		//Save data to DB
		addedElements = categoryDao.insertCategories(categoriesList.keySet());
		
		int catIdx = 0;
		for(Category cat : categoriesList.keySet()){
			categoryTagDao.insertTags(cat.getTags(), addedElements[catIdx++]);
		}
		
		addedElements = categoryItemDao.insertItems(categoriesList);
		
		addedElements = categoryTagDao.insertTags(defTags4Cats,0);
		addedElements = categoryTagDao.insertTags(defTags4Items,0);

		//TODO Make changes in index.php

	}

	//load catagery items
	private List<Item> loadCatItems(File catFolder){
		List<Item> items = new ArrayList<Item>();

		File itemFile = new File(catFolder, "items.txt");
		
		String itemContentTmpl = Utils.loadFileAsString(new File(catFolder.getParentFile().getParentFile(),"items_main_tmpl.txt"));

		if(itemFile.exists() && itemFile.isFile()){

			List<String> itemLst = Utils.loadFileAsStrList(new File(catFolder, "items.txt"));

			for(String itemStr : itemLst){
				Item parcedItem = Item.parseItem(itemStr);
				parcedItem.setText_tmpl(itemContentTmpl);
				parcedItem.setText(Utils.synonymizeText(Utils.fillTemplate(itemContentTmpl, "", preTitles, DoorgenGeneratorRunner.PRETITLES_REGEXP)));
				items.add(parcedItem);
			}
		}

		return items;
	}

	private void loadPageMetaData4Cats(File mainFolder){

		List<String> keys = Utils.loadFileAsStrList(new File(mainFolder, "metacategories_categories.txt"), true);

		titleTmplCats = keys.get(0);
		metaDescrTmplCats = keys.get(1);
		metaKeyWordsTmplCats = keys.get(2);
	}
	
	private void loadPageMetaData4Items(File mainFolder){

		List<String> keys = Utils.loadFileAsStrList(new File(mainFolder, "metacategories_items.txt"), true);

		titleTmplItems = keys.get(0);
		metaDescrTmplItems = keys.get(1);
		metaKeyWordsTmplItems = keys.get(2);
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
