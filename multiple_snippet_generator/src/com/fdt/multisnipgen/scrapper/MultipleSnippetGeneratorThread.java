/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.multisnipgen.scrapper;

import java.io.File;
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
	private File pathToLinkFolder = null;
	private boolean addLinkFromFolder = false;

	public MultipleSnippetGeneratorThread(SnippetTaskWrapper snippetTask, ProxyFactory proxyFactory, TaskFactory taskFactory,ArrayList<String> linkList, boolean addLinkFromFolder, File linkFile, File pathToLinkFolder) {
		super();

		this.snippetTask = snippetTask;
		this.proxyFactory = proxyFactory;
		this.taskFactory = taskFactory;
		this.linkList = linkList;
		this.addLinkFromFolder = addLinkFromFolder;
		this.linkFile = linkFile;
		this.pathToLinkFolder = pathToLinkFolder;
		log.debug(toString());
	}

	@Override
	public String call() throws Exception
	{
		taskFactory.incRunThreadsCount();
		try{
			boolean errorExist = false;
			String generatedContent = null;

			try {
				SnippetExtractor snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, linkList);
				snippetExtractor.setAddLinkFromFolder(addLinkFromFolder);
				generatedContent = snippetExtractor.extractSnippetsWithInsertedLinks().getCurrentTask().getResult();
			}
			catch (Throwable e) {
				errorExist = true;
				//move file to input folder
				log.error("Error occured during processing key: " + snippetTask.getCurrentTask().getKeyWords());
			}
			
			if(!errorExist){
				//check task for reprocessing
				if(generatedContent != null && !"".equals(generatedContent.trim())){
					taskFactory.putTaskInSuccessQueue(snippetTask);

					if(addLinkFromFolder && linkFile != null && linkFile.exists()){
						linkFile.delete();
					}
				}else{
					//move file to input folder
					if(addLinkFromFolder && linkFile != null && linkFile.exists()){
						FileUtils.moveFile(linkFile, new File(pathToLinkFolder,linkFile.getName()));
					}
					taskFactory.reprocessingTask(snippetTask);
				}
			}else{
				if(addLinkFromFolder && linkFile != null && linkFile.exists()){
					FileUtils.moveFile(linkFile, new File(pathToLinkFolder,linkFile.getName()));
				}
				taskFactory.reprocessingTask(snippetTask);
			}

			return generatedContent;
		}
		finally{
			taskFactory.decRunThreadsCount(snippetTask.getCurrentTask());
		}
	}

	@Override
	public String toString() {
		return "MultipleSnippetGeneratorThread [snippetTask=" + snippetTask
				+ ", linkFile=" + linkFile + ", pathToLinkFolder="
				+ pathToLinkFolder + ", isInsLnkFrmGenFile="
				+ addLinkFromFolder + "]";
	}
}
