package com.fdt.multisnipgen.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.IResultFormatter;
import com.fdt.scrapper.SaverThread;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.scrapper.task.TaskFactory;

/**
 *
 * @author Administrator
 */
public class MultipleSnippetGeneratorRunner{
	private static final String LANG_LABEL = "lang";

	private static final String SOURCE_LABEL = "source";
	private static final String FREQUENCY_LABEL = "frequency";

	private static final Logger log = Logger.getLogger(MultipleSnippetGeneratorRunner.class);

	private String proxyFilePath;
	private String keyWordsFilePath;
	private int maxThreadCount;
	private long proxyDelay;

	private String source = null;
	private String lang = null;
	private int[] frequencies = null;

	private int maxPageNum = 50;

	private Boolean addLinkFromFolder = false;
	private File pathToLinkFolder;

	private String linksListFilePath;

	private ArrayList<String> linksList = new ArrayList<String>();

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String LINKS_LIST_FILE_PATH_LABEL = "links_list_file_path";

	protected final static String KEY_WORDS_FILE_PATH_LABEL = "key_words_file_path";
	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";

	private static final String SNIPPET_SEARCH_PAGE_MAX_LABEL = "snippet_search_page_max";

	private static final String ADD_LINK_FROM_FOLDER_FILES_LABEL = "add_links_from_folder_files";
	private static final String PATH_TO_LINK_FOLDER_LABEL = "path_to_link_folder";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	private final File FOLDER_4_LOADED_LINK_FILES = new File("error");

	Random rnd = new Random();

	private TaskFactory taskFactory;
	private ProxyFactory proxyFactory;

	private SaverThread saver;

