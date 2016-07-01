package com.fdt.doorgen.generator;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.doorgen.generator.categories.Category;
import com.fdt.doorgen.generator.categories.CategoryConfigInfo;
import com.fdt.doorgen.generator.categories.Item;
import com.fdt.doorgen.generator.dao.CategoryDao;
import com.fdt.doorgen.generator.dao.CategoryItemDao;
import com.fdt.doorgen.generator.xmlparser.CustomParser;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

public class DoorgenGeneratorRunner {

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
	
	private CategoryConfigInfo catConfig;
	
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
		if(args.length < 1){
			System.out.print("Not enought arguments....");
		}else{
			System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
			ConfigManager.getInstance().loadProperties(args[0]);
			System.out.println(args[0]);

			DOMConfigurator.configure("log4j_doorgen_gen.xml");

			DoorgenGeneratorRunner taskposter = null;
			try {
				taskposter = new DoorgenGeneratorRunner(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}

			taskposter.executeGenerator();
		}
	}

	private void executeGenerator() throws Exception{
		try{
			generateSiteStructure();
			
			//TODO Load categories and their configs
			//TODO Uncomment
			//loadAndGenerateCategoriesContent();
			
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
				//TODO Load category
				Category category = Category.parseCategory(catFolder);
				//TODO Load cat info and subfiles
				List<Item> catItems = loadCatItems(catFolder);
				
				categoriesList.put(category, catItems);
			}
		}
		
		int[] addedElements = null;
		//Save data to DB
		addedElements = categoryDao.insertCategories(categoriesList.keySet());
		addedElements = categoryItemDao.insertItems(categoriesList);
		
		//TODO Make changes in index.php
		
	}
	
	private List<Item> loadCatItems(File catFolder){
		List<Item> catItems = new ArrayList<Item>();
		
		List<String> keys = Utils.loadFileAsStrList(new File(catFolder, "keys.txt"));
		
		for(String key : keys){
			catItems.add(Item.parseItem(key));
		}
		
		return catItems;
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
			Utils.appendStringToFile(String.format("%s=%s^%s^%s^%s", mi.getHref(),  mi.getContentFile(), mi.getPageTitle(), mi.getPageMetaKeywords(), mi.getPageMetaDescription() ), menuMapFile);
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

	public DoorgenGeneratorRunner(String cfgFilePath) throws Exception
	{
		ConfigManager.getInstance().loadProperties(cfgFilePath);

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

	private Connection getConnection() throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		//Setup the connection with the DB
		Connection connection = (Connection) DriverManager.getConnection(this.connectionString);

		return connection;
	}

}
