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
import com.fdt.scrapper.task.NewsTask;

/**
 * @author VarenKoks
 */
public class PosterTaskRunner {
	private static final Logger log = Logger.getLogger(PosterTaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	private String proxyFilePath;
	private String urlsFilePath;
	private int maxThreadCount;
	private long proxyDelay;
	private String resultFile;
	private String siteLogin;
	private String sitePass;

	//private ArrayList<Thread> threads = new ArrayList<Thread>();

	public PosterTaskRunner(final String login, final char[] pass, String proxyFilePath, String urlsFilePath, int maxThreadCount, long proxyDelay, String resultFile, String siteLogin, String sitePass){
		this.proxyFilePath = proxyFilePath;
		this.urlsFilePath = urlsFilePath;
		this.maxThreadCount = maxThreadCount;
		this.proxyDelay = proxyDelay;
		this.resultFile = resultFile;
		this.siteLogin = siteLogin;
		this.sitePass = sitePass;
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(login, pass);
			}
		});
	}

	public static void main(String[] args) {
		try{
			PosterTaskRunner taskRunner = new PosterTaskRunner("VIPUAoVrs68fdmb", "TC3aH96sAR".toCharArray(),"proxy.txt","keywords.txt", 1, 5000L, "success_result.csv","udryfgtsukry@yopmail.com","lol200");
			taskRunner.run();

			/*ResultParser rp = new ResultParser();
			ArrayList<PageTasks> scrappResults = rp.parseResultFile("success_result.csv");
			System.out.println(scrappResults);*/

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
			taskFactory.loadTaskQueue(urlsFilePath, siteLogin, sitePass);

			ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
			ProxyFactory proxyFactory = ProxyFactory.getInstance();
			proxyFactory.init(proxyFilePath);

			PosterThread newThread = null;
			log.debug("Total tasks: "+taskFactory.getTaskQueue().size());
			while(!taskFactory.isTaskFactoryEmpty() || taskFactory.runThreadsCount > 0){
				log.debug("Try to get request from RequestFactory queue.");
				NewsTask task = taskFactory.getTask();
				if(null != task){
				    	log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getResultQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
					newThread = new PosterThread(task, taskFactory, proxyFactory);
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

			BufferedWriter bufferedWriter = null;

			//save success tasks
			try {
				log.debug("Starting saving success results...");
				//Construct the BufferedWriter object
				bufferedWriter = new BufferedWriter(new FileWriter(resultFile,false));
				for(NewsTask task : taskFactory.getResultQueue()){
					bufferedWriter.write(task.toString());
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
		}
	}
}