	/**
	 * args[0] - language
	 * args[1] - path to config file
	 */
	public static void main(String[] args){
		try{
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

				MultipleSnippetGeneratorRunner taskRunner = null;
				try {
					taskRunner = new MultipleSnippetGeneratorRunner(args[0]);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				taskRunner.execute();
			}
		}finally{
			//creation marker file
			try {
				FileWriter fw = new FileWriter("complete.txt", false);
				fw.write("complete");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public MultipleSnippetGeneratorRunner(String cfgFilePath) throws Exception{

		ConfigManager.getInstance().loadProperties(cfgFilePath);
		this.taskFactory = TaskFactory.getInstance();
		this.proxyFilePath = ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.keyWordsFilePath = ConfigManager.getInstance().getProperty(KEY_WORDS_FILE_PATH_LABEL);
		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
		this.proxyDelay = Integer.valueOf(ConfigManager.getInstance().getProperty(PROXY_DELAY_LABEL));

		this.source = ConfigManager.getInstance().getProperty(SOURCE_LABEL);
		this.lang = ConfigManager.getInstance().getProperty(LANG_LABEL);

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

		taskFactory.loadTaskQueue(keyWordsFilePath, source, frequencies, lang);

		//init proxy factory
		ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(proxyFilePath);

		//run saver thread
		saver = new SaverThread(taskFactory, ConfigManager.getInstance().getProperty(KEY_WORDS_FILE_PATH_LABEL), new IResultFormatter() {

			@Override
			public String formatResult(SnippetTask task) {
				return task.getResult();
			}
		});
		saver.start();

		//load links from file
		this.linksListFilePath = ConfigManager.getInstance().getProperty(LINKS_LIST_FILE_PATH_LABEL);

		String pageVaule = ConfigManager.getInstance().getProperty(SNIPPET_SEARCH_PAGE_MAX_LABEL);
		if(pageVaule != null && !"".equals(pageVaule.trim())){
			maxPageNum = Integer.valueOf(pageVaule);
		}

		String addLinkValue = ConfigManager.getInstance().getProperty(ADD_LINK_FROM_FOLDER_FILES_LABEL);
		if(addLinkValue != null && !"".equals(addLinkValue.trim())){
			addLinkFromFolder = Boolean.valueOf(addLinkValue);
		}

		if(addLinkFromFolder){
			String linkFolderValue = ConfigManager.getInstance().getProperty(PATH_TO_LINK_FOLDER_LABEL);
			if(linkFolderValue != null && !"".equals(linkFolderValue.trim())){
				pathToLinkFolder= new File(linkFolderValue).getCanonicalFile();
			}
		}

		if(!addLinkFromFolder){
			loadLinkListFromFile();
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


	public void execute(){
		try{
			synchronized (this) {
				ExecutorService executor = Executors.newFixedThreadPool(TaskFactory.getMAX_THREAD_COUNT());

				MultipleSnippetGeneratorThread newThread = null;
				log.info("Total tasks: "+taskFactory.getTaskQueue().size());

				while(!taskFactory.isTaskFactoryEmpty() || taskFactory.getRunThreadsCount() > 0){
					log.debug("Try to get request from RequestFactory queue.");
					SnippetTaskWrapper task = taskFactory.getTask();
					log.debug("Task: " + task);
					if(task != null){
						log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
						task.getCurrentTask().setPage(rnd.nextInt(maxPageNum));

						File linkFile = null;

						if(addLinkFromFolder)
						{
							linkFile = getLinkFile();
							if(linkFile == null){	
								taskFactory.reprocessingTask(task);
								break;
							}
						}

						newThread = new MultipleSnippetGeneratorThread(task, proxyFactory, taskFactory, linksList, addLinkFromFolder, linkFile, this.pathToLinkFolder);
						executor.submit(newThread);
						continue;
					}

					waiting(RUNNER_QUEUE_EMPTY_WAIT_TIME);
				}

				try {
					executor.shutdown();
					boolean finished = executor.awaitTermination(1, TimeUnit.MINUTES);
					while(!finished){
						finished = executor.awaitTermination(1, TimeUnit.MINUTES);
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				saver.stopped = true;
				while(saver.isAlive()){
					waiting(RUNNER_QUEUE_EMPTY_WAIT_TIME);
				}

				try {
					saver.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				log.info("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.getRunThreadsCount());
				log.info("Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
			}
		}finally{
		}
	}

	private void waiting(long time){
		try {
			this.wait(time);
		} catch (InterruptedException e) {
			log.error("InterruptedException occured during RequestRunner process",e);
		}
	}

	private File getLinkFile(){
		File linkFile = null;

		linkFile = getLinkFile(FOLDER_4_LOADED_LINK_FILES);
		
		//if link files is empty - end process execution
		while(linkFile == null)
		{
			File[] processedFiles = pathToLinkFolder.listFiles();
			if(processedFiles == null || processedFiles.length == 0){
				log.warn("Link files are absent. Program will be finished");
				break;
			}
			waiting(RUNNER_QUEUE_EMPTY_WAIT_TIME);
			linkFile = getLinkFile(FOLDER_4_LOADED_LINK_FILES);
		}
		return linkFile;
	}

	
	//берём случайны файл с линками из папки pathToLinkFolder, читаем из него линки, перемещаем файл в папку folder4ProcessedLinkFiles
	public synchronized File getLinkFile(File folder4ProcessedLinkFiles){
		File linkFile = null;

		File[] files = pathToLinkFolder.listFiles();
		if(files == null || files.length == 0){
			return null;
		}
		//get random file and load link list for it
		linkFile = files[rnd.nextInt(files.length)];
		try {
			FileUtils.moveFile(linkFile, new File(folder4ProcessedLinkFiles,linkFile.getName()));
			linkFile = new File(folder4ProcessedLinkFiles, linkFile.getName());
			log.debug(String.format("Moved file %s", linkFile.getName()));
		} catch (IOException e) {
			log.error(String.format("Error during moving file %s", linkFile.getName()),e);
		}

		this.linksList = loadLinkList(linkFile);

		return linkFile;
	}

	public synchronized void loadLinkListFromFile(){
		//else just load list links from single file
		File linkFile = new File(linksListFilePath);
		this.linksList = loadLinkList(linkFile);
	}

	private synchronized ArrayList<String> loadLinkList(File cfgFile){
		ArrayList<String> linkList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader( new FileInputStream(cfgFile), "UTF8" ));

			String line = br.readLine();
			while(line != null){
				String utf8Line = new String(line.getBytes(),"UTF-8");
				linkList.add(utf8Line.trim());
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.error("Reading PROPERTIES file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading PROPERTIES file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
		return linkList;
	}
}
