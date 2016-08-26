package com.fdt.scrapper;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.PageTasks;
import com.fdt.scrapper.task.PrTicTask;
import com.fdt.scrapper.task.Task;
import com.fdt.scrapper.util.ResultParser;

/**
 * @author VarenKoks
 */
/**@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.fdt.scrapper.proxy")
@PropertySource("classpath:config.ini")
**/
public class ScrapperTaskRunner implements Runnable{
	private static final Logger log = Logger.getLogger(ScrapperTaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 50L;

	@Autowired
	private ProxyFactory proxyFactory;
	
	private String urlsFilePath;
	private String resultFile;
	private boolean scrapResultViaProxy;

	private boolean appendToPrevResult = false;
	
	private int topCountForScan = 0;

	private SaverThreadPS saver;

	private TaskFactory taskFactory;

	private ICallback callback;
	
	public ScrapperTaskRunner(final String login, final char[] pass, String urlsFilePath, int maxThreadCount,  String resultFile, boolean scrapResultViaProxy, boolean appendToPrevResult, ICallback callback){
		this(
				login, 
				pass, 
				urlsFilePath, 
				maxThreadCount, 
				resultFile, 
				scrapResultViaProxy, 
				appendToPrevResult, 
				0, 
				callback
			);
	}
	
	//private ArrayList<Thread> threads = new ArrayList<Thread>();
	public ScrapperTaskRunner(final String login, final char[] pass, String urlsFilePath, int maxThreadCount, String resultFile, boolean scrapResultViaProxy, boolean appendToPrevResult, int topCountForScan, ICallback callback){
		super();
		this.urlsFilePath = urlsFilePath;
		this.resultFile = resultFile;
		this.scrapResultViaProxy = scrapResultViaProxy;
		this.appendToPrevResult = appendToPrevResult;
		this.topCountForScan = topCountForScan;
		this.callback = callback;
		
		TaskFactory.MAX_THREAD_COUNT = maxThreadCount;
		taskFactory = TaskFactory.getInstance();
		taskFactory.clear();
		
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(login, pass);
			}
		});
	}

	public static void main(String[] args) {
		try{
			ApplicationContext ctx = SpringApplication.run(ScrapperTaskRunner.class, args);
			DOMConfigurator.configure("log4j.xml");
			long start = System.currentTimeMillis();
			ScrapperTaskRunner taskRunner = new ScrapperTaskRunner("SuperVIP153051", "v52HVHtisM".toCharArray(),"111.txt", 100, "success_result.csv", false, false, 0, null);
			taskRunner.run();
			System.out.println("Processing file time: " + (System.currentTimeMillis() - start)/1000 + " seconds");

			start = System.currentTimeMillis();
			ResultParser rp = new ResultParser();
			ArrayList<PageTasks> scrappResults = rp.parseResultFile("success_result.csv");
			System.out.println("Parsing result file time: " + (System.currentTimeMillis() - start)/1000 + " seconds");
			//System.out.println(scrappResults);

		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	public void run(){
		try{
			//taskFactory.loadTaskQueue(urlsFilePath);
			File resultFileFile = new File(resultFile);

			log.debug("Append to previous result: " + appendToPrevResult);
			if(appendToPrevResult && resultFileFile.exists()){
				taskFactory.loadTaskQueue(urlsFilePath, resultFile);
			}else{
				taskFactory.loadTaskQueue(urlsFilePath, null, topCountForScan);
			}
			
			saver = new SaverThreadPS(taskFactory, this.resultFile, callback);

			if(scrapResultViaProxy){
				ScrapperThread newThread = null;
				log.debug("Total tasks: "+taskFactory.getTaskQueue().size());
				while(!taskFactory.isTaskFactoryEmpty() || taskFactory.runThreadsCount.get() > 0){
					log.debug("Try to get request from RequestFactory queue.");
					PageTasks tasks = taskFactory.getTask();
					if(null != tasks){
						log.debug("Pending tasts: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getResultQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
						newThread = new ScrapperThread(tasks, taskFactory, proxyFactory);
						newThread.start();
					}
					else{
						try {
							log.debug("Waiting...");
							Thread.sleep(RUNNER_QUEUE_EMPTY_WAIT_TIME);
						} catch (InterruptedException e) {
							log.error("InterruptedException occured during RequestRunner process: ",e);
						}
					}
					saver.saveResult();
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
						if(task instanceof PrTicTask){
							task.setResultAsIs(new ArrayList<String>(Arrays.asList("1:1")));
						}else{
							task.setResultAsIs(new ArrayList<String>(Arrays.asList("1")));
						}
					}
					taskFactory.putTaskInSuccessQueue(pageTask);
				}
			}
		}
		finally{
			while(taskFactory.runThreadsCount.get() > 0){
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
				}
			}
			saver.saveResult(true);
		}
	}

	public TaskFactory getTaskFactory(){
		return taskFactory;
	}
}
