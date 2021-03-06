package com.fdt.dailymotion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.fdt.dailymotion.task.NewsTask;
import com.fdt.dailymotion.task.TaskFactory;
import com.fdt.dailymotion.util.ComplexVideoGenerator;
import com.fdt.dailymotion.util.VideoCreator;
import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTaskWrapper;

public class VideoPosterThread extends Thread{

	private static final String LINE_FEED = "\r\n";

	private static final Logger log = Logger.getLogger(VideoPosterThread.class);

	private static final String MAX_SNIPPET_COUNT_LABEL = "MAX_SNIPPET_COUNT";
	private static final String MIN_SNIPPET_COUNT_LABEL = "MIN_SNIPPET_COUNT";

	private Integer MIN_SNIPPET_COUNT=5;
	private Integer MAX_SNIPPET_COUNT=10;

	private static final String MIN_DURATION_VIDEO_LABEL = "MIN_DURATION_VIDEO";
	private static final String MAX_DURATION_VIDEO_LABEL = "MAX_DURATION_VIDEO";

	private Integer MIN_DURATION_VIDEO=2400;
	private Integer MAX_DURATION_VIDEO=3590;

	private NewsTask task;
	private Account account;
	private TaskFactory taskFactory;
	private ProxyFactory proxyFactory;
	private AccountFactory accountFactory;
	private boolean addAudioToFile;
	private String audioFolderPath;
	private File linkList;
	private File linkTitleList;
	private String listProcessedFilePath;
	private String errorFilePath;

	private Boolean loadPreGenFile = false;

	private int intrvlCount;

	private String lang;
	private String sourcesSrt;
	private int[] frequencies;
	
	private static final Random rnd = new Random();

