package com.fdt.scrapper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.fdt.scrapper.task.PageTasks;

/**
 * Thread class for saving results to DB
 * 
 * @author Administrator
 *
 */
public class SaverThreadPS extends Thread
{
	private static final Logger log = Logger.getLogger(SaverThreadPS.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	private TaskFactory taskFactory;
	private String resultFile;
	
	private boolean appendToPrevResult;
	
	private boolean save;
	
	public SaverThreadPS(TaskFactory taskFactory, String resultFile, boolean appendToPrevResult) {
		super();
		this.taskFactory = taskFactory;
		this.resultFile = resultFile;
		this.appendToPrevResult = appendToPrevResult;
	}

	@Override
	public void start(){
		super.start();
	}

	@Override
	public void run() {
		synchronized (this) {
			while(true){	
				BufferedWriter bufferedWriter = null;

				//save success tasks
				try {
					if(taskFactory.getResultQueue().size() > 0){
						log.debug("Starting saving success results...");
						//Construct the BufferedWriter object
						bufferedWriter = new BufferedWriter(new FileWriter(resultFile,appendToPrevResult));

						for(int i = 0; i < taskFactory.getResultQueue().size(); i++){
							PageTasks task = taskFactory.getResultQueue().remove(0);
							bufferedWriter.write(task.toCsv());
							bufferedWriter.newLine();
						}
						log.debug("Success results was saved successfully.");
					}

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

				//save error tasks
				try {
					//Construct the BufferedWriter object
					if(taskFactory.getErrorQueue().size() > 0){
						log.debug("Starting saving error results...");
						bufferedWriter = new BufferedWriter(new FileWriter("../errors_links.txt",appendToPrevResult));
						for(int j = 0; j < taskFactory.getErrorQueue().size(); j++){
							PageTasks task =taskFactory.getErrorQueue().remove(0);
							String domainName = task.getDomain().getName();
							for(int i = 0; i < task.getDomain().getCount(); i++){
								bufferedWriter.write("http://" + domainName + "/");
								bufferedWriter.newLine();
							}
							domainName = "." + domainName;
							for(int i = 0; i < task.getDomain().getSubDomainsList().size(); i++){
								String subDomain = task.getDomain().getSubDomainsList().get(i).getName();
								for(int k = 0; k < task.getDomain().getSubDomainCount(subDomain); k++){
									bufferedWriter.write(subDomain + domainName);
									bufferedWriter.newLine();
								}
							}
						}
						log.debug("Error results was saved successfully.");
					}
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
				try {
					if(!isInterrupted()){
						Thread.currentThread().wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
					}
				} catch (InterruptedException e) {
					interrupt();
					log.error("Error occured during saving error results",e);
				}
				
				if(isInterrupted() && save){
					break;
				}else{
					if(isInterrupted()){
						save = true;
					}
				}
			}
		}
	}
}
