/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.cntgetter.scrapper;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
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

	public KeyDomainCntGetterThread(SnippetTaskWrapper snippetTask, ProxyFactory proxyFactory, TaskFactory taskFactory, boolean isCountDomain) {
		super();

		this.snippetTask = snippetTask;
		this.proxyFactory = proxyFactory;
		this.taskFactory = taskFactory;
		if(isCountDomain){
			snippetTask.getCurrentTask().setKeyWords("site:" + snippetTask.getCurrentTask().getKeyWords());
			snippetTask.getCurrentTask().setUseOrigKeywords(true);
		}
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
				taskFactory.reprocessingTask(snippetTask);
				log.error("Error occured during processing key: " + snippetTask.getCurrentTask().getKeyWords());
			}
			if(!errorExist){
				//check task for reprocessing
				if(cntResult != null && !"".equals(cntResult.trim())){
					taskFactory.putTaskInSuccessQueue(snippetTask);

				}else{
					taskFactory.reprocessingTask(snippetTask);
				}
			}
		} finally{
			taskFactory.decRunThreadsCount(snippetTask.getCurrentTask());
		}
		
		return cntResult;
	}
}