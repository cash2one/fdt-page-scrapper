package com.fdt.multisnipgen.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.filemapping.RowScope;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.scrapper.task.TaskFactory;
import com.fdt.utils.Utils;

/**
 *
 * @author Administrator
 */
public class MegaMultipleSnippetGeneratorRunner{
	private static final String LANG_LABEL = "lang";

	private static final String SOURCE_LABEL = "source";
	private static final String FREQUENCY_LABEL = "frequency";

	private static final Logger log = Logger.getLogger(MegaMultipleSnippetGeneratorRunner.class);

	private String proxyFilePath;

	private String keyWordsInputFolderPath;
	private String keyWordsOutputFolderPath;

	//словарь в котором содержится <ключ> и в соответствие ему ставится файл, в котором содержится обрабатываемый ключ
	private Map<String, List<File>> keyWordFileMapping = new HashMap<String, List<File>>();
	protected static Lock readWriteLock = new ReentrantLock();

	private int maxThreadCount;
	private long proxyDelay;

	private String source = null;
	private String lang = null;
	private int[] frequencies = null;

	private int maxPageNum = 50;

	private Boolean addLinkFromFolder = false;

	private HashMap<String, File> mappingLinkFileMap = new HashMap<String, File>();

	private String linksListFilePath;

	private ArrayList<String> linksList = new ArrayList<String>();
	
	private boolean isUseLinkCnt4SnpCnt = false;
	private int minSnpCntIncrement = 0;
	private int maxSnpCntIncrement = 3;

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String LINKS_LIST_FILE_PATH_LABEL = "links_list_file_path";

	protected final static String KEY_WORDS_FILE_INPUT_FOLDER_LABEL = "key_words_file_input_folder_path";
	protected final static String KEY_WORDS_FILE_OUTPUT_FOLDER_LABEL = "key_words_file_output_folder_path";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";

	private static final String SNIPPET_SEARCH_PAGE_MAX_LABEL = "snippet_search_page_max";

	private static final String ADD_LINK_FROM_FOLDER_FILES_LABEL = "add_links_from_folder_files";
	private static final String PATH_TO_LINK_FOLDER_LABEL = "path_to_link_folder";
	
	private static final String USE_LINK_CNT_4_SNP_CNT_LABEL = "use_link_cnt_4_snp_cnt";
	private static final String MAX_SNP_CNT_INCREMENT_LABEL = "max_snp_cnt_increment";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 5000L;

	private final File FOLDER_4_PROCESSING_LINK_FILES = new File("processed_link_files");

	private final File FOLDER_4_PROCESSING_INPUT_FILE = new File("processing");

	Random rnd = new Random();

	private TaskFactory taskFactory;
	private ProxyFactory proxyFactory;

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

