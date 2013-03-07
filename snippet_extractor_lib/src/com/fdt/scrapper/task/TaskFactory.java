package com.fdt.scrapper.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

public class TaskFactory {

	private static final Logger log = Logger.getLogger(TaskFactory.class);

	private static TaskFactory instance = null;

	private static Integer MAX_THREAD_COUNT = 100;
	public static Integer MAX_ATTEMP_COUNT = 50;
	protected int runThreadsCount = 0;

	/**
	 * HashMap<process_program,queue_for_process_program>
	 * 
	 */
	private ArrayList<SnippetTask> taskQueue;
	private ArrayList<SnippetTask> successQueue;
	private ArrayList<SnippetTask> errorQueue;

	private Random rnd = new Random();

	private TaskFactory(){
		taskQueue = new ArrayList<SnippetTask>();
		successQueue = new ArrayList<SnippetTask>();
		errorQueue = new ArrayList<SnippetTask>();
	}

	public void clear(){
		taskQueue.clear();
		successQueue.clear();
		errorQueue.clear();
	}

	public synchronized static Integer getMAX_THREAD_COUNT() {
		return MAX_THREAD_COUNT;
	}

	public synchronized static void setMAX_THREAD_COUNT(Integer mAXTHREADCOUNT) {
		MAX_THREAD_COUNT = mAXTHREADCOUNT;
	}

	public ArrayList<SnippetTask> getErrorQueue() {
		return errorQueue;
	}

	public synchronized void incRunThreadsCount() {
		runThreadsCount++;
		log.debug("INC thread: " + runThreadsCount);
	}

	public synchronized void decRunThreadsCount(SnippetTask task) {
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
	public synchronized boolean reprocessingTask(SnippetTask task){
		synchronized (this) {
			if(task.getAttemptCount() < MAX_ATTEMP_COUNT){
				task.incAttemptCount();
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
	public synchronized void putTaskInSuccessQueue(SnippetTask result){
		synchronized (this) {
			successQueue.add(result);
		}
	}

	/**
	 * return Request for running
	 * 
	 * @return
	 */
	public synchronized SnippetTask getTask(){
		synchronized (this) {
			if(runThreadsCount < MAX_THREAD_COUNT){
				if(!isTaskFactoryEmpty()){
					return taskQueue.remove(rnd.nextInt(taskQueue.size()));
				}
			}
			return null;
		}
	}

	public synchronized boolean isTaskFactoryEmpty() {
		return taskQueue.isEmpty();
	}

	public void loadTaskQueue(String pathToTaskList, String source, String lang) {
		ArrayList<String> keyWordsList = loadKeyWordsList(pathToTaskList);
		fillTaskQueue(keyWordsList, source, lang);
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
					String utf8Line = new String(line.getBytes(),"UTF-8");
					keyWordsList.add(utf8Line.trim());
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

	private synchronized void fillTaskQueue(ArrayList<String> keyWordsList, String source, String lang){
		for(String keyWords : keyWordsList){
			taskQueue.add(initSnippetTask( keyWords, source, lang));
		}
	}
	
	private SnippetTask initSnippetTask(String key, String source, String lang){
		SnippetTask task = null;
		if("google".equals(source.toLowerCase().trim())){
			task = new GoogleSnippetTask(key);
		}
		if("bing".equals(source.toLowerCase().trim())){
			task = new BingSnippetTask(key);
		}
		if("tut".equals(source.toLowerCase().trim())){
			task = new TutSnippetTask(key);
		}
		if("ukrnet".equals(source.toLowerCase().trim())){
			task = new UkrnetSnippetTask(key);
		}
		task.setLanguage(lang);
		return task;
	}

	public synchronized ArrayList<SnippetTask> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<SnippetTask> getSuccessQueue() {
		return successQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount;
	}
}
