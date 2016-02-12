package com.fdt.jimbo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.fdt.jimbo.task.NewsTask;
import com.fdt.jimbo.task.TaskFactory;
import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.utils.Constants;

public class JimboPosterThread extends Thread{

	private static final Logger log = Logger.getLogger(JimboPosterThread.class);

	private static final String MAX_SNIPPET_COUNT_LABEL = "MAX_SNIPPET_COUNT";
	private static final String MIN_SNIPPET_COUNT_LABEL = "MIN_SNIPPET_COUNT";

	private Integer MIN_SNIPPET_COUNT=5;
	private Integer MAX_SNIPPET_COUNT=10;

	private NewsTask task;
	private Account account;
	private TaskFactory taskFactory;
	private ProxyFactory proxyFactory;
	private AccountFactory accountFactory;
	private File lnkLstFl4Res;
	private File lnkTtlLstFl4Res;
	private String listProcessedFilePath;
	private String errorFilePath;
	
	private ArrayList<String> linkList;
	
	private String lang;
	private String sourcesSrt;
	private int[] frequencies;

	public JimboPosterThread(
			NewsTask task, 
			Account account, 
			TaskFactory taskFactory,
			ProxyFactory proxyFactory, 
			AccountFactory accountFactory, 
			File lnkLstFl4Res,
			File lnkTtlLstFl4Res,
			String listProcessedFilePath,
			String errorFilePath,
			ArrayList<String> linkList,
			String lang,
			String sourcesSrt, 
			int[] frequencies
			) 
	{
		this.task = task;
		this.account = account;
		this.taskFactory = taskFactory;
		this.proxyFactory = proxyFactory;
		this.accountFactory = accountFactory;
		this.lnkLstFl4Res = lnkLstFl4Res;
		this.lnkTtlLstFl4Res = lnkTtlLstFl4Res;
		this.listProcessedFilePath = listProcessedFilePath;
		this.errorFilePath = errorFilePath;
		
		this.lang = lang;
		this.sourcesSrt = sourcesSrt; 
		this.frequencies = frequencies;

		if(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL) != null)
			MIN_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL));
		if(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL) != null)
			MAX_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL));
	}

	@Override
	public void start(){
		taskFactory.incRunThreadsCount();
		super.start();
	}

	@Override
	public void run() {
		synchronized (this) {
			try{
				boolean errorExist = false;
				try {

					SnippetTaskWrapper snipWrapTask = new SnippetTaskWrapper(sourcesSrt, frequencies, task.getKey(), lang);
					SnippetExtractor snippetExtractor = new SnippetExtractor(snipWrapTask, proxyFactory, linkList);
					
					//TODO Add random image for generation
					//create video
					Integer[] times = null;
					
					//TODO Add Snippet task chooser
					if(MIN_SNIPPET_COUNT == 0 && MAX_SNIPPET_COUNT == 0){
						task.setSnippets("");
					}else{
						ArrayList<Snippet> snippets = snippetExtractor.extractSnippetsWithInsertedLinks().getCurrentTask().getSnipResult();
						
						if(snippets.size() == 0)
							throw new Exception("Could not extract snippets");
	
						//get random snippets
						snippets = getRandSnippets(snippets, snippetExtractor);
	
						StringBuilder snippetsStr = new StringBuilder(); 
						for(Snippet snippet : snippets){
							snippetsStr.append(Constants.LINE_FEED).append(Constants.LINE_FEED).append(snippet.toString());
						}
						task.setSnippets(snippetsStr.toString());
					}

					NewsPoster nPoster = new NewsPoster(task, proxyFactory.getRandomProxyConnector().getConnect(ProxyFactory.PROXY_TYPE), this.account);
					String linkToVideo = nPoster.executePostNews(times);
					appendStringToFile(linkToVideo, lnkLstFl4Res);
					appendStringToFile(linkToVideo + ";" + task.getTitle(), lnkTtlLstFl4Res);

					//Move file to processed folder
					File destFile = new File(listProcessedFilePath + "/" + task.getInputFile().getName());
					if(destFile.exists()){
						destFile.delete();
					}
					FileUtils.moveFile(task.getInputFile(), destFile);

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
