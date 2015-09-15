package com.fdt.scrapper.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class TaskFactory {

	private static final Logger log = Logger.getLogger(TaskFactory.class);

	private static TaskFactory instance = null;

	private static AtomicInteger MAX_THREAD_COUNT = new AtomicInteger(100);
	public static Integer MAX_ATTEMP_COUNT = 50;
	protected AtomicInteger runThreadsCount = new AtomicInteger(0);

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

	public static Integer getMAX_THREAD_COUNT() {
		return MAX_THREAD_COUNT.get();
	}

	public static void setMAX_THREAD_COUNT(Integer mAXTHREADCOUNT) {
		MAX_THREAD_COUNT.set(mAXTHREADCOUNT);
	}

	public ArrayList<SnippetTask> getErrorQueue() {
		return errorQueue;
	}

	public void incRunThreadsCount() {
		runThreadsCount.incrementAndGet();
		log.debug("INC thread: " + runThreadsCount);
	}

	public void decRunThreadsCount(SnippetTask task) {
		runThreadsCount.decrementAndGet();
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
			if(runThreadsCount.get() < MAX_THREAD_COUNT.get()){
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

	public void loadTaskQueue(String pathToTaskList, String source, String lang) throws Exception {
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
			BufferedReader br = null;
			String line = null;
			try {
				br = new BufferedReader(new InputStreamReader( new FileInputStream(cfgFilePath), "UTF8" ));

				line = br.readLine();
				while(line != null && !"".equals(line.trim())){
					//String utf8Line = new String(line.getBytes(),"UTF-8");
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
			}
		}
		return keyWordsList;
	}

	private synchronized void fillTaskQueue(ArrayList<String> keyWordsList, String source, String lang) throws Exception{
		for(String keyWords : keyWordsList){
			taskQueue.add(initSnippetTask( keyWords, source, lang));
		}
	}
	
	private SnippetTask initSnippetTask(String key, String source, String lang) throws Exception{
		SnippetTask task = null;
		if("google".equals(source.toLowerCase().trim())){
			task = new GoogleSnippetTask(key);
		} else
		if("bing".equals(source.toLowerCase().trim())){
			task = new BingSnippetTask(key);
		} else
		if("tut".equals(source.toLowerCase().trim())){
			task = new TutSnippetTask(key);
		} else
		if("ukrnet".equals(source.toLowerCase().trim())){
			task = new UkrnetSnippetTask(key);
		}else{
			throw new Exception("Can't find assosiated task for source: " + source);
		}
		task.setLanguage(lang);
		task.setSource(source);
		return task;
	}

	public synchronized ArrayList<SnippetTask> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<SnippetTask> getSuccessQueue() {
		return successQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount.get();
	}
}
