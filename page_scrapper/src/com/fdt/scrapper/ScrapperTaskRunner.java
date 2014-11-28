package com.fdt.scrapper;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.PageTasks;
import com.fdt.scrapper.task.Task;
import com.fdt.scrapper.util.ResultParser;

/**
 * @author VarenKoks
 */
public class ScrapperTaskRunner {
	private static final Logger log = Logger.getLogger(ScrapperTaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	private String proxyFilePath;
	private String urlsFilePath;
	private int maxThreadCount;
	private long proxyDelay;
	private String resultFile;
	private boolean scrapResultViaProxy;
	
	private boolean appendToPrevResult = false;
	
	private SaverThreadPS saver;
	
	private TaskFactory taskFactory;

	//private ArrayList<Thread> threads = new ArrayList<Thread>();

	public ScrapperTaskRunner(final String login, final char[] pass, String proxyFilePath, String urlsFilePath, int maxThreadCount, long proxyDelay, String resultFile, boolean scrapResultViaProxy, boolean appendToPrevResult){
		this.proxyFilePath = proxyFilePath;
		this.urlsFilePath = urlsFilePath;
		this.maxThreadCount = maxThreadCount;
		this.proxyDelay = proxyDelay;
		this.resultFile = resultFile;
		this.scrapResultViaProxy = scrapResultViaProxy;
		this.appendToPrevResult = appendToPrevResult;
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(login, pass);
			}
		});
	}

	public static void main(String[] args) {
		try{
			ScrapperTaskRunner taskRunner = new ScrapperTaskRunner("EUR102217", "J8Fjh5TN5H".toCharArray(),"proxy.txt","links_small.txt", 1, 5000L, "success_result.csv", true, true);
			taskRunner.run();

			ResultParser rp = new ResultParser();
			ArrayList<PageTasks> scrappResults = rp.parseResultFile("success_result.csv");
			System.out.println(scrappResults);

		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	public void run(){
		synchronized (this) {
			TaskFactory.MAX_THREAD_COUNT = maxThreadCount;
			taskFactory = TaskFactory.getInstance();
			taskFactory.clear();
			//taskFactory.loadTaskQueue(urlsFilePath);
			File resultFileFile = new File(resultFile);
			
			log.debug("Append to previous result: " + appendToPrevResult);
			if(appendToPrevResult && resultFileFile.exists()){
				taskFactory.loadTaskQueue(urlsFilePath, resultFile);
			}else{
				taskFactory.loadTaskQueue(urlsFilePath, null);
			}
			
			saver = new SaverThreadPS(taskFactory, this.resultFile);
			saver.start();

			if(scrapResultViaProxy){
				ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
				ProxyFactory proxyFactory = ProxyFactory.getInstance();
				proxyFactory.init(proxyFilePath);

				ScrapperThread newThread = null;
				log.debug("Total tasks: "+taskFactory.getTaskQueue().size());
				while(!taskFactory.isTaskFactoryEmpty() || taskFactory.runThreadsCount > 0){
					log.debug("Try to get request from RequestFactory queue.");
					PageTasks tasks = taskFactory.getTask();
					if(null != tasks){
						log.debug("Pending tasts: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getResultQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
						newThread = new ScrapperThread(tasks, taskFactory, proxyFactory);
						newThread.start();
						//threads.add(newThread);

					}
					else{
						try {
							log.debug("Waiting...");
							this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
						} catch (InterruptedException e) {
							log.error("InterruptedException occured during RequestRunner process: ",e);
						}
					}
				}

				log.debug("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.runThreadsCount);
				log.debug("Success tasks: "+taskFactory.getResultQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());

				/*for(Thread thread : threads){
				if(thread != null && newThread.isAlive()){
					try {
						thread.join();
						log.debug("Run join method for next thread: " + thread.getName());
					} catch (InterruptedException e) {
						log.error("Error occured during join method",e);
					}
				}
			}*/
			}else{
				for(PageTasks pageTask:taskFactory.getTaskQueue()){
					for(Task task:pageTask.getTasks()){
						task.setResultAsIs(new ArrayList<String>(Arrays.asList("1")));
					}
					taskFactory.putTaskInSuccessQueue(pageTask);
				}
			}
			
			saver.interrupt();	
			try {
				saver.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
