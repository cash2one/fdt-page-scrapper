package com.fdt.scrapper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;

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

	//private ArrayList<Thread> threads = new ArrayList<Thread>();

	public ScrapperTaskRunner(final String login, final char[] pass, String proxyFilePath, String urlsFilePath, int maxThreadCount, long proxyDelay, String resultFile, boolean scrapResultViaProxy){
		this.proxyFilePath = proxyFilePath;
		this.urlsFilePath = urlsFilePath;
		this.maxThreadCount = maxThreadCount;
		this.proxyDelay = proxyDelay;
		this.resultFile = resultFile;
		this.scrapResultViaProxy = scrapResultViaProxy;
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(login, pass);
			}
		});
	}

	public static void main(String[] args) {
		try{
			ScrapperTaskRunner taskRunner = new ScrapperTaskRunner("EUR102217", "J8Fjh5TN5H".toCharArray(),"proxy.txt","links_small.txt", 1, 5000L, "success_result.csv", true);
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
			TaskFactory taskFactory = TaskFactory.getInstance();
			taskFactory.clear();
			//taskFactory.loadTaskQueue(urlsFilePath);
			taskFactory.loadTaskQueue(urlsFilePath);

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
						task.setResultAsIs("1");
					}
					taskFactory.putTaskInSuccessQueue(pageTask);
				}
			}
			
			BufferedWriter bufferedWriter = null;

			//save success tasks
			try {
				log.debug("Starting saving success results...");
				//Construct the BufferedWriter object
				bufferedWriter = new BufferedWriter(new FileWriter(resultFile,false));
				for(PageTasks tasks : taskFactory.getResultQueue()){
					bufferedWriter.write(tasks.toCsv());
					bufferedWriter.newLine();
				}
				log.debug("Success results was saved successfully.");

			} catch (FileNotFoundException ex) {
				log.error("Error occured during saving sucess result",ex);
			} catch (IOException ex) {
				log.error("Error occured during saving sucess result",ex);
			} finally {
				//Close the BufferedWriter
				try {
					if (bufferedWriter != null) {
						bufferedWriter.flush();
						bufferedWriter.close();
					}
				} catch (IOException ex) {
					log.error("Error occured during closing output streams during saving success results",ex);
				}
			}

			//save success tasks
			try {
				//Construct the BufferedWriter object
				log.debug("Starting saving error results...");
				bufferedWriter = new BufferedWriter(new FileWriter("../errors_links.txt",false));
				for(PageTasks tasks : taskFactory.getErrorQueue()){
					String domainName = tasks.getDomain().getName();
					for(int i = 0; i < tasks.getDomain().getCount(); i++){
						bufferedWriter.write("http://" + domainName + "/");
						bufferedWriter.newLine();
					}
					domainName = "." + domainName;
					for(int i = 0; i < tasks.getDomain().getSubDomainsList().size(); i++){
						String subDomain = tasks.getDomain().getSubDomainsList().get(i).getName();
						for(int j = 0; j < tasks.getDomain().getSubDomainCount(subDomain); j++){
							bufferedWriter.write(subDomain + domainName);
							bufferedWriter.newLine();
						}
					}
				}
				log.debug("Error results was saved successfully.");
			} catch (FileNotFoundException ex) {
				log.error("Error occured during saving error results",ex);
			} catch (IOException ex) {
				log.error("Error occured during saving error results",ex);
			} finally {
				//Close the BufferedWriter
				try {
					if (bufferedWriter != null) {
						bufferedWriter.flush();
						bufferedWriter.close();
					}
				} catch (IOException ex) {
					log.error("Error occured during closing output streams during saving error results",ex);
				}
			}
		}
	}
}
