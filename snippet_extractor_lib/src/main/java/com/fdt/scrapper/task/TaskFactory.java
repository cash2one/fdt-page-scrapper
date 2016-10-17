package com.fdt.scrapper.task;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	private ArrayList<SnippetTaskWrapper> taskQueue;
	private ArrayList<SnippetTaskWrapper> successQueue;
	private ArrayList<SnippetTaskWrapper> errorQueue;

	private Random rnd = new Random();

	private TaskFactory(){
		taskQueue = new ArrayList<SnippetTaskWrapper>();
		successQueue = new ArrayList<SnippetTaskWrapper>();
		errorQueue = new ArrayList<SnippetTaskWrapper>();
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

	public ArrayList<SnippetTaskWrapper> getErrorQueue() {
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
	public synchronized boolean reprocessingTask(SnippetTaskWrapper task){
		synchronized (this) {
			if(task.getAttemptCount() < MAX_ATTEMP_COUNT){
				task.getCurrentTask().incAttemptCount();
				task.selectRandTask();
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
	public synchronized void putTaskInSuccessQueue(SnippetTaskWrapper result){
		synchronized (this) {
			successQueue.add(result);
		}
	}

	/**
	 * return Request for running
	 * 
	 * @return
	 */
	public synchronized SnippetTaskWrapper getTask(){
		synchronized (this) {
			if(runThreadsCount.get() < MAX_THREAD_COUNT.get()){
				if(!isTaskFactoryEmpty()){
					SnippetTaskWrapper task = taskQueue.remove(rnd.nextInt(taskQueue.size()));
					task.selectRandTask();
					return task;
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

	public void loadTaskQueue(String pathToTaskList, String sourcesSrt, int[] frequencies, String lang) throws Exception {
		ArrayList<String> keyWordsList = loadKeyWordsList(pathToTaskList);
		fillTaskQueue(sourcesSrt,frequencies,keyWordsList,lang);
		keyWordsList.clear();
	}
	
	public void loadTaskQueue(ArrayList<String> keyWordsList, String sourcesSrt, int[] frequencies, String lang) throws Exception {
		fillTaskQueue(sourcesSrt,frequencies,keyWordsList,lang);
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
				while(line != null){
					if(!"".equals(line.trim())){
						//String utf8Line = new String(line.getBytes(),"UTF-8");
						keyWordsList.add(line.trim());
					}
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
			taskQueue.add(new SnippetTaskWrapper(makeSnippetTask( keyWords, source, lang)));
		}
	}

	private synchronized void fillTaskQueue(String sourcesSrt, int[] frequencies, ArrayList<String> keyWordsList, String lang) throws Exception{
		for(String keyWords : keyWordsList){
			taskQueue.add(new SnippetTaskWrapper(sourcesSrt, frequencies, keyWords, lang));
		}
	}

	public static SnippetTask makeSnippetTask(String key, String source) throws Exception{
		return makeSnippetTask(key, source, "en");
	}

	public synchronized ArrayList<SnippetTaskWrapper> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<SnippetTaskWrapper> getSuccessQueue() {
		return successQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount.get();
	}
	
	public static SnippetTask makeSnippetTask(String key, String source, String lang) throws Exception{
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
					}
					else
						if("aol".equals(source.toLowerCase().trim())){
							task = new AolSnippetTask(key);
						}
						else
							if("yahoo".equals(source.toLowerCase().trim())){
								task = new YahooSnippetTask(key);
							}else{
								throw new Exception("Can't find assosiated task for source: " + source);
							}
		task.setLanguage(lang);
		task.setSource(source);
		return task;
	}
}
