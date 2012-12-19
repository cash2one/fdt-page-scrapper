package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fdt.scrapper.task.NewsTask;

public class TaskFactory {

	private static final Logger log = Logger.getLogger(TaskFactory.class);

	private static TaskFactory instance = null;
	public ArrayList<NewsTask> getErrorQueue() {
		return errorQueue;
	}

	public static Integer MAX_THREAD_COUNT = 100;
	public static Integer MAX_ATTEMP_COUNT = 50;
	protected int runThreadsCount = 0;
	
	private static final String pattern = "(http[s]?://)?(www[\\d]{0,1}\\.)?([^/]*)/(.*)";
	private static final String ipPattern="[\\d]{0,3}\\.[\\d]{0,3}\\.[\\d]{0,3}\\.[\\d]{0,3}";

	/**
	 * HashMap<process_program,queue_for_process_program>
	 * 
	 */
	private ArrayList<NewsTask> taskQueue;
	private ArrayList<NewsTask> resultQueue;
	private ArrayList<NewsTask> errorQueue;

	private TaskFactory(){
		taskQueue = new ArrayList<NewsTask>();
		resultQueue = new ArrayList<NewsTask>();
		errorQueue = new ArrayList<NewsTask>();
	}
	
	public void clear(){
	    taskQueue.clear();
	    resultQueue.clear();
	    errorQueue.clear();
	}

	public synchronized void incRunThreadsCount() {
		runThreadsCount++;
		log.debug("INC thread: " + runThreadsCount);
	}

	public synchronized void decRunThreadsCount(NewsTask task) {
		runThreadsCount--;
		log.debug("DEC thread: " + runThreadsCount);
		this.notifyAll();
	}

	/**
	 * Singleton for TaskFactory
	 * 
	 * @return
	 */
	public static TaskFactory getInstance(){
		if(null == instance){
			synchronized (TaskFactory.class) {
				if(null == instance){
					instance = new TaskFactory(); 
				}
			}
		}
		return instance;
	}

	/**
	 * Save request into TaskFactory
	 * 
	 * @param request
	 */
	public synchronized boolean reprocessingTask(NewsTask task){
		synchronized (this) {
			if(task.getAttempsCount() < MAX_ATTEMP_COUNT){
				task.incAttempsCount();
				taskQueue.add(task);
				log.info("Task returned to queue for reprocessing: " + task.toString());
				return true;
			}
			else{
				errorQueue.add(task);
				log.error("Task was put to error queue: " + task.toString());
				return false;
			}
		}
	}

	/**
	 * Save request into TaskFactory
	 * 
	 * @param request
	 */
	public synchronized void putTaskInSuccessQueue(NewsTask result){
		synchronized (this) {
			resultQueue.add(result);
		}
	}

	/**
	 * return Request for running
	 * 
	 * @return
	 */
	public synchronized NewsTask getTask(){
		synchronized (this) {
			if(runThreadsCount < MAX_THREAD_COUNT){
				if(!isTaskFactoryEmpty()){
					return taskQueue.remove(0);
				}
			}
			return null;
		}
	}

	public synchronized boolean isTaskFactoryEmpty() {
		return taskQueue.isEmpty();
	}

	public void loadTaskQueue(String pathToTaskList, String login, String password) {
		ArrayList<String> keyWordsList = loadKeyWordsList(pathToTaskList);
		fillTaskQueue(keyWordsList,login,password);
		keyWordsList.clear();
	}

	/**
	 * 
	 * @param cfgFilePath
	 * @return <key_words><>
	 */
	private synchronized ArrayList<String> loadKeyWordsList(String cfgFilePath) {
		ArrayList<String> keyWordsList = new ArrayList<String>();
		synchronized (this){ 
			FileReader fr = null;
			BufferedReader br = null;
			String line = null;
			try {
				fr = new FileReader(new File(cfgFilePath));
				br = new BufferedReader(fr);

				line = br.readLine();
				while(line != null){
					keyWordsList.add(line.trim());
					line = br.readLine();
				}
			} catch (FileNotFoundException e) {
				log.error("Reading PROPERTIES file: FileNotFoundException exception occured",e);
			} catch (IOException e) {
				log.error("Reading PROPERTIES file: IOException exception occured",e);
			} finally {
				try {
					if(br != null)
						br.close();
				} catch (Throwable e) {
					log.warn("Error while initializtion", e);
				}
				try {
					if(fr != null)
						fr.close();
				} catch (Throwable e) {
					log.warn("Error while initializtion", e);
				}
			}
		}
		return keyWordsList;
	}

	private synchronized void fillTaskQueue(ArrayList<String> keyWordsList,String login, String password){
		for(String keyWords : keyWordsList){
			taskQueue.add(new NewsTask(login, password, keyWords, "CONTENT MUST BE HERE"));
		}
	}

	public synchronized ArrayList<NewsTask> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<NewsTask> getResultQueue() {
		return resultQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount;
	}
}