	public VideoPosterThread(
			NewsTask task, 
			Account account, 
			TaskFactory taskFactory,
			ProxyFactory proxyFactory, 
			AccountFactory accountFactory, 
			boolean addAudioToFile,
			String audioFolderPath,
			File linkList,
			File linkTitleList,
			String listProcessedFilePath,
			String errorFilePath,
			Boolean loadPreGenFile,
			int intrvlCount,
			String lang,
			String sourcesSrt,
			int[] frequencies) {
		this.task = task;
		this.account = account;
		this.taskFactory = taskFactory;
		this.proxyFactory = proxyFactory;
		this.accountFactory = accountFactory;
		this.addAudioToFile = addAudioToFile;
		this.audioFolderPath = audioFolderPath;
		this.linkList = linkList;
		this.linkTitleList = linkTitleList;
		this.listProcessedFilePath = listProcessedFilePath;
		this.errorFilePath = errorFilePath;

		this.loadPreGenFile = loadPreGenFile;
		this.intrvlCount = intrvlCount;

		this.lang = lang;
		this.sourcesSrt = sourcesSrt; 
		this.frequencies = frequencies;

		if(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL) != null)
			MIN_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL));
		if(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL) != null)
			MAX_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL));

		if(ConfigManager.getInstance().getProperty(MIN_DURATION_VIDEO_LABEL) != null)
			MIN_DURATION_VIDEO = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_DURATION_VIDEO_LABEL));
		if(ConfigManager.getInstance().getProperty(MAX_DURATION_VIDEO_LABEL) != null)
			MAX_DURATION_VIDEO = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_DURATION_VIDEO_LABEL));
	}

	@Override
	public void start(){
		taskFactory.incRunThreadsCount();
		super.start();
	}

	@Override
	public void run() {
		synchronized (this) {
			ProxyConnector proxyConnector = null;
			
			try{
				boolean errorExist = false;
				try {
					if(!loadPreGenFile){
						task.parseFile();
					}

					File previewImg = new File("./images/preview_" + getFileNameWOExt(task.getInputFile()) + ".jpg");
					if(!previewImg.exists()){
						previewImg = new File("./images/preview_" + getFileNameWOExt(task.getInputFile()) + ".png");
						if(!previewImg.exists()){
							previewImg = null;
						}
					}
					task.setPreviewImageFile(previewImg);
					//TODO Add random image for generation
					//create video
					Integer[] times = null;
					if(!loadPreGenFile){
						times = createVideoVer2(task, this.addAudioToFile, this.audioFolderPath, previewImg, MIN_DURATION_VIDEO, MAX_DURATION_VIDEO, intrvlCount);
						log.debug("Times array: " + times);
						Thread.sleep(10000L);
					}

					Random rnd = new Random();
					rnd.nextInt(100);

					SnippetTaskWrapper snipWrapTask = new SnippetTaskWrapper(sourcesSrt, frequencies, task.getKey(), lang);
					snipWrapTask.selectRandTask().setPage(rnd.nextInt(50));
					SnippetExtractor snippetExtractor = new SnippetExtractor(snipWrapTask, proxyFactory, new ArrayList<String>());
					snippetExtractor.setAddLinkFromFolder(false);

					//TODO Add Snippet task chooser
					if(MIN_SNIPPET_COUNT == 0 && MAX_SNIPPET_COUNT == 0){
						task.setSnippets("");
					}else{
						String snippetsStr = snippetExtractor.extractSnippetsWithInsertedLinks().getCurrentTask().getResult();

						if(snippetsStr == null || "".equals(snippetsStr.trim()))
							throw new Exception("Could not extract snippets");

						task.setSnippets(snippetsStr);
					}


					//TODO Add Snippet task chooser
					/*if(MIN_SNIPPET_COUNT == 0 && MAX_SNIPPET_COUNT == 0){
						task.setSnippets("");
					}else{
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
					}*/

					proxyConnector = proxyFactory.getRandomProxyConnector();
					
					NewsPoster nPoster = new NewsPoster(task, proxyConnector.getConnect(proxyFactory.getProxyType()), this.account, loadPreGenFile);
					String linkToVideo = nPoster.executePostNews(times);
					appendStringToFile(linkToVideo, linkList);
					appendStringToFile(linkToVideo + ";" + task.getVideoTitle(), linkTitleList);

					//Move file to processed folder
					if(!loadPreGenFile){
						File destFile = new File(listProcessedFilePath + "/" + task.getInputFile().getName());
						if(destFile.exists()){
							destFile.delete();
						}
						FileUtils.moveFile(task.getInputFile(), destFile);
					}

				} 
				catch (Throwable e) {
					log.error(e, e);
					if(e instanceof NoSuchElementException){
						//if 
						accountFactory.markAccountForExclude(account);
					}
					errorExist = true;
					boolean reprocessed = taskFactory.reprocessingTask(task);

					if(!reprocessed){
						try {
							File destFile = new File(errorFilePath + "/" + task.getInputFile().getName());
							if(destFile.exists()){
								destFile.delete();
							}
							FileUtils.moveFile(task.getInputFile(), destFile);
						} catch (IOException e1) {
							log.error(e1);
						}
					}
					log.error("Error occured during process task: " + task.toString(), e);
				}finally{
					if(!loadPreGenFile){
						deleteAllTempFiles(task);
					}
				}
				if(!errorExist){
					taskFactory.putTaskInSuccessQueue(task);
					accountFactory.incrementPostedCounter(account);
				}else{
					accountFactory.releaseAccount(account);
				}
			} finally {
				if(proxyConnector != null){
					proxyFactory.releaseProxy(proxyConnector);
				}
				taskFactory.decRunThreadsCount(task);
			}
		}
	}

	private String getFileNameWOExt(File file){
		String fileName = FilenameUtils.getBaseName(file.getName());
		//fileName.replaceAll("."+FilenameUtils.getBaseName(filename), replacement)
		return fileName;
	}

	public NewsTask getTask(){
		return task;
	}

	private Integer[] createVideo(NewsTask task, boolean addAudioToFile, String audioFolderPath, File previewImg, int minDur, int maxDur, int intrvlCount) throws Exception{
		//TODO Calculate bitrate via file creation
		Integer frameRate = calculateBitRateViaFileCreation(task, addAudioToFile, previewImg);

		Integer[] times = VideoCreator.makeVideo(task.getVideoFile().getPath(), task.getImageFiles(), previewImg, addAudioToFile, new File(audioFolderPath).listFiles(), minDur, maxDur, frameRate, task.isUsePreview(), intrvlCount);
		/*long bitRate = (8*task.getVideoFile().length()/maxDur);
		while(bitRate < VideoCreator.successBitrate){
			times = VideoCreator.makeVideo(task.getVideoFile().getPath(), task.getImageFile(), previewImg, new File("08.wav"), minDur, maxDur, times[1]*2);
			bitRate = (8*task.getVideoFile().length()/maxDur);
		}*/
		return times;
	}
	
	private Integer[] createVideoVer2(NewsTask task, boolean addAudioToFile, String audioFolderPath, File previewImg, int minDur, int maxDur, int intrvlCount) throws Exception{
		//Calculate bitrate via file creation
		//TODO Uncomment after test
		Integer frameRate = 25;
		//frameRate = calculateBitRateViaFileCreation(task, addAudioToFile, previewImg);
		
		//TODO Uncomment if we need add text on the image
		/*ComplexVideoGenerator.drawSpeech2Img(
				new String[]{"Click Link In Description To Download eBook Online"},
				task.getImageFiles()[0],
				task.getImageFiles()[0]
		);*/
		
		File[] rndAudioFiles = new File(audioFolderPath).listFiles();
		
		Integer[] times = VideoCreator.makeVideo(
				task.getVideoFile().getPath(), 
				task.getImageFiles(), 
				previewImg, 
				addAudioToFile, 
				 new File[]{rndAudioFiles[rnd.nextInt(rndAudioFiles.length)]}, 
				minDur, 
				maxDur, 
				frameRate, 
				task.isUsePreview(), 
				intrvlCount, 
				true,
				true
			);

		return times;
	}

	private Integer calculateBitRateViaFileCreation(NewsTask task, boolean addAudioToFile, File previewImg) throws IOException{
		File testFile = new File(task.getVideoFile().getPath() + "_checker.mov");
		Integer[] times = VideoCreator.makeVideo(testFile.getPath(), task.getImageFiles(), previewImg, addAudioToFile, new File[]{new File("08.wav")}, 59, 60, task.isUsePreview());
		long bitRate = (8*testFile.length()/60);
		while(bitRate < VideoCreator.successBitrate && times[1] <= 30){
			testFile.delete();
			times = VideoCreator.makeVideo(testFile.getPath(), task.getImageFiles(), previewImg, addAudioToFile, new File[]{new File("08.wav")}, 59, 60, times[1] + 5, task.isUsePreview(), 1);
			bitRate = (8*testFile.length()/60);
			log.info(String.format("Calculated bitrate/framerate for file %s is %d/%d", testFile.getName(),bitRate, times[1]));
		}

		testFile.delete();

		log.info(String.format("Calculated framerate for file %s is %d", task.getVideoFile().getName(),times[1]));

		return times[1];
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

	private void deleteAllTempFiles(NewsTask task){
		if(task != null ){
			//delete video file
			deleteFile(task.getVideoFile());

			//delete image file
			if(task.isGetImageFromLink()){
				for(File file : task.getImageFiles())
					deleteFile(file);
			}
		}
	}

	private boolean deleteFile(File file){
		if(file != null && file.exists()){
			try {
				log.debug("Delete file: " + file.getName());
				return file.delete();
			} catch (Exception e) {
				log.error(e);
			}
		}

		return false;
	}
}
