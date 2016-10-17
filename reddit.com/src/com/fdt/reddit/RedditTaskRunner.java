package com.fdt.reddit;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.filemapping.RowMapping;
import com.fdt.reddit.task.RedditTask;
import com.fdt.reddit.task.TaskFactory;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

/**
 * @author VarenKoks
 */
public class RedditTaskRunner 
{
	private static final Logger log = Logger.getLogger(RedditTaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	public final static String MAIN_URL_LABEL = "main_url";

	private String proxyFilePath;
	private String accListFilePath;
	private long proxyDelay;
	private String proxyType;

	private String inputLinksFilePath;
	private String listProcessedFilePath;
	private String errorFilePath;

	private int maxThreadCount;

	private String outputFilePath;
	private String outputTitleFilePath;

	private String linksListFilePath;

	private TaskFactory taskFactory;

	private String lang = null;
	private String source = null;
	private int[] frequencies = null;

	private Properties config = new Properties();

	private static final String LANG_LABEL = "lang";
	private static final String SOURCE_LABEL = "source";
	private static final String FREQUENCY_LABEL = "frequency";

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String ACCOUNTS_LIST_FILE_PATH_LABEL = "account_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String PROXY_TYPE_LABEL = "proxy_type";

	private final static String INPUT_LINKS_FILE_PATH_LABEL = "input_links_file_path";
	private final static String LIST_PROCESSED_FILE_PATH_LABEL = "list_processed_file_path";
	private final static String ERROR_FILE_PATH_LABEL = "error_file_path";

	private final static String OUTPUT_FILE_PATH_LABEL = "output_file_path";
	private final static String OUTPUT_TITLE_FILE_PATH_LABEL = "output_title_file_path";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private final static String RANDOM_IMAGES_FILE_PATH="random_images_file_path";
	private final static String RANDOM_JPG_FILE_PATH="random_jpg_file_path";
	private final static String RANDOM_BUTTON_FILE_PATH="random_button_file_path";
	private final static String RANDOM_TITLE_FILE_PATH="random_title_file_path";

	private List<String> usedLinsk = new ArrayList<String>();

	public RedditTaskRunner(String cfgFilePath){

		Config.getInstance().loadProperties(cfgFilePath);
		ConfigManager.getInstance().loadProperties(cfgFilePath);

		this.proxyFilePath = Config.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.accListFilePath = Config.getInstance().getProperty(ACCOUNTS_LIST_FILE_PATH_LABEL);
		this.proxyDelay = 0;
		//this.proxyDelay = Integer.valueOf(Config.getInstance().getProperty(PROXY_DELAY_LABEL));
		this.proxyType = Config.getInstance().getProperty(PROXY_TYPE_LABEL);

		this.inputLinksFilePath = Config.getInstance().getProperty(INPUT_LINKS_FILE_PATH_LABEL);
		this.listProcessedFilePath = Config.getInstance().getProperty(LIST_PROCESSED_FILE_PATH_LABEL);
		this.errorFilePath = Config.getInstance().getProperty(ERROR_FILE_PATH_LABEL);

		this.outputFilePath = Config.getInstance().getProperty(OUTPUT_FILE_PATH_LABEL);
		this.outputTitleFilePath = Config.getInstance().getProperty(OUTPUT_TITLE_FILE_PATH_LABEL); 

		this.maxThreadCount = Integer.valueOf(Config.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));

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

		this.taskFactory = TaskFactory.getInstance();

		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						Config.getInstance().getProperty(PROXY_LOGIN_LABEL), Config.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
						);
			}
		});
	}

	public static void main(String[] args) {
		try{
			RedditTaskRunner taskRunner = new RedditTaskRunner("config.ini");
			DOMConfigurator.configure("./log4j.xml");
			taskRunner.runUploader();
			System.out.print("Program execution finished");
			System.exit(0);
		}catch(Throwable e){
			e.printStackTrace();
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}


	public void runUploader() throws Exception{
		AccountFactory accountFactory = null;
		try{
			synchronized(this)
			{
				ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
				ProxyFactory.PROXY_TYPE = proxyType;
				ProxyFactory proxyFactory = ProxyFactory.getInstance();
				proxyFactory.init(proxyFilePath);

				//load account list
				accountFactory = new AccountFactory();
				accountFactory.fillAccounts(accListFilePath);

				TaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
				taskFactory = TaskFactory.getInstance();
				taskFactory.clear();
				//taskFactory.loadTaskQueue(urlsFilePath);
				taskFactory.fillTaskQueue(Utils.loadFileAsStrList(inputLinksFilePath));

				File resLinkList = new File(outputFilePath);
				File resLinkTitleList = new File(outputTitleFilePath);

				//Copy account list file
				File accountFile = new File(accListFilePath);
				accountFile.renameTo(new File(accListFilePath + "_" + String.valueOf(System.currentTimeMillis())));

				usedLinsk = loadUsedLinks(new File ("./links_processed.txt"));
				
				

				Account account = null;
				RedditPosterThread newThread = null;
				RedditTask task = null;

				ExecutorService executor = Executors.newFixedThreadPool(TaskFactory.getMAX_THREAD_COUNT());

				while( (  !taskFactory.isTaskFactoryEmpty() && ( (account = accountFactory.getAccount()) != null) ) || taskFactory.getRunThreadsCount() > 0)
				{
					if(taskFactory.getRunThreadsCount() < TaskFactory.getMAX_THREAD_COUNT() && account != null)
					{
						log.debug("Try to get request from RequestFactory queue.");
						log.debug("Account: " + account);
						task = taskFactory.getTask();

						if(task != null && !usedLinsk.contains(task.getLink())){
							log.info("Current thread count: " + taskFactory.getRunThreadsCount());
							log.info("Task retrieved. Link value: " + task.getLink());
							log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Error tasks: " + taskFactory.getErrorQueue().size());
							newThread = new RedditPosterThread(
									task, 
									account, 
									taskFactory, 
									proxyFactory, 
									accountFactory,
									resLinkList, 
									resLinkTitleList, 
									this.listProcessedFilePath, 
									this.errorFilePath,
									new ArrayList<String>(),
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
				executor.awaitTermination(60, TimeUnit.SECONDS);
				//saver.running = false;

				log.info("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.getRunThreadsCount());
				log.info("Error tasks: " + taskFactory.getErrorQueue().size());
			}
		}finally{
			//TODO Uncomment
			/*try{
				if(accountFactory != null){
					saveUnusedAccounts(accountFactory.getAccounts());
				}
			}catch(Throwable e){
				log.error("Some error occured", e);
			}*/

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

	private List<String> loadUsedLinks(File processedLinksFile)
	{
		List<String> usedLinks = new ArrayList<String>();
		
		List<String> processed = Utils.loadFileAsStrList(processedLinksFile);

		for(String str: processed){
			if(str != null && !"".equals(str.trim())){
				usedLinks.add(str.split(";")[0]);
			}
		}

		return usedLinks;
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
