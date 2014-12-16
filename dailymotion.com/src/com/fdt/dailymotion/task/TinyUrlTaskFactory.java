package com.fdt.dailymotion.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

public class TinyUrlTaskFactory {

	private static final Logger log = Logger.getLogger(TinyUrlTaskFactory.class);

	private static TinyUrlTaskFactory instance = null;
	private String templateFilePath = "";

	private static Integer MAX_THREAD_COUNT = 100;
	public static Integer MAX_ATTEMP_COUNT = 50;
	protected int runThreadsCount = 0;

	/**
	 * HashMap<process_program,queue_for_process_program>
	 * 
	 */
	private ArrayList<TinyUrlTask> taskQueue;
	private ArrayList<TinyUrlTask> successQueue;
	private ArrayList<TinyUrlTask> errorQueue;
	private ArrayList<TinyUrlTask> savedTaskList;

	private Random rnd = new Random();

	private TinyUrlTaskFactory(){
		taskQueue = new ArrayList<TinyUrlTask>();
		successQueue = new ArrayList<TinyUrlTask>();
		errorQueue = new ArrayList<TinyUrlTask>();
		savedTaskList = new ArrayList<TinyUrlTask>();
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

	public String getTemplateFilePath()
	{
		return templateFilePath;
	}

	public void setTemplateFilePath(String templateFilePath)
	{
		this.templateFilePath = templateFilePath;
	}

	public ArrayList<TinyUrlTask> getErrorQueue() {
		return errorQueue;
	}

	public synchronized void incRunThreadsCount() {
		runThreadsCount++;
		log.debug("INC thread: " + runThreadsCount);
	}

	public synchronized void decRunThreadsCount(TinyUrlTask task) {
		runThreadsCount--;
		log.debug("DEC thread: " + runThreadsCount);
		this.notifyAll();
	}

	/**
	 * Singleton for TaskFactory
	 * 
	 * @return
	 */
	public static TinyUrlTaskFactory getInstance(){
		if(null == instance){
			synchronized (TinyUrlTaskFactory.class) {
				if(null == instance){
					instance = new TinyUrlTaskFactory(); 
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
	public synchronized boolean reprocessingTask(TinyUrlTask task){
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
	public synchronized void putTaskInSuccessQueue(TinyUrlTask result){
		synchronized (this) {
			successQueue.add(result);
		}
	}

	/**
	 * return Request for running
	 * 
	 * @return
	 */
	public synchronized TinyUrlTask getTask(){
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

	public synchronized void fillTaskQueue(File[] fileList) throws Exception{
		for(File file : fileList){
			taskQueue.add(new TinyUrlTask(file));
		}
	}

	public synchronized ArrayList<TinyUrlTask> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<TinyUrlTask> getSuccessQueue() {
		return successQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount;
	}

	public ArrayList<TinyUrlTask> getSavedTaskList()
	{
		return savedTaskList;
	}
}
