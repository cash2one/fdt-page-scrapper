/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.multisnipgen.scrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.scrapper.task.TaskFactory;

/**
 *
 * @author Administrator
 */
public class MultipleSnippetGeneratorThread implements Callable<String> {
	private static final Logger log = Logger.getLogger(MultipleSnippetGeneratorThread.class);

	private SnippetTaskWrapper snippetTask = null;
	private ProxyFactory proxyFactory = null;
	private TaskFactory taskFactory = null;
	private ArrayList<String> linkList = null;
	private File linkFile = null;
	private boolean isInsLnkFrmGenFile = false;

	public MultipleSnippetGeneratorThread(SnippetTaskWrapper snippetTask, ProxyFactory proxyFactory, TaskFactory taskFactory,ArrayList<String> linkList, boolean isInsLnkFrmGenFile, File linkFile) {
		super();

		this.snippetTask = snippetTask;
		this.proxyFactory = proxyFactory;
		this.taskFactory = taskFactory;
		this.linkList = linkList;
		this.isInsLnkFrmGenFile = isInsLnkFrmGenFile;
		this.linkFile = linkFile;

		//move file to processing dir
		if(isInsLnkFrmGenFile){
			try {
				FileUtils.moveFile(linkFile, new File("error/",linkFile.getName()));
				this.linkFile = new File("error/",linkFile.getName());
			} catch (IOException e) {
				log.error(String.format("Error during moving file %s", linkFile.getName()),e);
			}
		}
	}

	@Override
	public String call() throws Exception
	{
		taskFactory.incRunThreadsCount();
		boolean errorExist = false;
		String generatedContent = null;
		try{
			try {
				SnippetExtractor snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, linkList);
				snippetExtractor.setInsLnkFrmGenFile(isInsLnkFrmGenFile);
				generatedContent = snippetExtractor.extractSnippetsWithInsertedLinks().getCurrentTask().getResult();
			}
			catch (Exception e) {
				errorExist = true;
				taskFactory.reprocessingTask(snippetTask);
				log.error("Error occured during processing key: " + snippetTask.getCurrentTask().getKeyWords());
			}
			if(!errorExist){
				//check task for reprocessing
				if(generatedContent != null && !"".equals(generatedContent.trim())){
					taskFactory.putTaskInSuccessQueue(snippetTask);

				}else{
					taskFactory.reprocessingTask(snippetTask);
				}
				if(isInsLnkFrmGenFile && linkFile != null && linkFile.exists()){
					linkFile.delete();
				}
			}
		} finally{
			taskFactory.decRunThreadsCount(snippetTask.getCurrentTask());
		}
		
		return generatedContent;
	}
}
