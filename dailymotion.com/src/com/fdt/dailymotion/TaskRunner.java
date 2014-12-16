package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.media.MediaLocator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.dailymotion.task.NewsTask;
import com.fdt.dailymotion.util.AudioVideoMerger;
import com.fdt.dailymotion.util.JpegImagesToMovie;
import com.fdt.dailymotion.util.VideoCreator;
import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.BingSnippetTask;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;

/**
 * @author VarenKoks
 */
public class TaskRunner {
	private static final Logger log = Logger.getLogger(TaskRunner.class);

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

	private String templateFilePath;

	private String linkListFilePath;
	private String linkTitleListFilePath;

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

	private static final String MAX_SNIPPET_COUNT_LABEL = "MAX_SNIPPET_COUNT";
	private static final String MIN_SNIPPET_COUNT_LABEL = "MIN_SNIPPET_COUNT";

	private static final String MAX_POST_PER_ACCOUNT_LABEL = "MAX_POST_PER_ACCOUNT";
	private static final String MIN_POST_PER_ACCOUNTLABEL = "MIN_POST_PER_ACCOUNT";

	private Integer MIN_SNIPPET_COUNT=5;
	private Integer MAX_SNIPPET_COUNT=10;

	private Integer MIN_POST_PER_ACCOUNT=5;
	private Integer MAX_POST_PER_ACCOUNT=10;

