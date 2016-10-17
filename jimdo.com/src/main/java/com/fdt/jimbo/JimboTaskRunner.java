package com.fdt.jimbo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fdt.filemapping.RowMapping;
import com.fdt.jimbo.task.NewsTask;
import com.fdt.jimbo.task.TaskFactory;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

/**
 * @author VarenKoks
 */
@Configuration
@EnableAutoConfiguration

@ComponentScan(basePackages = "com.fdt")

@PropertySource("classpath:config.ini")
@PropertySource(value="file:${config.file}",ignoreResourceNotFound = true)
public class JimboTaskRunner 
{
	private static final Logger log = Logger.getLogger(JimboTaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	public final static String MAIN_URL_LABEL = "main_url";

	private String accListFilePath;

	private String inputMapppingFilePath;
	private String listInputFilePath;
	private String listProcessedFilePath;
	private String errorFilePath;

	private int maxThreadCount;

	private String templateFilePath;
	private String templateFilePathWOPic;

	private String outputFilePath;
	private String outputTitleFilePath;

	private String linksListFilePath;
	private Boolean addLinkFromFolder = false;

	private TaskFactory taskFactory;

	private File randImagesFilePath = null;
	private File randJpgFilePath = null;
	private File randButtonFilePath = null;
	private File randTitleFilePath = null;

	private String lang = null;
	private String source = null;
	private int[] frequencies = null;

	private RowMapping rowMapping = null;

	private Properties config = new Properties();
	
	@Autowired
	private ProxyFactory proxyFactory;
	
	@Autowired
	private ConfigManager configManager;

	private static final String LANG_LABEL = "lang";
	private static final String SOURCE_LABEL = "source";
	private static final String FREQUENCY_LABEL = "frequency";

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String ACCOUNTS_LIST_FILE_PATH_LABEL = "account_list_file_path";

	private final static String INPUT_MAPPING_PATH_LABEL = "input_mapping_file_path";
	private final static String LIST_INPUT_FILE_PATH_LABEL = "list_input_file_path";
	private final static String LIST_PROCESSED_FILE_PATH_LABEL = "list_processed_file_path";
	private final static String ERROR_FILE_PATH_LABEL = "error_file_path";

	private final static String OUTPUT_FILE_PATH_LABEL = "output_file_path";
	private final static String OUTPUT_TITLE_FILE_PATH_LABEL = "output_title_file_path";

	private final static String LINKS_LIST_FILE_PATH_LABEL = "links_list_file_path";
	private final static String ADD_LINK_FROM_FOLDER_FILES_LABEL = "add_links_from_folder_files";

	private final static String CONTENT_TEMPLATE_FILE_PATH_LABEL = "content_template_file_path";
	private final static String CONTENT_TEMPLATE_FILE_PATH_WO_PIC_LABEL = "content_template_file_path_wo_pic";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private final static String RANDOM_IMAGES_FILE_PATH="random_images_file_path";
	private final static String RANDOM_JPG_FILE_PATH="random_jpg_file_path";
	private final static String RANDOM_BUTTON_FILE_PATH="random_button_file_path";
	private final static String RANDOM_TITLE_FILE_PATH="random_title_file_path";

	private HashMap<String, HashSet<String>> usedKeys = new HashMap<String, HashSet<String>>();

	public JimboTaskRunner(){
		super();
	}
	
	@PostConstruct
	private void init(){
		this.accListFilePath = configManager.getProperty(ACCOUNTS_LIST_FILE_PATH_LABEL);

		this.inputMapppingFilePath = configManager.getProperty(INPUT_MAPPING_PATH_LABEL);
		this.listInputFilePath = configManager.getProperty(LIST_INPUT_FILE_PATH_LABEL);
		this.listProcessedFilePath = configManager.getProperty(LIST_PROCESSED_FILE_PATH_LABEL);
		this.errorFilePath = configManager.getProperty(ERROR_FILE_PATH_LABEL);

		this.templateFilePath = configManager.getProperty(CONTENT_TEMPLATE_FILE_PATH_LABEL);
		this.templateFilePathWOPic = configManager.getProperty(CONTENT_TEMPLATE_FILE_PATH_WO_PIC_LABEL);

		this.linksListFilePath = configManager.getProperty(LINKS_LIST_FILE_PATH_LABEL);
		
		String addLinkValue = configManager.getProperty(ADD_LINK_FROM_FOLDER_FILES_LABEL);
		if(addLinkValue != null && !"".equals(addLinkValue.trim())){
			addLinkFromFolder = Boolean.valueOf(addLinkValue);
		}

		this.outputFilePath = configManager.getProperty(OUTPUT_FILE_PATH_LABEL);
		this.outputTitleFilePath = configManager.getProperty(OUTPUT_TITLE_FILE_PATH_LABEL); 

		this.maxThreadCount = Integer.valueOf(configManager.getProperty(MAX_THREAD_COUNT_LABEL));

		String randImagesFilePathValue = configManager.getProperty(RANDOM_IMAGES_FILE_PATH);
		if(randImagesFilePathValue != null && !"".equals(randImagesFilePathValue.trim())){
			randImagesFilePath = new File(randImagesFilePathValue);
		}

		String randJpgFilePathValue = configManager.getProperty(RANDOM_JPG_FILE_PATH);
		if(randJpgFilePathValue != null && !"".equals(randJpgFilePathValue.trim())){
			randJpgFilePath = new File(randJpgFilePathValue);
		}

		String randButtonFilePathValue = configManager.getProperty(RANDOM_BUTTON_FILE_PATH);
		if(randButtonFilePathValue != null && !"".equals(randButtonFilePathValue.trim())){
			randButtonFilePath = new File(randButtonFilePathValue);
		}

		String randTitleFilePathValue = configManager.getProperty(RANDOM_TITLE_FILE_PATH);
		if(randTitleFilePathValue != null && !"".equals(randTitleFilePathValue.trim())){
			randTitleFilePath = new File(randTitleFilePathValue);
		}

		this.source = configManager.getProperty(SOURCE_LABEL);
		this.lang = configManager.getProperty(LANG_LABEL);

		String freqStr = configManager.getProperty(FREQUENCY_LABEL);
		if(freqStr != null && !"".equals(freqStr.trim())){
			String[] freqArray = freqStr.split(":");
			this.frequencies = new int[freqArray.length];
			for(int i = 0; i < freqArray.length; i++){
				this.frequencies[i] = Integer.parseInt(freqArray[i]);
			}
		}else{
			this.frequencies = new int[]{1};	
		}

		this.rowMapping = new RowMapping(new File(this.inputMapppingFilePath));

		this.taskFactory = TaskFactory.getInstance();
	}

	public static void main(String[] args) {
		
		try{
			DOMConfigurator.configure("log4j.xml");
			ApplicationContext ctx = SpringApplication.run(JimboTaskRunner.class, args);

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
			
			JimboTaskRunner taskRunner = ctx.getBean(JimboTaskRunner.class);
			
			taskRunner.runUploader();
			System.out.print("Program execution finished");
			System.exit(0);
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}


	public void runUploader() throws Exception{
		AccountFactory accountFactory = null;
		try{
			synchronized(this)
			{
				File rootInputFiles = new File(listInputFilePath);

				//load account list
				accountFactory = new AccountFactory(proxyFactory, configManager);
				accountFactory.fillAccounts(accListFilePath);

				TaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
				taskFactory = TaskFactory.getInstance();
				taskFactory.clear();
				//taskFactory.loadTaskQueue(urlsFilePath);
				taskFactory.fillTaskQueue(
						rootInputFiles.listFiles(), 
						this.rowMapping, 
						new File(this.templateFilePath), 
						new File(this.templateFilePathWOPic), 
						Utils.loadFileAsStrList(randImagesFilePath),
						Utils.loadFileAsStrList(randTitleFilePath),
						Utils.loadFileAsStrList(randJpgFilePath),
						Utils.loadFileAsStrList(randButtonFilePath)
						);

				File resLinkList = new File(outputFilePath);
				File resLinkTitleList = new File(outputTitleFilePath);

				//Copy account list file
				File accountFile = new File(accListFilePath);
				accountFile.renameTo(new File(accListFilePath + "_" + String.valueOf(System.currentTimeMillis())));

				usedKeys = loadUsedKeywords(new File ("./domen"));

				Account account = null;
				JimboPosterThread newThread = null;
				NewsTask task = null;

				ExecutorService executor = Executors.newFixedThreadPool(TaskFactory.getMAX_THREAD_COUNT());

				while( (  !taskFactory.isTaskFactoryEmpty() && ( (account = accountFactory.getAccount()) != null) ) || taskFactory.getRunThreadsCount() > 0)
				{
					if(taskFactory.getRunThreadsCount() < TaskFactory.getMAX_THREAD_COUNT() && account != null)
					{
						log.debug("Try to get request from RequestFactory queue.");
						log.debug("Account: " + account);
						task = taskFactory.getTask();

						if(task != null && (usedKeys.get(account.getSiteWOHttp()) == null || usedKeys.get(account.getSiteWOHttp()) != null && !usedKeys.get(account.getSiteWOHttp()).contains(task.getKey()))){
							log.info("Current thread count: " + taskFactory.getRunThreadsCount());
							log.info("Task retrieved. File name: " + task.getInputFile().getName());
							log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Error tasks: " + taskFactory.getErrorQueue().size());
							newThread = new JimboPosterThread(
									task, 
									account, 
									taskFactory, 
									proxyFactory, 
									accountFactory,
									resLinkList, 
									resLinkTitleList, 
									this.listProcessedFilePath, 
									this.errorFilePath,
									(ArrayList<String>)Utils.loadFileAsStrList(linksListFilePath),
									addLinkFromFolder,
									lang,
									source,
									frequencies
									);

							executor.submit(newThread);

							account = null;
							newThread = null;
							task = null;
							wait(1L);
							continue;
						}else{
							if(task != null){
								taskFactory.reprocessingTask(task);
							}
							accountFactory.releaseAccount(account);
						}
						account = null;
						newThread = null;
						task = null;

					}else{
						if(account != null){
							accountFactory.releaseAccount(account);
						}
						try {
							log.debug("Waiting when theads count will be decremented...");
							this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
						} catch (InterruptedException e) {
							log.error("InterruptedException occured during RequestRunner process",e);
						}
					}
				}

				
				executor.shutdown();
				executor.awaitTermination(300, TimeUnit.SECONDS);
				//saver.running = false;

				log.info("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.getRunThreadsCount());
				log.info("Error tasks: " + taskFactory.getErrorQueue().size());
			}
		}finally{
			try{
				if(accountFactory != null){
					saveUnusedAccounts(accountFactory.getAccounts());
				}
			}catch(Throwable e){
				log.error("Some error occured", e);
			}

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

	private void saveUnusedAccounts(HashMap<String, Account> accounts){
		BufferedWriter bufferedWriter = null;

		try {
			log.info("Starting saving unused account...");
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(accListFilePath,false));
			for(Account account : accounts.values()){
				bufferedWriter.write(account.toString());
				bufferedWriter.newLine();
			}
			log.info("Unused accounts was saved successfully.");

		} catch (FileNotFoundException ex) {
			log.error("Error occured during saving sucess result",ex);
		} catch (IOException ex) {
			log.error("Error occured during saving sucess result",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error occured during closing output streams during saving success results",ex);
			}
		}
	}

	private HashMap<String, HashSet<String>> loadUsedKeywords(File domenDir)
	{
		HashMap<String, HashSet<String>> usedKeys = new HashMap<String, HashSet<String>>();

		for(File file : domenDir.listFiles()){
			List<String> keysList = Utils.loadFileAsStrList(file);
			usedKeys.put(file.getName().substring(0, file.getName().length()-4), new HashSet<String>(keysList));
		}

		return usedKeys;
	}

	public void loadProperties(String cfgFilePath){
		synchronized (this){ 
			InputStream is = null;
			try {
				is = new FileInputStream(new File(cfgFilePath));
				config.load(is);
			} catch (FileNotFoundException e) {
				log.error("Reading PROPERTIES file: FileNotFoundException exception occured: " + e.getMessage());
			} catch (IOException e) {
				log.error("Reading PROPERTIES file: IOException exception occured: " + e.getMessage());
			} finally {
				try {
					is.close();
				} catch (Throwable e) {
					log.warn("Error while initializtion", e);
				}
			}
		}
	}
}
