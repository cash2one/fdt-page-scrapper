package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.dailymotion.task.NewsTask;
import com.fdt.dailymotion.task.TaskFactory;
import com.fdt.scrapper.proxy.ProxyFactory;

/**
 * @author VarenKoks
 */
public class VideoTaskRunner {
	private static final Logger log = Logger.getLogger(VideoTaskRunner.class);

	private static final String LINE_FEED = "\r\n";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	public final static String MAIN_URL_LABEL = "main_url";

	private String proxyFilePath;
	private String accListFilePath;
	private long proxyDelay;

	private boolean addAudioToFile;

	private String listInputFilePath;
	private String listProcessedFilePath;
	private String errorFilePath;

	private int maxThreadCount;

	private String templateFilePath;

	private String linkListFilePath;
	private String linkTitleListFilePath;
	
	private TaskFactory taskFactory;

	private final Random rnd = new Random();

	private Properties config = new Properties();

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String ACCOUNTS_LIST_FILE_PATH_LABEL = "account_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String ADD_AUDIO_TO_VIDEO_FILE_LABEL = "add_audio_to_video_file";

	private final static String LIST_INPUT_FILE_PATH_LABEL = "list_input_file_path";
	private final static String LIST_PROCESSED_FILE_PATH_LABEL = "list_processed_file_path";
	private final static String ERROR_FILE_PATH_LABEL = "error_file_path";

	private final static String LINK_LIST_FILE_PATH_LABEL = "link_list_file_path";
	private final static String LINK_TITLE_LIST_FILE_PATH_LABEL = "link_title_list_file_path";

	private final static String CONTENT_TEMPLATE_FILE_PATH_LABEL = "content_template_file_path";

	private static final String MAX_POST_PER_ACCOUNT_LABEL = "MAX_POST_PER_ACCOUNT";
	private static final String MIN_POST_PER_ACCOUNTLABEL = "MIN_POST_PER_ACCOUNT";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private Integer MIN_SNIPPET_COUNT=5;
	private Integer MAX_SNIPPET_COUNT=10;

	public VideoTaskRunner(String cfgFilePath){

		Constants.getInstance().loadProperties(cfgFilePath);

		this.proxyFilePath = Constants.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.accListFilePath = Constants.getInstance().getProperty(ACCOUNTS_LIST_FILE_PATH_LABEL);
		this.proxyDelay = Integer.valueOf(Constants.getInstance().getProperty(PROXY_DELAY_LABEL));
		this.addAudioToFile = Boolean.valueOf(Constants.getInstance().getProperty(ADD_AUDIO_TO_VIDEO_FILE_LABEL));

		this.listInputFilePath = Constants.getInstance().getProperty(LIST_INPUT_FILE_PATH_LABEL);
		this.listProcessedFilePath = Constants.getInstance().getProperty(LIST_PROCESSED_FILE_PATH_LABEL);
		this.errorFilePath = Constants.getInstance().getProperty(ERROR_FILE_PATH_LABEL);

		this.templateFilePath = Constants.getInstance().getProperty(CONTENT_TEMPLATE_FILE_PATH_LABEL);

		this.linkListFilePath = Constants.getInstance().getProperty(LINK_LIST_FILE_PATH_LABEL);
		this.linkTitleListFilePath = Constants.getInstance().getProperty(LINK_TITLE_LIST_FILE_PATH_LABEL); 

		this.maxThreadCount = Integer.valueOf(Constants.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));

		this.taskFactory = TaskFactory.getInstance();

		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						Constants.getInstance().getProperty(PROXY_LOGIN_LABEL), Constants.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
						);
			}
		});
	}

	public static void main(String[] args) {
		try{
			VideoTaskRunner taskRunner = new VideoTaskRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
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
			synchronized(this){
				File rootInputFiles = new File(listInputFilePath);

				ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
				ProxyFactory proxyFactory = ProxyFactory.getInstance();
				proxyFactory.init(proxyFilePath);

				//load account list
				accountFactory = new AccountFactory(proxyFactory);
				accountFactory.fillAccounts(accListFilePath);

				TaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
				taskFactory = TaskFactory.getInstance();
				taskFactory.clear();
				//taskFactory.loadTaskQueue(urlsFilePath);
				taskFactory.fillTaskQueue(rootInputFiles.listFiles(), new File(this.templateFilePath));

				File linkList = new File(linkListFilePath);
				File linkTitleList = new File(linkTitleListFilePath);

				File templateFile = new File(templateFilePath);

				//TODO Copy account list file
				File accountFile = new File(accListFilePath);
				accountFile.renameTo(new File(accListFilePath + "_" + String.valueOf(System.currentTimeMillis())));

				Account account = null;
				VideoPosterThread newThread = null;
				
				while((!taskFactory.isTaskFactoryEmpty() && ((account = accountFactory.getAccount()) != null)) || taskFactory.getRunThreadsCount() > 0){

					log.debug("Try to get request from RequestFactory queue.");
					log.debug("Account: " + account);
					if(account != null){
						NewsTask task = taskFactory.getTask();
						log.debug("Task: " + task);
						if(task != null){
							log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Error tasks: " + taskFactory.getErrorQueue().size());
							newThread = new VideoPosterThread(
									task, 
									account, 
									taskFactory, 
									proxyFactory, 
									accountFactory,
									this.addAudioToFile, 
									linkList, 
									linkTitleList, 
									this.listProcessedFilePath, 
									this.errorFilePath
								);
							newThread.start();
							continue;
						}else{
							accountFactory.releaseAccount(account);
						}
					}
					try {
						this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
					} catch (InterruptedException e) {
						log.error("InterruptedException occured during RequestRunner process",e);
					}
				}

				//saver.running = false;

				log.debug("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.getRunThreadsCount());
				log.debug("Error tasks: " + taskFactory.getErrorQueue().size());
			}
		}finally{
			try{
				//Save unused account if they was not used
				if(accountFactory != null){
					saveUnusedAccounts(accountFactory.getAccounts());
				}
				deleteAllVideoFiles();
			}catch(Throwable e){
				log.error(e);
			}
		}
	}

	private void deleteAllVideoFiles(){
		File outputFolder = new File("output_video");
		for(File file: outputFolder.listFiles()){
			try {
				log.debug("Delete video file: " + file.getName());
				file.delete();
			}
			catch (Exception e1) {
				log.error(e1);
			}
		}
	}

	private void saveUnusedAccounts(HashMap<String, Account> accounts){
		BufferedWriter bufferedWriter = null;

		try {
			log.debug("Starting saving unused account...");
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(accListFilePath,false));
			for(Account account : accounts.values()){
				bufferedWriter.write(account.toString());
				bufferedWriter.newLine();
			}
			log.debug("Unused accounts was saved successfully.");

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

	public synchronized ArrayList<String> loadLinkList(String cfgFilePath){
		ArrayList<String> linkList = new ArrayList<String>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(new File(cfgFilePath));
			br = new BufferedReader(fr);

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
			try {
				if(fr != null)
					fr.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
		return linkList;
	}
}
