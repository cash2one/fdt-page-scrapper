/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.multisnipgen.scrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.scrapper.task.TaskFactory;
import com.fdt.utils.Utils;

/**
 *
 * @author Administrator
 */
public class MegaMultipleSnippetGeneratorThread implements Callable<String> {
	private static final Logger log = Logger.getLogger(MegaMultipleSnippetGeneratorThread.class);

	private SnippetTaskWrapper snippetTask = null;
	private ProxyFactory proxyFactory = null;
	private TaskFactory taskFactory = null;

	private boolean isAddLinks = true;
	private boolean addLinkFromFolder = false;
	private ArrayList<String> linkList = null;
	private File linkFile = null;
	private File pathToLinkFolder = null;
	
	private boolean isUseLinkCnt4SnpCnt;
	private int minSnpCntIncrement;
	private int maxSnpCntIncrement;

	//словарь в котором содержится <ключ> и в соответствие ему ставится файл, в котором содержится обрабатываемый ключ
	private Map<String, List<File>> keyWordFileMapping;
	private File successPathFile;

	public MegaMultipleSnippetGeneratorThread(
			SnippetTaskWrapper snippetTask, 
			ProxyFactory proxyFactory, 
			TaskFactory taskFactory,
			boolean isAddLinks,
			boolean addLinkFromFolder,
			ArrayList<String> linkList, 
			File linkFile, 
			File pathToLinkFolder, 
			Map<String, List<File>> keyWordFileMapping,
			File successPathFile,
			boolean isUseLinkCnt4SnpCnt,
			int minSnpCntIncrement,
			int maxSnpCntIncrement) 
	{
		super();

		this.snippetTask = snippetTask;
		this.proxyFactory = proxyFactory;
		this.taskFactory = taskFactory;
		
		this.isAddLinks = isAddLinks;
		this.linkList = linkList;
		this.addLinkFromFolder = addLinkFromFolder;
		this.linkFile = linkFile;
		this.pathToLinkFolder = pathToLinkFolder;
		this.keyWordFileMapping = keyWordFileMapping;
		this.successPathFile = successPathFile;
		this.isUseLinkCnt4SnpCnt = isUseLinkCnt4SnpCnt;
		this.minSnpCntIncrement = minSnpCntIncrement;
		this.maxSnpCntIncrement = maxSnpCntIncrement;
		log.debug(toString());
	}

	@Override
	public String call() throws Exception
	{
		taskFactory.incRunThreadsCount();
		try{
			boolean errorExist = false;
			String generatedContent = null;

			log.warn(String.format("Starting processing key: %s", snippetTask.getCurrentTask().getKeyWordsOrig()));

			try {
				SnippetExtractor snippetExtractor;
				
				if(isUseLinkCnt4SnpCnt && isAddLinks){
					snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, linkList, linkList.size() + minSnpCntIncrement, linkList.size() + maxSnpCntIncrement, linkList.size(), linkList.size());
				}else{
					snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, linkList);
				}
				
				snippetExtractor.setAddLinkFromFolder(addLinkFromFolder);
				generatedContent = snippetExtractor.extractSnippetsWithInsertedLinks(isAddLinks).getCurrentTask().getResult();
			}
			catch (Throwable e) {
				errorExist = true;
				//move file to input folder
				log.error("Error occured during processing key: " + snippetTask.getCurrentTask().getKeyWords());
			}

			List<File> processedFileList  = null;

			if(!errorExist && generatedContent != null && !"".equals(generatedContent.trim())){
				//check task for reprocessing

				taskFactory.putTaskInSuccessQueue(snippetTask);
				try{
					MegaMultipleSnippetGeneratorRunner.readWriteLock.lock();
					//получаем список обработанных файлов
					processedFileList = keyWordFileMapping.remove(snippetTask.getCurrentTask().getKeyWordsOrig());
				}finally{
					MegaMultipleSnippetGeneratorRunner.readWriteLock.unlock();
				}

				//сохранем результаты в файлы в папку output/success
				for(File processedFile : processedFileList){
					File outputFile = new File(successPathFile, processedFile.getName());

					//создаём файл с результатами в output папке
					if(outputFile.exists()){
						log.warn(String.format("Another files %s exist in output(success) folder. Another file will be deleted", processedFile.getName()));
						outputFile.delete();
					}
					Utils.appendStringToFile(generatedContent, outputFile);

					//удаляем обработанный файл с кеем
					if(processedFile.exists()){
						processedFile.delete();
					}
					log.warn(String.format("File %s was processed", processedFile.getName()));
				}

				//если линки успешно добавлены из отдельного файла с линками, то удаляем этот файл
				if(addLinkFromFolder && linkFile != null && linkFile.exists()){
					linkFile.delete();
				}

			}else{

				//возвращаем файл линка обратно в папку, откуда его взяли
				if(addLinkFromFolder && linkFile != null && linkFile.exists()){
					FileUtils.moveFile(linkFile, new File(pathToLinkFolder,linkFile.getName()));
				}

				//пытаемся вернуть таск обратно на повторную обработку
				if(!taskFactory.reprocessingTask(snippetTask)){

					//
					try{
						MegaMultipleSnippetGeneratorRunner.readWriteLock.lock();
						processedFileList = keyWordFileMapping.get(snippetTask.getCurrentTask().getKeyWordsOrig());
					}finally{
						MegaMultipleSnippetGeneratorRunner.readWriteLock.unlock();
					}

					for(File processedFile : processedFileList){
						//пытаемся переместить файл из папки processing в папку error
						File destFile = new File("error/" + processedFile.getName());
						if(destFile.exists()){
							destFile.delete();
						}

						//перемещаем файл в папку error если было совершено заданное количество попыток его обработки 
						log.warn(String.format("Moving file %s to error folder", destFile.getName()));
						FileUtils.moveFile(processedFile, destFile);
						if(processedFile.exists()){
							processedFile.delete();
						}
					}
				}
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
