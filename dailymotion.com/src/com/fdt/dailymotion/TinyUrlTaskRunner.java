package com.fdt.dailymotion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.dailymotion.task.TinyUrlTask;
import com.fdt.dailymotion.task.TinyUrlTaskFactory;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;

/**
 * @author VarenKoks
 */
public class TinyUrlTaskRunner {
	private static final Logger log = Logger.getLogger(TinyUrlTaskRunner.class);

	private static final String LINE_FEED = "\r\n";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	public final static String MAIN_URL_LABEL = "main_url";

	private String proxyFilePath;
	private long proxyDelay;

	private String listInputFilePath;
	private String listProcessedFilePath;
	private String errorFilePath;

	private int maxThreadCount;

	private Properties config = new Properties();

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";

	private final static String MAX_THREAD_TINY_URL_COUNT_LABEL = "max_thread_tiny_url_count";

	private final static String TINYURL_LIST_INPUT_FILE_PATH_LABEL = "tinyurl_list_input_file_path";
	private final static String TINYURL_LIST_PROCESSED_FILE_PATH_LABEL = "tinyurl_list_processed_file_path";
	private final static String TINYURL_ERROR_FILE_PATH_LABEL = "tinyurl_error_file_path";

	private final static String TINYURL_DOWNLOAD_URL_LABEL = "tinyurl_tinyurl_download_url";

	private TinyUrlTaskFactory taskFactory;

	public TinyUrlTaskRunner(String cfgFilePath){

		Constants.getInstance().loadProperties(cfgFilePath);

		this.proxyFilePath = Constants.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.proxyDelay = Integer.valueOf(Constants.getInstance().getProperty(PROXY_DELAY_LABEL));

		this.listInputFilePath = Constants.getInstance().getProperty(TINYURL_LIST_INPUT_FILE_PATH_LABEL);
		this.listProcessedFilePath = Constants.getInstance().getProperty(TINYURL_LIST_PROCESSED_FILE_PATH_LABEL);
		this.errorFilePath = Constants.getInstance().getProperty(TINYURL_ERROR_FILE_PATH_LABEL);

		this.taskFactory = TinyUrlTaskFactory.getInstance();

		this.maxThreadCount = Integer.valueOf(Constants.getInstance().getProperty(MAX_THREAD_TINY_URL_COUNT_LABEL));

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
			TinyUrlTaskRunner taskRunner = new TinyUrlTaskRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
			taskRunner.runTinyUrlReplacer();
			System.out.print("Program execution finished");
			System.exit(0);
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}


	public void runTinyUrlReplacer() throws Exception{

		File rootInputFiles = new File(listInputFilePath);

		ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
		ProxyFactory proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(proxyFilePath);

		TinyUrlTaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
		taskFactory = TinyUrlTaskFactory.getInstance();
		taskFactory.clear();
		//taskFactory.loadTaskQueue(urlsFilePath);
		taskFactory.fillTaskQueue(rootInputFiles.listFiles());

		TinyUrlThread newThread = null;
		log.debug("Total tasks: "+taskFactory.getTaskQueue().size());

		//TaskFactory.setMAX_THREAD_COUNT(1);
		while( !taskFactory.isTaskFactoryEmpty() || taskFactory.getRunThreadsCount() > 0){
			log.debug("Try to get request from RequestFactory queue.");

			TinyUrlTask task = taskFactory.getTask();
			log.debug("Task: " + task);
			if(task != null){
				log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Error tasks: " + taskFactory.getErrorQueue().size());
				newThread = new TinyUrlThread(task, taskFactory, proxyFactory, listProcessedFilePath);
				newThread.start();
				continue;
			}
			try {
				this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
			} catch (InterruptedException e) {
				log.error("InterruptedException occured during RequestRunner process",e);
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
}