				MegaMultipleSnippetGeneratorRunner taskRunner = null;
				try {
					taskRunner = new MegaMultipleSnippetGeneratorRunner(args[0]);
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

	public MegaMultipleSnippetGeneratorRunner(String cfgFilePath) throws Exception{

		ConfigManager.getInstance().loadProperties(cfgFilePath);
		this.taskFactory = TaskFactory.getInstance();
		this.proxyFilePath = ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);

		this.keyWordsInputFolderPath = ConfigManager.getInstance().getProperty(KEY_WORDS_FILE_INPUT_FOLDER_LABEL);
		this.keyWordsOutputFolderPath = ConfigManager.getInstance().getProperty(KEY_WORDS_FILE_OUTPUT_FOLDER_LABEL);

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

		taskFactory.loadTaskQueue(getTaskList(), source, frequencies, lang);

		//init proxy factory
		ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(proxyFilePath);

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

		if(addLinkFromFolder)
		{
			String linkFolderValue = ConfigManager.getInstance().getProperty(PATH_TO_LINK_FOLDER_LABEL);

			if(linkFolderValue != null && !"".equals(linkFolderValue.trim()))
			{
				//TODO split link folders to separeted items
				String[] mappingLinkFile = linkFolderValue.trim().split(";");

				for(String elements : mappingLinkFile)
				{
					String[] linksElem = elements.split("=");
					mappingLinkFileMap.put(linksElem[0].trim(), new File(linksElem[1].trim()).getCanonicalFile());
				}
			}
		}

		if(!addLinkFromFolder){
			//загружаем линки из общего файла с линками
			this.linksList = loadLinkListFromFile(this.linksListFilePath);
		}
		
		String isUseLinkCnt4SnpCntStr = ConfigManager.getInstance().getProperty(USE_LINK_CNT_4_SNP_CNT_LABEL);
		if(isUseLinkCnt4SnpCntStr != null && !"".equals(isUseLinkCnt4SnpCntStr.trim())){
			isUseLinkCnt4SnpCnt = Boolean.valueOf(isUseLinkCnt4SnpCntStr);
		}
		
		if(isUseLinkCnt4SnpCnt){
			String maxSnpCntIncrementStr = ConfigManager.getInstance().getProperty(MAX_SNP_CNT_INCREMENT_LABEL);
			
			if(maxSnpCntIncrementStr != null && !"".equals(maxSnpCntIncrementStr.trim()))
			{
				String values[] = maxSnpCntIncrementStr.split("-");
				if(values.length == 1){
					maxSnpCntIncrement = Integer.parseInt(values[0]);
				}else if(values.length == 2){
					minSnpCntIncrement = Integer.parseInt(values[0]);
					maxSnpCntIncrement = Integer.parseInt(values[1]);
				}else{
					throw new NumberFormatException("Input string does not satisfied required format <number>[-<number>]. Input string: " + maxSnpCntIncrementStr);
				}			
			}
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

	private ArrayList<String> getTaskList() throws IOException
	{
		ArrayList<String> taskRowList = new ArrayList<String>();
		File inputFolder = new File(keyWordsInputFolderPath);

		for(File file : inputFolder.listFiles())
		{
			if(file.isFile())
			{

				List<String> rowsList = Utils.loadFileAsStrList(file);

				String keyWord = rowsList.get(1);

				try{
					readWriteLock.lock();

					//перемещаем файл в processing папку, чтобы в дальнейшем он случайно не был взят для обработки
					File destFile = new File(FOLDER_4_PROCESSING_INPUT_FILE, file.getName());
					if(destFile.exists()){
						log.warn(String.format("Another files %s exist in processing folder. Another file will be deleted", file.getName()));
						destFile.delete();
					}

					log.warn(String.format("Moving file %s to process folder", file.getName()));
					FileUtils.moveFile(file, destFile);
					file.delete();

					List<File> fileList = keyWordFileMapping.get(keyWord);

					if(fileList != null)
					{
						fileList.add(destFile);
					}
					else {
						//добавляем кейворд для обработки только в том случае, когда у нас он ещё не был добавлен, иначе просто добавляем новый файл в keyWordFileMapping
						taskRowList.add(keyWord);
						fileList = new ArrayList<File>();
						fileList.add(destFile);
					}

					keyWordFileMapping.put(keyWord, fileList);
				}finally{
					readWriteLock.unlock();
				}
			}
		}

		return taskRowList;
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

				MegaMultipleSnippetGeneratorThread newThread = null;
				log.info("Total tasks: "+taskFactory.getTaskQueue().size());

				while(true){
					log.debug("Try to get request from RequestFactory queue.");
					SnippetTaskWrapper task = taskFactory.getTask();
					log.debug("Task: " + task);
					if(task != null){
						log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
						task.getCurrentTask().setPage(rnd.nextInt(maxPageNum));

						File linkFile = null;
						File linkFolder = null;
						this.linksList = new ArrayList<String>();

						String inputFileName = keyWordFileMapping.get(task.getCurrentTask().getKeyWordsOrig()).get(0).getName();

						boolean isAddLink = inputFileName.startsWith("no_link")? false:true;

						if(isAddLink){
							linkFolder = getLinkFolder(inputFileName);
							
							if(addLinkFromFolder)
							{
								linkFile = getRandomLinkFile(linkFolder);
								
								this.linksList = loadLinkList(linkFile);
								
								if(linkFile == null){	
									taskFactory.reprocessingTask(task);
									break;
								}
							}
						}

						newThread = new MegaMultipleSnippetGeneratorThread(
								task, 
								proxyFactory, 
								taskFactory,
								isAddLink,
								addLinkFromFolder, 
								linksList,
								linkFile, 
								linkFolder, 
								keyWordFileMapping, 
								new File(keyWordsOutputFolderPath),
								isUseLinkCnt4SnpCnt,
								minSnpCntIncrement,
								maxSnpCntIncrement
								);
						executor.submit(newThread);
						continue;
					}else{
						try {
							taskFactory.loadTaskQueue(getTaskList(), source, frequencies, lang);
						} catch (Exception e) {
							log.error("Error occured during load tasks from input folder: " + keyWordsInputFolderPath, e);
						}
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
					e1.printStackTrace();
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

	private File getLinkFolder(String inputFileName)
	{
		File linkFolder = null;
		
		for(String prefix : mappingLinkFileMap.keySet()){
			if(inputFileName.startsWith(prefix)){
				linkFolder = mappingLinkFileMap.get(prefix);
				break;
			}
		}

		//если не нашли нужную папку, то берём папку по умолчанию
		if(linkFolder == null || !linkFolder.exists()){
			linkFolder = mappingLinkFileMap.get("default");
		}
		
		return linkFolder;
	}

	//берём случайны файл с линками из папки pathToLinkFolder, читаем из него линки, перемещаем файл в папку folder4ProcessedLinkFiles
	public synchronized File getRandomLinkFile(File pathToLinkFolder){
		File linkFile = null;

		File[] files = pathToLinkFolder.listFiles();
		if(files == null || files.length == 0){
			return null;
		}
		//get random file and load link list for it
		linkFile = files[rnd.nextInt(files.length)];
		try {
			FileUtils.moveFile(linkFile, new File(FOLDER_4_PROCESSING_LINK_FILES,linkFile.getName()));
			linkFile = new File(FOLDER_4_PROCESSING_LINK_FILES, linkFile.getName());
			log.debug(String.format("Moved file %s", linkFile.getName()));
		} catch (IOException e) {
			log.error(String.format("Error during moving file %s", linkFile.getName()),e);
		}

		return linkFile;
	}

	private synchronized ArrayList<String> loadLinkListFromFile(String lnksLstFilePath){
		//else just load list links from single file
		File linkFile = new File(lnksLstFilePath);
		return loadLinkList(linkFile);
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
