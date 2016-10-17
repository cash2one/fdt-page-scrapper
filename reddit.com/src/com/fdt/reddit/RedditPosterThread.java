package com.fdt.reddit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.fdt.reddit.task.RedditTask;
import com.fdt.reddit.task.TaskFactory;
import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.utils.Utils;

public class RedditPosterThread implements Runnable {

	private static final Logger log = Logger.getLogger(RedditPosterThread.class);

	private static final String MAX_SNIPPET_COUNT_LABEL = "MAX_SNIPPET_COUNT";
	private static final String MIN_SNIPPET_COUNT_LABEL = "MIN_SNIPPET_COUNT";

	private Integer MIN_SNIPPET_COUNT=1;
	private Integer MAX_SNIPPET_COUNT=3;

	private RedditTask task;
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

	private static final Lock fileSaveLock= new ReentrantLock();

	public RedditPosterThread(
			RedditTask task, 
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
	public void run() {

		this.taskFactory.incRunThreadsCount();

		try{
			synchronized (this) 
			{
				ProxyConnector proxyConnector = null;
				
				try
				{
					proxyConnector = proxyFactory.getRandomProxyConnector();
					Random rnd = new Random();
					rnd.nextInt();

					account.setLoginErr(false);
					
					if(account.isLogged() || accountFactory.loginAccount(account, proxyConnector)){
						account.setLogged(true);
					}else{
						if(!account.isLoginErr()){
							//account was banned
							accountFactory.removeNotLoggedAccount(account);
							log.error(String.format("Account '%s' was added to remove list", account.getLogin()));
						}else{
							//some error occured during login. account will be relogined in future
							log.warn(String.format("Account '%s' will be relogin", account.getLogin()));
							account.setLogged(false);
							accountFactory.releaseAccount(account);
						}
						return;
					}

					log.debug(String.format("Starting processing file %s ...", task.getLink()));


					/*boolean errorExist = false;
					try {

						SnippetTaskWrapper snipWrapTask = new SnippetTaskWrapper(sourcesSrt, frequencies, task.getKey(), lang);
						snipWrapTask.selectRandTask().setPage(rnd.nextInt(50));
						SnippetExtractor snippetExtractor = new SnippetExtractor(snipWrapTask, proxyFactory, new ArrayList<String>());
						snippetExtractor.setAddLinkFromFolder(addLinkFromFolder);

						log.debug(String.format("Starting extract snippets for account %s ", account.getLogin()));
						//TODO Add Snippet task chooser
						if(MIN_SNIPPET_COUNT == 0 && MAX_SNIPPET_COUNT == 0){
							task.setSnippets("");
						}else{
							String snippetsStr = snippetExtractor.extractSnippetsWithInsertedLinks(false).getCurrentTask().getResult();

							if(snippetsStr == null || "".equals(snippetsStr.trim()))
								throw new Exception("Could not extract snippets");

							task.setSnippets(snippetsStr);
						}

						log.debug(String.format("Snippets for account '%s' are extracted", account.getLogin()));

						NewsPoster nPoster = new NewsPoster(task, proxyConnector.getConnect(ProxyFactory.PROXY_TYPE), this.account, accountFactory);

						String url2Post = nPoster.postNews();

						fileSaveLock.lock();
						try{
							log.info(String.format("ACCOUNT (%s) NEWS URL: %s", account.getLogin(), url2Post));
							log.debug(String.format("Saving result to file %s file", lnkLstFl4Res));
							Utils.appendStringToFile(url2Post, lnkLstFl4Res);
							log.debug(String.format("Saving result to file %s file", lnkTtlLstFl4Res));
							Utils.appendStringToFile(url2Post + ";" + task.getLink(), lnkTtlLstFl4Res);
						}finally{
							fileSaveLock.unlock();
						}

						String domainFilePath = account.getSite().substring(7) + ".txt";
						log.debug(String.format("Saving key %s for account %s to file %s file", task.getKey(), account.getLogin(), domainFilePath));
						Utils.appendStringToFile(task.getKey(), new File("./domen", domainFilePath));

						//Move file to processed folder
						File destFile = new File(listProcessedFilePath + "/" + task.getLink());
						if(destFile.exists()){
							destFile.delete();
						}
						
						//TODO Save link to processed file
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
							//TODO Save link to error file
						}
							
						log.error(String.format("Account: '%s'; Error occured during process link: '%s'", account.getLogin(), task.toString()), e);
					}*/

					/*if(!errorExist){
						taskFactory.putTaskInSuccessQueue(task);
						accountFactory.incrementPostedCounter(account);
					}else{
						account.setLogged(false);
						accountFactory.releaseAccount(account);
					}*/
				} finally {
					if(proxyConnector != null){
						proxyFactory.releaseProxy(proxyConnector);
					}
				}
			}
			log.debug(String.format("Processing task '%s' IS COMPLETED", task.toString()));
		}
		finally{
			taskFactory.decRunThreadsCount(task);
		}
	}

	public RedditTask getTask(){
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
