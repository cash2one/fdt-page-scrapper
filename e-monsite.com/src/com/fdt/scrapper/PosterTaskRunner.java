package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.Constants;
import com.fdt.scrapper.task.NewsTask;

/**
 * @author VarenKoks
 */
public class PosterTaskRunner {
	private static final Logger log = Logger.getLogger(PosterTaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	private String proxyFilePath;
	private String keyWordsFilePath;
	private String linksListFilePath;
	private int maxThreadCount;
	private long proxyDelay;
	private String accListFilePath;



	private Properties config = new Properties();

	//private ArrayList<Thread> threads = new ArrayList<Thread>();

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String KEY_WORDS_FILE_PATH_LABEL = "key_words_file_path";
	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String LINKS_LIST_FILE_PATH_LABEL = "links_list_file_path";
	private final static String ACCOUNTS_LIST_FILE_PATH_LABEL = "account_list_file_path";


	private PostbitTaskFactory taskFactory;

	private PostbitSaverThread saver;

	private ArrayList<String> linksList = new ArrayList<String>();

	public PosterTaskRunner(String cfgFilePath){

		Constants.getInstance().loadProperties(cfgFilePath);
		taskFactory = PostbitTaskFactory.getInstance();
		this.proxyFilePath = Constants.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.keyWordsFilePath = Constants.getInstance().getProperty(KEY_WORDS_FILE_PATH_LABEL);
		this.maxThreadCount = Integer.valueOf(Constants.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
		this.proxyDelay = Integer.valueOf(Constants.getInstance().getProperty(PROXY_DELAY_LABEL));
		this.linksListFilePath = Constants.getInstance().getProperty(LINKS_LIST_FILE_PATH_LABEL);
		this.accListFilePath = Constants.getInstance().getProperty(ACCOUNTS_LIST_FILE_PATH_LABEL);

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
			PosterTaskRunner taskRunner = new PosterTaskRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
			taskRunner.run();
			System.out.print("Program execution finished successfully");
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}


	public void run(){
		try{
			synchronized (this) {
				PostbitTaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
				taskFactory = PostbitTaskFactory.getInstance();
				taskFactory.clear();
				//taskFactory.loadTaskQueue(urlsFilePath);
				taskFactory.loadTaskQueue(keyWordsFilePath);

				ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
				ProxyFactory proxyFactory = ProxyFactory.getInstance();
				proxyFactory.init(proxyFilePath);

				//run saver thread
				saver = new PostbitSaverThread(taskFactory, Constants.getInstance().getProperty(KEY_WORDS_FILE_PATH_LABEL));
				saver.start();

				//load links from file
				this.linksList= loadLinkList(linksListFilePath) ;

				//load account list
				AccountFactory accountFactory = new AccountFactory(proxyFactory);
				accountFactory.fillAccounts(accListFilePath);

				PosterThread newThread = null;
				log.debug("Total tasks: "+taskFactory.getTaskQueue().size());

				Account account = null;
				//TaskFactory.setMAX_THREAD_COUNT(1);
				while(!taskFactory.isTaskFactoryEmpty()){
					while((!taskFactory.isTaskFactoryEmpty() && ((account = accountFactory.getAccount()) != null)) || taskFactory.runThreadsCount > 0){
						log.debug("Try to get request from RequestFactory queue.");

						if(account != null){
							NewsTask task = taskFactory.getTask();
							log.debug("Task: " + task);
							if(task != null){
								//load new links
								this.linksList= loadLinkList(linksListFilePath) ;
								log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
								newThread = new PosterThread(task, taskFactory, proxyFactory, account, accountFactory, linksList);
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
					//reset account number
					accountFactory.resetPostedCount();
					taskFactory.resetTaskFactory();
				}
				saver.running = false;

				log.debug("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.runThreadsCount);
				log.debug("Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
			}
		}finally{
			try{
				saveKeysToFile();
			}catch(Throwable e){
				log.error(e);
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

	private void saveKeysToFile() {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(keyWordsFilePath)), "UTF8"));
			for (NewsTask task : taskFactory.getTaskQueue()) {
				bufferedWriter.write(task.getKeyWords());
				bufferedWriter.newLine();
			}
			for (NewsTask task : taskFactory.getErrorQueue()) {
				bufferedWriter.write(task.getKeyWords());
				bufferedWriter.newLine();
			}
		} catch (FileNotFoundException ex) {
			log.error("Error during saving not used keys to file",ex);
		} catch (IOException ex) {
			log.error("Error during saving not used keys to file",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error during closing output stream",ex);
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
