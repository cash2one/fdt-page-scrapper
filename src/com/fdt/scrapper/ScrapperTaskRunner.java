package com.fdt.scrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.PageTasks;
import com.fdt.scrapper.task.Task;
import com.fdt.scrapper.util.ResultParser;

/**
 * @author VarenKoks
 */
public class ScrapperTaskRunner {
	private static final Logger log = Logger.getLogger(ScrapperTaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 100L;

	private String proxyFilePath;
	private String urlsFilePath;
	private int maxThreadCount;
	private long proxyDelay;
	private String resultFile;

	public ScrapperTaskRunner(final String login, final char[] pass, String proxyFilePath, String urlsFilePath, int maxThreadCount, long proxyDelay, String resultFile){
		this.proxyFilePath = proxyFilePath;
		this.urlsFilePath = urlsFilePath;
		this.maxThreadCount = maxThreadCount;
		this.proxyDelay = proxyDelay;
		this.resultFile = resultFile;
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(login, pass);
			}
		});
	}

	public static void main(String[] args) {
		try{
			ScrapperTaskRunner taskRunner = new ScrapperTaskRunner("VIPUAoVrs68fdmb", "TC3aH96sAR".toCharArray(),"proxy.txt","links_small.txt", 1, 30000L, "success_result.csv");
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

			ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
			ProxyFactory proxyFactory = ProxyFactory.getInstance();
			proxyFactory.init(proxyFilePath);

			ScrapperThread newThread = null;
			while(!taskFactory.isTaskFactoryEmpty() || taskFactory.runThreadsCount > 0){
				log.debug("Try to get request from RequestFactory queue.");
				PageTasks tasks = taskFactory.getTask();
				if(null != tasks){
					newThread = new ScrapperThread(tasks, taskFactory, proxyFactory);
					newThread.start();
				}
				else{
					try {
						this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
					} catch (InterruptedException e) {
						log.error("InterruptedException occured during RequestRunner process: " + e.getMessage());
					}
				}
			}

			try {
				if(newThread != null && newThread.isAlive())
					newThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			BufferedWriter bufferedWriter = null;

			//save success tasks
			try {
				//Construct the BufferedWriter object
				bufferedWriter = new BufferedWriter(new FileWriter(resultFile,false));
				for(PageTasks tasks : taskFactory.getResultQueue()){
					bufferedWriter.write(tasks.toCsv());
					bufferedWriter.newLine();
				}

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				//Close the BufferedWriter
				try {
					if (bufferedWriter != null) {
						bufferedWriter.flush();
						bufferedWriter.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			//save success tasks
			try {
				//Construct the BufferedWriter object
				bufferedWriter = new BufferedWriter(new FileWriter("errors_links.txt",false));
				for(PageTasks tasks : taskFactory.getErrorQueue()){
					String domainName = tasks.getDomain().getName();
					for(int i = 0; i < tasks.getDomain().getCount(); i++){
						bufferedWriter.write(domainName);
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
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				//Close the BufferedWriter
				try {
					if (bufferedWriter != null) {
						bufferedWriter.flush();
						bufferedWriter.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
