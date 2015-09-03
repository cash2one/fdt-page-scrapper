package com.fdt.imgur;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.imgur.task.ImgurPromoTask;
import com.fdt.imgur.task.ImgurTask;
import com.fdt.imgur.task.ImgurTaskFactory;
import com.fdt.scrapper.proxy.ProxyFactory;

/**
 * @author VarenKoks
 */
public class ImgurTaskRunner {
	private static final Logger log = Logger.getLogger(ImgurTaskRunner.class);

	private static final String LINE_FEED = "\r\n";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	public final static String MAIN_URL_LABEL = "main_url";

	private String proxyFilePath;
	private long proxyDelay;
	
	private File promoFile;

	private String listInputFilePath;
	private String listProcessedFilePath;
	private String errorFilePath;

	private int maxThreadImgurCount;
	
	private int imgurPostPerProxy = 20;
	
	private Boolean loadFromPromo = true;
	private Boolean loadFromFolder = true;

	private Properties config = new Properties();

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	
	private final static String PROMO_FILE_PATH_LABEL = "imgur_promo_file_path";
	
	private final static String PROXY_TYPE_LABEL = "proxy_type";

	private final static String IMGUR_MAX_THREAD_COUNT_LABEL = "max_thread_imgur_count";
	
	private final static String IMRUG_POST_PER_PROXY_LABEL = "imgur_post_per_proxy";

	private final static String IMGUR_LIST_INPUT_FILE_PATH_LABEL = "imgur_list_input_file_path";
	private final static String IMGUR_LIST_PROCESSED_FILE_PATH_LABEL = "imgur_list_processed_file_path";
	private final static String IMGUR_ERROR_FILE_PATH_LABEL = "imgur_error_file_path";
	
	private final static String IMGUR_LOAD_FROM_FOLDER_LABEL = "imgur_load_from_folder";
	private final static String IMGUR_LOAD_FROM_PROMO_LABEL = "imgur_load_from_promo";

	private ImgurTaskFactory taskFactory;

	public ImgurTaskRunner(String cfgFilePath){

		Constants.getInstance().loadProperties(cfgFilePath);

		this.proxyFilePath = Constants.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.proxyDelay = Integer.valueOf(Constants.getInstance().getProperty(PROXY_DELAY_LABEL));
		
		this.promoFile = new File(Constants.getInstance().getProperty(PROMO_FILE_PATH_LABEL));
		
		this.listInputFilePath = Constants.getInstance().getProperty(IMGUR_LIST_INPUT_FILE_PATH_LABEL);
		this.listProcessedFilePath = Constants.getInstance().getProperty(IMGUR_LIST_PROCESSED_FILE_PATH_LABEL);
		this.errorFilePath = Constants.getInstance().getProperty(IMGUR_ERROR_FILE_PATH_LABEL);

		this.taskFactory = ImgurTaskFactory.getInstance();

		this.maxThreadImgurCount = Integer.valueOf(Constants.getInstance().getProperty(IMGUR_MAX_THREAD_COUNT_LABEL));
		
		this.imgurPostPerProxy = Integer.valueOf(Constants.getInstance().getProperty(IMRUG_POST_PER_PROXY_LABEL));
		
		this.loadFromPromo = Boolean.valueOf(Constants.getInstance().getProperty(IMGUR_LOAD_FROM_PROMO_LABEL));
		this.loadFromFolder= Boolean.valueOf(Constants.getInstance().getProperty(IMGUR_LOAD_FROM_FOLDER_LABEL));

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
			ImgurTaskRunner taskRunner = new ImgurTaskRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
			taskRunner.runImgurLoader();
			System.out.print("Program execution finished");
			System.exit(0);
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}


	public void runImgurLoader() throws Exception{

		synchronized(this){
			File rootInputFiles = new File(listInputFilePath);

			ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
			ProxyFactory proxyFactory = ProxyFactory.getInstance();
			proxyFactory.init(proxyFilePath);
			ProxyFactory.PROXY_TYPE = Constants.getInstance().getProperty(PROXY_TYPE_LABEL);

			ImgurTaskFactory.setMAX_THREAD_COUNT(maxThreadImgurCount);
			taskFactory = ImgurTaskFactory.getInstance();
			taskFactory.clear();
			//taskFactory.loadTaskQueue(urlsFilePath);
			
			if(loadFromFolder){
				taskFactory.fillTaskQueue(rootInputFiles.listFiles());
			}
			
			if(loadFromPromo){
				taskFactory.loadPromoFile(promoFile);
			}

			ImgurThread newThread = null;
			log.debug("Total tasks: "+taskFactory.getTaskQueue().size());

			//TaskFactory.setMAX_THREAD_COUNT(1);
			while( !taskFactory.isTaskFactoryEmpty() || taskFactory.getRunThreadsCount() > 0){
				log.debug("Try to get request from RequestFactory queue.");

				Object[] tasks = taskFactory.getTasks(imgurPostPerProxy);
				if(tasks != null){
					log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Error tasks: " + taskFactory.getErrorQueue().size());
					
					newThread = new ImgurThread((List<ImgurTask>)tasks[0], (List<ImgurPromoTask>)tasks[1], taskFactory, proxyFactory, listProcessedFilePath, errorFilePath);
					
					newThread.start();
					Thread.sleep(500L);
					continue;
				}
				try {
					this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
				} catch (InterruptedException e) {
					log.error("InterruptedException occured during RequestRunner process",e);
				}
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
