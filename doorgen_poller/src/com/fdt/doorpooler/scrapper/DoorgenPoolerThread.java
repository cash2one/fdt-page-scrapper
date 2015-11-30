package com.fdt.doorpooler.scrapper;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.scrapper.task.TaskFactory;

/**
 *
 * @author Administrator
 */
public class DoorgenPoolerThread implements Callable<String> {
	private static final Logger log = Logger.getLogger(DoorgenPoolerThread.class);

	private SnippetTaskWrapper snippetTask = null;
	private ProxyFactory proxyFactory = null;
	private TaskFactory taskFactory = null;

	public DoorgenPoolerThread(SnippetTaskWrapper snippetTask, ProxyFactory proxyFactory, TaskFactory taskFactory) {
		super();

		this.snippetTask = snippetTask;
		this.proxyFactory = proxyFactory;
		this.taskFactory = taskFactory;
	}

	@Override
	public String call() throws Exception
	{
		taskFactory.incRunThreadsCount();
		boolean errorExist = false;
		String generatedContent = null;
		ArrayList<Snippet> snippetResult = null;
		try{
			try {
				SnippetExtractor snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, new ArrayList<String>());
				snippetResult = snippetExtractor.extractSnippetsFromPageContent();
			}
			catch (Exception e) {
				errorExist = true;
				taskFactory.reprocessingTask(snippetTask);
				log.error("Error occured during processing key: " + snippetTask.getCurrentTask().getKeyWords());
			}
			if(!errorExist){
				//check task for reprocessing
				if(snippetResult != null && snippetResult.size() > 0){
					taskFactory.putTaskInSuccessQueue(snippetTask);

				}else{
					taskFactory.reprocessingTask(snippetTask);
				}
			}
		} finally{
			taskFactory.decRunThreadsCount(snippetTask.getCurrentTask());
		}
		
		return generatedContent;
	}
}
