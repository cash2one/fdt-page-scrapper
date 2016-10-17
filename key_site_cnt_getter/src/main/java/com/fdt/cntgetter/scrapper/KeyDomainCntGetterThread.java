/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.cntgetter.scrapper;

import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.scrapper.task.TaskFactory;

/**
 *
 * @author Administrator
 */
public class KeyDomainCntGetterThread implements Callable<String> {
	private static final Logger log = Logger.getLogger(KeyDomainCntGetterThread.class);

	private SnippetTaskWrapper snippetTask = null;
	private ProxyFactory proxyFactory = null;
	private TaskFactory taskFactory = null;
	private boolean isCountDomain;

	public KeyDomainCntGetterThread(SnippetTaskWrapper snippetTask, ProxyFactory proxyFactory, TaskFactory taskFactory, boolean isCountDomain) {
		super();

		this.snippetTask = snippetTask;
		this.proxyFactory = proxyFactory;
		this.taskFactory = taskFactory;
		this.isCountDomain = isCountDomain;
		
		if(isCountDomain){
			String site = extractHref(snippetTask.getCurrentTask().getKeyWordsOrig());
			snippetTask.getCurrentTask().setKeyWords("site:" + site);
			snippetTask.getCurrentTask().setUseOrigKeywords(true);
		}
	}
	
	private String extractHref(String input){
		Pattern p = Pattern.compile("href=\"(.*?)\"");
		Matcher m = p.matcher(input);
		String url;
		if (m.find()) {
		    url = m.group(1); // this variable should contain the link URL
		}else{
			url = input;
		}
		
		return url;
	}

	@Override
	public String call() throws Exception
	{
		taskFactory.incRunThreadsCount();
		String cntResult = null;
		
		boolean errorExist = false;
		try{
			try {
				SnippetExtractor snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, null);
				
				cntResult = snippetExtractor.extractResultCount();
				cntResult = snippetTask.getCurrentTask().getRsltCnt(cntResult).toString();
				
				snippetTask.getCurrentTask().setResult(cntResult);
			}
			catch (Exception e) {
				errorExist = true;
				if(isCountDomain){
					clearKeyWord(snippetTask.getCurrentTask());
				}
				taskFactory.reprocessingTask(snippetTask);
				log.error("Error occured during processing key: " + snippetTask.getCurrentTask().getKeyWords());
			}
			if(!errorExist){
				//check task for reprocessing
				if(cntResult != null && !"".equals(cntResult.trim())){
					taskFactory.putTaskInSuccessQueue(snippetTask);

				}else{
					if(isCountDomain){
						clearKeyWord(snippetTask.getCurrentTask());
					}
					taskFactory.reprocessingTask(snippetTask);
				}
			}
		} finally{
			taskFactory.decRunThreadsCount(snippetTask.getCurrentTask());
		}
		
		return cntResult;
	}
	
	private void clearKeyWord(SnippetTask snipTask){
		String clearKey = snipTask.getKeyWordsOrig().substring(5);
		snipTask.setKeyWords(clearKey);
	}
}