	public TaskRunner(String cfgFilePath){

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

		if(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL) != null)
			MIN_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL));
		if(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL) != null)
			MAX_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL));

		if(ConfigManager.getInstance().getProperty(MAX_POST_PER_ACCOUNT_LABEL) != null)
			MIN_POST_PER_ACCOUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_POST_PER_ACCOUNT_LABEL));
		if(ConfigManager.getInstance().getProperty(MIN_POST_PER_ACCOUNTLABEL) != null)
			MAX_POST_PER_ACCOUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_POST_PER_ACCOUNTLABEL));

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
			TaskRunner taskRunner = new TaskRunner("config.ini");
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
		ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
		ProxyFactory proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(proxyFilePath);

		//load account list
		AccountFactory accountFactory = new AccountFactory(proxyFactory);
		accountFactory.fillAccounts(accListFilePath);

		File rootInputFiles = new File(listInputFilePath);

		File linkList = new File(linkListFilePath);
		File linkTitleList = new File(linkTitleListFilePath);

		File templateFile = new File(templateFilePath);

		int postPerAccount = MIN_POST_PER_ACCOUNT + rnd.nextInt(MAX_POST_PER_ACCOUNT - MIN_POST_PER_ACCOUNT+1);
		int postedNewPerAccount = 0;

		for(File file : rootInputFiles.listFiles())
		{
			if(file.isFile())
			{
				if(postedNewPerAccount < postPerAccount && accountFactory.getAccounts().size() > 0)
				{
					NewsTask task = null;
					try {
						postedNewPerAccount++;
						task = new NewsTask(file, templateFile);
						SnippetExtractor snippetExtractor = new SnippetExtractor(null, proxyFactory, null);

						//create video
						createVideo(task, this.addAudioToFile);

						//TODO Add Snippet task chooser
						ArrayList<Snippet> snippets = snippetExtractor.extractSnippetsFromPageContent(new BingSnippetTask(task.getKey()));
						if(snippets.size() == 0)
							throw new Exception("Could not extract snippets");

						//get random snippets
						snippets = getRandSnippets(snippets, snippetExtractor);

						StringBuilder snippetsStr = new StringBuilder(); 
						for(Snippet snippet : snippets){
							snippetsStr.append(LINE_FEED).append(LINE_FEED).append(snippet.toString());
						}
						task.setSnippets(snippetsStr.toString());

						NewsPoster nPoster = new NewsPoster(task, proxyFactory.getProxyConnector().getConnect(), accountFactory.getAccounts().get(0));
						String linkToVideo = nPoster.executePostNews();
						appendStringToFile(linkToVideo, linkList);
						appendStringToFile(linkToVideo + ";" + task.getVideoTitle(), linkTitleList);

						//Move file to processed folder
						File destFile = new File(listProcessedFilePath + "/" + task.getInputFileName().getName());
						if(destFile.exists()){
							destFile.delete();
						}
						FileUtils.moveFile(task.getInputFileName(), destFile);
					}  catch (Throwable e) {
						try {
							File destFile = new File(errorFilePath + "/" + file.getName());
							if(destFile.exists()){
								destFile.delete();
							}
							FileUtils.moveFile(file, destFile);
						} catch (IOException e1) {
							log.error(e1);
						}
						log.error("Error during execution: ", e);
					}
					finally{
						deleteVideoFile(task);
					}
				}else{
					if(accountFactory.getAccounts().size() > 0){
						accountFactory.getAccounts().remove(0);
						postedNewPerAccount = 0;
					}else{
						break;
					}
				}
			}
		}

		deleteAllVideoFiles();

		//TODO Copy account list file
		File accountFile = new File(accListFilePath);
		accountFile.renameTo(new File(accListFilePath + "_" + String.valueOf(System.currentTimeMillis())));

		//Save unused account if they was not used
		saveUnusedAccounts(accountFactory.getAccounts());
	}

	private void deleteVideoFile(NewsTask task){
		if(task != null ){
			if(task.getVideoFile() != null){
				try {
					log.debug("Delete video file: " + task.getVideoFile().getName());
					task.getVideoFile().delete();
				} catch (Exception e1) {
					log.error(e1);
				}
			}

			if(task.getVideoFileWOAudio() != null){
				try {
					log.debug("Delete video file: " + task.getVideoFileWOAudio().getName());
					task.getVideoFileWOAudio().delete();
				} catch (Exception e1) {
					log.error(e1);
				}
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

	private ArrayList<Snippet> getRandSnippets(List<Snippet> snippets, SnippetExtractor snpExtr){
		ArrayList<Snippet> rndSnipList = new ArrayList<Snippet>();

		//calculate snippets count
		int snipCount = 0;

		if(snippets.size() <= MIN_SNIPPET_COUNT){
			snipCount = snippets.size();
		}else{
			int randomValue = snpExtr.getRandomValue(MIN_SNIPPET_COUNT, MAX_SNIPPET_COUNT);
			if(randomValue <= snippets.size()){
				snipCount = randomValue;
			}else{
				snipCount = snippets.size();
			}
		}

		log.debug("Keywords: task.getKeyWords(). Snippet count: " + snipCount);

		int indexShift = snpExtr.getRandomValue(0,snippets.size()-snipCount); 

		for(int i = indexShift; i < (snipCount+indexShift); i++){
			rndSnipList.add(snippets.get(i));	
		}

		return rndSnipList;
	}

	private void createVideo(NewsTask task, boolean addAudioToFile) throws Exception{
		AudioVideoMerger avMerger = new AudioVideoMerger();

		VideoCreator.makeVideo(task.getVideoFileWOAudio().getPath(), task.getImageFile());

		if(addAudioToFile){
			MediaLocator ivml = JpegImagesToMovie.createMediaLocator(task.getVideoFileWOAudio().getPath());
			MediaLocator iaml = JpegImagesToMovie.createMediaLocator("08.wav");
			MediaLocator ovml = JpegImagesToMovie.createMediaLocator(task.getVideoFile().getPath());

			avMerger.mergeFiles(ivml, iaml, ovml);

			avMerger = null;
			ivml = null;
			iaml = null;
			ovml = null;
		}else{
			task.setVideoFile(task.getVideoFileWOAudio());
		}
	}

	private void saveUnusedAccounts(List<Account> accounts){
		BufferedWriter bufferedWriter = null;

		try {
			log.debug("Starting saving unused account...");
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(accListFilePath,false));
			for(Account account : accounts){
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

	private void appendStringToFile(String str, File file) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();
		} catch (FileNotFoundException ex) {
			log.error("Error during saving string to file",ex);
		} catch (IOException ex) {
			log.error("Error during saving string to file",ex);
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
}
