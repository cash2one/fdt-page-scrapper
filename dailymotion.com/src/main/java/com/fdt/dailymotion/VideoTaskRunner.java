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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fdt.dailymotion.task.NewsTask;
import com.fdt.dailymotion.task.TaskFactory;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;

/**
 * @author VarenKoks
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.fdt")
@PropertySource(value="file:${config.file}",ignoreResourceNotFound = true)
public class VideoTaskRunner {
	private static final Logger log = Logger.getLogger(VideoTaskRunner.class);

	private static final String LINE_FEED = "\r\n";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 5000L;

	public final static String MAIN_URL_LABEL = "main_url";
	
	@Autowired
	private ProxyFactory proxyFactory;
	
	@Autowired
	private ConfigManager cfgManager;

	@Value("${dailymotion.account_list_file_path}")
	private String accListFilePath;

	@Value("${dailymotion.add_audio_to_video_file}")
	private boolean addAudioToFile;
	@Value("${dailymotion.audio_folder_path}")
	private String audioFolderPath;
	
	@Value("${dailymotion.list_input_file_path}")
	private String listInputFilePath;
	@Value("${dailymotion.list_processed_file_path}")
	private String listProcessedFilePath;
	@Value("${dailymotion.error_file_path}")
	private String errorFilePath;

	@Value("${max_thread_count}")
	private int maxThreadCount;

	@Value("${dailymotion.content_template_file_path}")
	private String templateFilePath;
	
	@Value("${dailymotion.short_urls_list}")
	private String shortUrlsList;

	@Value("${dailymotion.link_list_file_path}")
	private String linkListFilePath;
	@Value("${dailymotion.link_title_list_file_path}")
	private String linkTitleListFilePath;

	private TaskFactory taskFactory;
	
	@Value("${dailymotion.load_pregenerated_file:false}")
	private Boolean loadPreGenFile;
	private File pregeneratedFile = null;
	private File titleFile = null;
	
	@Value("${dailymotion.is_use_image_from_link_only:true}")
	private boolean isUseImageFromLinkOnly;
	
	private File randImagesDirPath = null;
	
	@Value("${dailymotion.is_use_preview_generating?:false}")
	private boolean isUsePreview;
	
	private int cntOfPicUsing = 1;

	private final Random rnd = new Random();

	private Properties config = new Properties();
	
	@Value("${lang}")
	private String lang = null;
	@Value("${source}")
	private String source = null;
	private int[] frequencies = null;
	
	
	@Value("${dailymotion.title_tmpl_list_file_path}")
	private String titleTmplListFilePath = null;
	
	@Value("${dailymotion.before_link_tmpl_file_path}")
	private String beforeLinkTmplFilePath = null;
	
	@Value("${dailymotion.desc_tmpl_file_path}")
	private String descTmplListFilePath = null;
	

	private static final String FREQUENCY_LABEL = "frequency";	
	
	private static final String MAX_POST_PER_ACCOUNT_LABEL = "MAX_POST_PER_ACCOUNT";
	private static final String MIN_POST_PER_ACCOUNT_LABEL = "MIN_POST_PER_ACCOUNT";

	private final static String PREGENERATED_FILE_LABEL = "dailymotion.pregenerated_file";
	private final static String TITLE_FILE_LABEL = "dailymotion.title_file";
	
	private final static String RANDOM_IMAGES_DIR_PATH="dailymotion.random_images_directory_path";
	
	private final static String COUNT_OF_PICTURE_USING_MIN="dailymotion.count_of_picture_using_min";
	private final static String COUNT_OF_PICTURE_USING_MAX="dailymotion.count_of_picture_using_max";

	private Integer MIN_SNIPPET_COUNT=5;
	private Integer MAX_SNIPPET_COUNT=10;
	
	public VideoTaskRunner(){
		super();
	}

	@PostConstruct
	public void init (){

		String pregenFileValue = cfgManager.getProperty(PREGENERATED_FILE_LABEL);
		if(pregenFileValue != null && !"".equals(pregenFileValue.trim())){
			pregeneratedFile = new File(pregenFileValue);
		}
		
		String titleFileValue = cfgManager.getProperty(TITLE_FILE_LABEL);
		if(titleFileValue != null && !"".equals(titleFileValue.trim())){
			titleFile = new File(titleFileValue);
		}
		
		if(!isUseImageFromLinkOnly){
			int min = Integer.parseInt(cfgManager.getProperty(COUNT_OF_PICTURE_USING_MIN));
			int max = Integer.parseInt(cfgManager.getProperty(COUNT_OF_PICTURE_USING_MAX));
			cntOfPicUsing = min + rnd.nextInt(max-min);
		}
		
		String randImagesDirPathValue = cfgManager.getProperty(RANDOM_IMAGES_DIR_PATH);
		if(randImagesDirPathValue != null && !"".equals(randImagesDirPathValue.trim())){
			randImagesDirPath = new File(randImagesDirPathValue);
		}
		
		String freqStr = cfgManager.getProperty(FREQUENCY_LABEL);
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
		
		NewsTask.titleTmplListFilePath = this.titleTmplListFilePath;
		NewsTask.beforeLinkTmplFilePath = this.beforeLinkTmplFilePath;
		NewsTask.descTmplListFilePath = this.descTmplListFilePath;
	}

	public static void main(String[] args) {
		try{
			System.out.println(System.getProperty("app.home"));
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			ApplicationContext ctx = SpringApplication.run(VideoTaskRunner.class, args);

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
			
			DOMConfigurator.configure("log4j.xml");
			VideoTaskRunner taskRunner = ctx.getBean(VideoTaskRunner.class);
			
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

				//load account list
				accountFactory = new AccountFactory(proxyFactory);
				accountFactory.fillAccounts(accListFilePath);

				TaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
				taskFactory = TaskFactory.getInstance();
				taskFactory.clear();
				//taskFactory.loadTaskQueue(urlsFilePath);
				if(!loadPreGenFile){
					taskFactory.fillTaskQueue(rootInputFiles.listFiles(), new File(this.templateFilePath), this.shortUrlsList, isUseImageFromLinkOnly, randImagesDirPath.listFiles(), isUsePreview);
				}else{
					//TODO Load tasks from single file
					taskFactory.fillTaskQueue(titleFile, new File(this.templateFilePath), this.shortUrlsList, pregeneratedFile);
				}

				File linkList = new File(linkListFilePath);
				File linkTitleList = new File(linkTitleListFilePath);

				//TODO Copy account list file
				File accountFile = new File(accListFilePath);
				accountFile.renameTo(new File(accListFilePath + "_" + String.valueOf(System.currentTimeMillis())));

				Account account = null;
				VideoPosterThread newThread = null;
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
							newThread = new VideoPosterThread(
									task, 
									account, 
									taskFactory, 
									proxyFactory, 
									accountFactory,
									this.addAudioToFile, 
									this.audioFolderPath,
									linkList, 
									linkTitleList, 
									this.listProcessedFilePath, 
									this.errorFilePath,
									loadPreGenFile,
									cntOfPicUsing,
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
