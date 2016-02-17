package com.fdt.jimbo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.fdt.jimbo.task.NewsTask;
import com.fdt.jimbo.task.TaskFactory;
import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.utils.Constants;
import com.fdt.utils.Utils;

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
	private boolean addLinkFromFolder = true;
	
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
		synchronized (this) 
		{
			Random rnd = new Random();
			rnd.nextInt();
			
			try
			{
				boolean errorExist = false;
				try {

					SnippetTaskWrapper snipWrapTask = new SnippetTaskWrapper(sourcesSrt, frequencies, task.getKey4Search(), lang);
					snipWrapTask.selectRandTask().setPage(rnd.nextInt(50));
					SnippetExtractor snippetExtractor = new SnippetExtractor(snipWrapTask, proxyFactory, new ArrayList<String>());
					snippetExtractor.setAddLinkFromFolder(addLinkFromFolder);
					
					//TODO Add Snippet task chooser
					if(MIN_SNIPPET_COUNT == 0 && MAX_SNIPPET_COUNT == 0){
						task.setSnippets("");
					}else{
						String snippetsStr = snippetExtractor.extractSnippetsWithInsertedLinks().getCurrentTask().getResult();
						
						if(snippetsStr == null || "".equals(snippetsStr.trim()))
							throw new Exception("Could not extract snippets");
	
						task.setSnippets(snippetsStr);
					}

					NewsPoster nPoster = new NewsPoster(task, proxyFactory.getRandomProxyConnector().getConnect(ProxyFactory.PROXY_TYPE), this.account, accountFactory);
					String url2Post = nPoster.postNews();
					Utils.appendStringToFile(url2Post, lnkLstFl4Res);
					Utils.appendStringToFile(url2Post + ";" + task.getTitle(), lnkTtlLstFl4Res);
					
					String domainFilePath = account.getSite().substring(7) + ".txt";
					Utils.appendStringToFile(task.getKey(), new File("./domen", domainFilePath));
					
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
					log.error(String.format("Account: %s; Error occured during process task: %s", account.getLogin(), task.toString()), e);
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

	public NewsTask getTask(){
		return task;
	}

	/*private ArrayList<Snippet> getRandSnippets(List<Snippet> snippets, SnippetExtractor snpExtr){
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

		log.debug(String.format("Keywords: %s. Snippets size: %d, Generated snippet count: %d", task.getKey(), snippets.size(), snipCount));

		int indexShift = snpExtr.getRandomValue(0,snippets.size()-snipCount); 

		for(int i = indexShift; i < (snipCount+indexShift); i++){
			rndSnipList.add(snippets.get(i));	
		}

		return rndSnipList;
	}*/
}
