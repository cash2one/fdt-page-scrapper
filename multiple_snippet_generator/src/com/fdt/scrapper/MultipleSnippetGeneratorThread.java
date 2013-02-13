/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.TaskFactory;

/**
 *
 * @author Administrator
 */
public class MultipleSnippetGeneratorThread extends Thread {
	private static final Logger log = Logger.getLogger(MultipleSnippetGeneratorThread.class);

	Random rnd = new Random();

	private SnippetTask snippetTask = null;
	private ProxyFactory proxyFactory = null;
	private TaskFactory taskFactory = null;
	private ArrayList<String> linkList = null;

	public MultipleSnippetGeneratorThread(SnippetTask snippetTask, ProxyFactory proxyFactory, TaskFactory taskFactory,ArrayList<String> linkList ) {
		super();
		this.snippetTask = snippetTask;
		this.proxyFactory = proxyFactory;
		this.taskFactory = taskFactory;
		this.linkList = linkList;
	}

	@Override
	public void start(){
		taskFactory.incRunThreadsCount();
		super.start();
	}

	@Override
	public void run()
	{
		synchronized (this)
		{
			boolean errorExist = false;
			String generatedContent = null;
			try{
				try {
					SnippetExtractor snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, linkList);
					generatedContent = snippetExtractor.extractSnippets().getResult();
				}
				catch (Exception e) {
					errorExist = true;
					taskFactory.reprocessingTask(snippetTask);
					log.error("Error occured during processing key: " + snippetTask.getKeyWords());
				}
				if(!errorExist){
					//check task for reprocessing
					if(generatedContent != null && !"".equals(generatedContent.trim())){
						taskFactory.putTaskInSuccessQueue(snippetTask);

					}else{
						taskFactory.reprocessingTask(snippetTask);
					}
				}
			} finally{
				taskFactory.decRunThreadsCount(snippetTask);
			}
		}
	}
}
