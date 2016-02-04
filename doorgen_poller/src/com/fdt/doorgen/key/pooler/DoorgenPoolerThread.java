package com.fdt.doorgen.key.pooler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyConnector;
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
	private int minSnipCount4Extract = DoorgenPoolerRunner.MIN_SNIPPET_COUNT_FOR_POST_PAGE;

	public DoorgenPoolerThread(SnippetTaskWrapper snippetTask, ProxyFactory proxyFactory, TaskFactory taskFactory, int minSnipCount4Extract) {
		super();

		this.snippetTask = snippetTask;
		this.proxyFactory = proxyFactory;
		this.taskFactory = taskFactory;
		this.minSnipCount4Extract = minSnipCount4Extract;
	}

	public String call() throws Exception
	{
		taskFactory.incRunThreadsCount();
		boolean errExist = false;

		Random rnd = new Random();
		String generatedContent = "";
		HashSet<Snippet> snippetResult = new HashSet<Snippet>();
		snippetTask.selectRandTask().setPage(50 + rnd.nextInt(20));
		ProxyConnector proxyConnector = proxyFactory.getRandomProxyConnector();

		try{
			//TODO Get proxy connector
			while(snippetResult.size() < minSnipCount4Extract && snippetTask.getCurrentTask().getPage() > 1){
				try {
					//≈сли не было ошибки - то уменьшаем количество страниц, 
					if(!errExist){
						snippetTask.selectRandTask().setPage(reducePage(snippetTask.selectRandTask().getPage()));
					}
					//иначе не уменьшаем, и пробуем с другим прокси
					else{
						errExist = false;
						if(proxyConnector != null){
							proxyFactory.releaseProxy(proxyConnector);
							proxyConnector = proxyFactory.getRandomProxyConnector();
						}
					}

					SnippetExtractor snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, new ArrayList<String>());
					snippetResult.addAll(snippetExtractor.extractSnippetsFromPageContent(proxyConnector));
				}
				catch (Exception e) {
					errExist = true;
					log.warn("Error occured during processing key: " + snippetTask.getCurrentTask().getKeyWords());
				}
			}

			//check task for reprocessing
			if(snippetResult != null && snippetResult.size() > 0){
				snippetTask.getCurrentTask().setSnipResult(new ArrayList<Snippet>(snippetResult));
				taskFactory.putTaskInSuccessQueue(snippetTask);

			}else{
				taskFactory.reprocessingTask(snippetTask);
			}
		} finally{
			if(proxyConnector != null){
				proxyFactory.releaseProxy(proxyConnector);
				proxyConnector = null;
			}
			//TODO Release proxy connector
			taskFactory.decRunThreadsCount(snippetTask.getCurrentTask());
		}

		return generatedContent;
	}

	private int reducePage(int currentPage){
		if(currentPage/5 > 1){
			log.info(String.format("Recude page from %d to %d",currentPage, currentPage/5 ));
			return currentPage/5;
		}else{
			log.info(String.format("Recude page from %d to %d",currentPage, 1));
			return 1;
		}
	}
}
