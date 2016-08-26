package com.fdt.scrapper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.fdt.scrapper.task.PageTasks;

/**
 * Thread class for saving results to DB
 * 
 * @author Administrator
 *
 */
public class SaverThreadPS
{
	private static final Logger log = Logger.getLogger(SaverThreadPS.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 2000L;

	private TaskFactory taskFactory;
	private String resultFile;

	private ICallback callback;

	public SaverThreadPS(TaskFactory taskFactory, String resultFile, ICallback callback) {
		super();
		this.taskFactory = taskFactory;
		this.resultFile = resultFile;
		this.callback = callback;
	}

	public void saveResult(){
		saveResult(false);
	}

	public void saveResult(boolean flushResult){
		if(taskFactory.getResultQueue().size() >= 10 || flushResult){
			log.debug(String.format("Saving results. Count of success task: %s; Flush: %s", taskFactory.getResultQueue().size(),flushResult));
			flushResults();
		}
	}

	private void flushResults() {
		BufferedWriter bufferedWriter = null;

		ArrayList<PageTasks> successQueue;
		ArrayList<PageTasks> errorQueue;

		synchronized (taskFactory.getResultQueue()) {
			successQueue = taskFactory.getResultQueue();
			taskFactory.reinitResultQueue();
		}
		
		synchronized (taskFactory.getErrorQueue()) {
			errorQueue = taskFactory.getErrorQueue();
			taskFactory.reinitErrorQueue();
		}

		//save success tasks
		log.debug("Success tasks: "+successQueue.size());
		try {
			log.debug("Starting saving success results...");
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(resultFile,true));
			int savedCount = 0;
			for(PageTasks task : successQueue){
				bufferedWriter.write(task.toCsv());
				bufferedWriter.newLine();
				savedCount++;
			}

			successQueue.clear();

			log.debug("Success results was saved successfully. Saved: " + savedCount);

		} catch (FileNotFoundException ex) {
			log.error("Error occured during saving sucess result",ex);
		} catch (IOException ex) {
			log.error("Error occured during saving sucess result",ex);
		} finally {
			if(callback != null){
				callback.callback();
			}
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
					bufferedWriter = null;
				}
			} catch (IOException ex) {
				log.error("Error occured during closing output streams during saving success results",ex);
			}
		}

		//save error tasks
		log.debug("Error tasks: " + errorQueue.size());
		try {
			//Construct the BufferedWriter object
			if(errorQueue.size() > 0){
				log.debug("Starting saving error results...");
				bufferedWriter = new BufferedWriter(new FileWriter("../errors_links.txt",true));
				int savedCount = 0;
				for(PageTasks task :  errorQueue)
				{
					String domainName = task.getDomain().getName();
					/*for(int i = 0; i < task.getDomain().getCount(); i++)
					{
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
					}*/
					bufferedWriter.write("http://" + domainName + "/");
					bufferedWriter.newLine();
					savedCount++;
				}
				errorQueue.clear();

				log.debug("Error results was saved successfully. Saved: " + savedCount);
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
					bufferedWriter = null;
				}
			} catch (IOException ex) {
				log.error("Error occured during closing output streams during saving error results",ex);
			}
		}
	}
}
