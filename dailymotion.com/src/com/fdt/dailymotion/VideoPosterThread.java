package com.fdt.dailymotion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.media.MediaLocator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.fdt.dailymotion.task.NewsTask;
import com.fdt.dailymotion.task.TaskFactory;
import com.fdt.dailymotion.util.AudioVideoMerger;
import com.fdt.dailymotion.util.JpegImagesToMovie;
import com.fdt.dailymotion.util.VideoCreator;
import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.BingSnippetTask;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;

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
	private File linkList;
	private File linkTitleList;
	private String listProcessedFilePath;
	private String errorFilePath;
	
	public VideoPosterThread(
			NewsTask task, 
			Account account, 
			TaskFactory taskFactory,
			ProxyFactory proxyFactory, 
			AccountFactory accountFactory, 
			boolean addAudioToFile,
			File linkList,
			File linkTitleList,
			String listProcessedFilePath,
			String errorFilePath) {
		this.task = task;
		this.account = account;
		this.taskFactory = taskFactory;
		this.proxyFactory = proxyFactory;
		this.accountFactory = accountFactory;
		this.addAudioToFile = addAudioToFile;
		this.linkList = linkList;
		this.linkTitleList = linkTitleList;
		this.listProcessedFilePath = listProcessedFilePath;
		this.errorFilePath = errorFilePath;

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
		ProxyConnector proxyConnector = null;
		synchronized (this) {
			try{
				boolean errorExist = false;
				try {
					task.parseFile();

					SnippetExtractor snippetExtractor = new SnippetExtractor(null, proxyFactory, null);
					File previewImg = new File("./images/preview_" + getFileNameWOExt(task.getInputFile()) + ".jpg");
					if(!previewImg.exists()){
						previewImg = new File("./images/preview_" + getFileNameWOExt(task.getInputFile()) + ".png");
						if(!previewImg.exists()){
							previewImg = null;
						}
					}
					//create video
					Integer[] times = createVideo(task, this.addAudioToFile, previewImg, MIN_DURATION_VIDEO, MAX_DURATION_VIDEO);
					Thread.sleep(10000L);
					
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

					NewsPoster nPoster = new NewsPoster(task, proxyFactory.getRandomProxyConnector().getConnect(ProxyFactory.PROXY_TYPE), this.account);
					String linkToVideo = nPoster.executePostNews(times);
					appendStringToFile(linkToVideo, linkList);
					appendStringToFile(linkToVideo + ";" + task.getVideoTitle(), linkTitleList);

					//Move file to processed folder
					File destFile = new File(listProcessedFilePath + "/" + task.getInputFile().getName());
					if(destFile.exists()){
						destFile.delete();
					}
					FileUtils.moveFile(task.getInputFile(), destFile);

				} 
				catch (Throwable e) {
					if(e instanceof NoSuchElementException){
						accountFactory.checkAccountForExclude(account);
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
					deleteVideoFile(task);
					if(proxyConnector != null){
						proxyFactory.releaseProxy(proxyConnector);
						proxyConnector = null;
					}

				}
				if(!errorExist){
					taskFactory.putTaskInSuccessQueue(task);
					accountFactory.incrementPostedCounter(account);
				}else{
					accountFactory.releaseAccount(account);
				}
			} finally {
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

	private Integer[] createVideo(NewsTask task, boolean addAudioToFile, File previewImg, int minDur, int maxDur) throws Exception{
		AudioVideoMerger avMerger = new AudioVideoMerger();

		Integer[] times = VideoCreator.makeVideo(task.getVideoFile().getPath(), task.getImageFile(), previewImg, new File("08.wav"), minDur, maxDur);

		/*if(addAudioToFile){
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
		}*/
		
		return times;
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

	private void deleteVideoFile(NewsTask task){
		if(task != null ){
			if(task.getVideoFile() != null && task.getVideoFile().exists()){
				try {
					log.debug("Delete video file: " + task.getVideoFile().getName());
					task.getVideoFile().delete();
				} catch (Exception e) {
					log.error(e);
				}
			}

			/*if(task.getVideoFileWOAudio() != null && task.getVideoFileWOAudio().exists()){
				try {
					log.debug("Delete video WO audio file: " + task.getVideoFileWOAudio().getName());
					task.getVideoFileWOAudio().delete();
				} catch (Exception e) {
					log.error(e);
				}
			}*/
		}
	}
}
