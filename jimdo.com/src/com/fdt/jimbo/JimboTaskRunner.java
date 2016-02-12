package com.fdt.jimbo;

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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.filemapping.RowMapping;
import com.fdt.jimbo.task.NewsTask;
import com.fdt.jimbo.task.TaskFactory;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

/**
 * @author VarenKoks
 */
public class JimboTaskRunner 
{
	private static final Logger log = Logger.getLogger(JimboTaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 5000L;

	public final static String MAIN_URL_LABEL = "main_url";

	private String proxyFilePath;
	private String accListFilePath;
	private long proxyDelay;
	private String proxyType;

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

	private TaskFactory taskFactory;
	
	private File randImagesFilePath = null;
	private File randJpgFilePath = null;
	private File randButtonFilePath = null;
	private File randTitleFilePath = null;
	
	private String lang = null;
	private String source = null;
	private int[] frequencies = null;
	
	private RowMapping rowMapping = null;
	
	private final Random rnd = new Random();

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

	private final static String INPUT_MAPPING_PATH_LABEL = "input_mapping_file_path";
	private final static String LIST_INPUT_FILE_PATH_LABEL = "list_input_file_path";
	private final static String LIST_PROCESSED_FILE_PATH_LABEL = "list_processed_file_path";
	private final static String ERROR_FILE_PATH_LABEL = "error_file_path";

	private final static String OUTPUT_FILE_PATH_LABEL = "output_file_path";
	private final static String OUTPUT_TITLE_FILE_PATH_LABEL = "output_title_file_path";
	
	private final static String LINKS_LIST_FILE_PATH_LABEL = "links_list_file_path";

	private final static String CONTENT_TEMPLATE_FILE_PATH_LABEL = "content_template_file_path";
	private final static String CONTENT_TEMPLATE_FILE_PATH_WO_PIC_LABEL = "content_template_file_path_wo_pic";
	
	private final static String SHORT_URLS_LIST_LABEL = "short_urls_list";

	private static final String MAX_POST_PER_ACCOUNT_LABEL = "MAX_POST_PER_ACCOUNT";
	private static final String MIN_POST_PER_ACCOUNT_LABEL = "MIN_POST_PER_ACCOUNT";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	
	private final static String RANDOM_IMAGES_FILE_PATH="random_images_file_path";
	private final static String RANDOM_JPG_FILE_PATH="random_jpg_file_path";
	private final static String RANDOM_BUTTON_FILE_PATH="random_button_file_path";
	private final static String RANDOM_TITLE_FILE_PATH="random_title_file_path";
	
	public JimboTaskRunner(String cfgFilePath){

		Config.getInstance().loadProperties(cfgFilePath);
		ConfigManager.getInstance().loadProperties(cfgFilePath);

		this.proxyFilePath = Config.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.accListFilePath = Config.getInstance().getProperty(ACCOUNTS_LIST_FILE_PATH_LABEL);
		this.proxyDelay = Integer.valueOf(Config.getInstance().getProperty(PROXY_DELAY_LABEL));
		this.proxyType = Config.getInstance().getProperty(PROXY_TYPE_LABEL);

		this.inputMapppingFilePath = Config.getInstance().getProperty(INPUT_MAPPING_PATH_LABEL);
		this.listInputFilePath = Config.getInstance().getProperty(LIST_INPUT_FILE_PATH_LABEL);
		this.listProcessedFilePath = Config.getInstance().getProperty(LIST_PROCESSED_FILE_PATH_LABEL);
		this.errorFilePath = Config.getInstance().getProperty(ERROR_FILE_PATH_LABEL);

		this.templateFilePath = Config.getInstance().getProperty(CONTENT_TEMPLATE_FILE_PATH_LABEL);
		this.templateFilePathWOPic = Config.getInstance().getProperty(CONTENT_TEMPLATE_FILE_PATH_WO_PIC_LABEL);
		
		this.linksListFilePath = ConfigManager.getInstance().getProperty(LINKS_LIST_FILE_PATH_LABEL);
		
		this.outputFilePath = Config.getInstance().getProperty(OUTPUT_FILE_PATH_LABEL);
		this.outputTitleFilePath = Config.getInstance().getProperty(OUTPUT_TITLE_FILE_PATH_LABEL); 

		this.maxThreadCount = Integer.valueOf(Config.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
		
		String randImagesFilePathValue = Config.getInstance().getProperty(RANDOM_IMAGES_FILE_PATH);
		if(randImagesFilePathValue != null && !"".equals(randImagesFilePathValue.trim())){
			randImagesFilePath = new File(randImagesFilePathValue);
		}
		
		String randJpgFilePathValue = Config.getInstance().getProperty(RANDOM_JPG_FILE_PATH);
		if(randJpgFilePathValue != null && !"".equals(randJpgFilePathValue.trim())){
			randJpgFilePath = new File(randJpgFilePathValue);
		}
		
		String randButtonFilePathValue = Config.getInstance().getProperty(RANDOM_BUTTON_FILE_PATH);
		if(randButtonFilePathValue != null && !"".equals(randButtonFilePathValue.trim())){
			randButtonFilePath = new File(randButtonFilePathValue);
		}
		
		String randTitleFilePathValue = Config.getInstance().getProperty(RANDOM_TITLE_FILE_PATH);
		if(randTitleFilePathValue != null && !"".equals(randTitleFilePathValue.trim())){
			randTitleFilePath = new File(randTitleFilePathValue);
		}
		
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
		
		this.rowMapping = new RowMapping(new File(this.inputMapppingFilePath));
		
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
			JimboTaskRunner taskRunner = new JimboTaskRunner("config.ini");
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
				
				RowMapping mapping = new RowMapping(new File(inputMapppingFilePath));

				ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
				ProxyFactory.PROXY_TYPE = proxyType;
				ProxyFactory proxyFactory = ProxyFactory.getInstance();
				proxyFactory.init(proxyFilePath);

				//load account list
				accountFactory = new AccountFactory(proxyFactory);
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
						Utils.loadFileAsStrList(randTitleFilePath)
						
				);
				
				File resLinkList = new File(outputFilePath);
				File resLinkTitleList = new File(outputTitleFilePath);

				//Change template file
				File templateFile = new File(templateFilePath);

				//TODO Copy account list file
				/*File accountFile = new File(accListFilePath);
				accountFile.renameTo(new File(accListFilePath + "_" + String.valueOf(System.currentTimeMillis())));
*/
				Account account = null;
				JimboPosterThread newThread = null;
				NewsTask task = null;
				
				while((!taskFactory.isTaskFactoryEmpty() && ((account = accountFactory.getAccount()) != null)) || taskFactory.getRunThreadsCount() > 0)
				{
					log.trace("Try to get request from RequestFactory queue.");
					log.trace("Account: " + account);
					if(account != null)
					{
						task = taskFactory.getTask();

						if(task != null){
							log.info("Task. File name: " + task.getInputFile().getName());
							log.trace("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Error tasks: " + taskFactory.getErrorQueue().size());
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
									lang,
									source,
									frequencies
							);
							newThread.start();
							account = null;
							newThread = null;
							task = null;
							continue;
						}else{
							accountFactory.releaseAccount(account);
						}
					}
					account = null;
					newThread = null;
					task = null;
					try {
						this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
					} catch (InterruptedException e) {
						log.error("InterruptedException occured during RequestRunner process",e);
					}
				}

				//saver.running = false;

				log.info("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.getRunThreadsCount());
				log.info("Error tasks: " + taskFactory.getErrorQueue().size());
			}
		}finally{
			try{
				//TODO Uncomment Save unused account if they was not used
				if(accountFactory != null){
					//saveUnusedAccounts(accountFactory.getAccounts());
				}
				deleteAllVideoFiles();
			}catch(Throwable e){
				log.error(e);
			}
		}
	}

	private void deleteAllVideoFiles(){
		File outputFolder = new File("output_video");
		//while(outputFolder.listFiles().length > 0){
			for(File file: outputFolder.listFiles()){
				try {
					log.info("Delete video file: " + file.getName());
					/*File newFile = new File(file.getPath() + "_delete_me");
					FileUtils.moveFile(file, newFile);*/
					FileUtils.forceDelete(file);
					//Thread.sleep(5000L);
					/*if(!deleted){
						log.warn(string.format("file %s was not deleted", newfile.getname()));
						while(!deleted){
							deleted = newfile.delete();
						}
					}*/
				}
				catch (Exception e) {
					log.error("Error occudred during file removing", e);
				}
			}
		//}
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
