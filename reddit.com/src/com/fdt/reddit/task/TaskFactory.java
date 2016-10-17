package com.fdt.reddit.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.fdt.filemapping.RowMappingData;
import com.fdt.reddit.Account;
import com.fdt.reddit.AccountFactory;

public class TaskFactory {

	private static final Logger log = Logger.getLogger(TaskFactory.class);

	private static TaskFactory instance = null;
	private String templateFilePath = "";

	private static Integer MAX_THREAD_COUNT = 100;
	public static Integer MAX_ATTEMP_COUNT = 50;
	protected AtomicInteger runThreadsCount = new AtomicInteger(0);

	/**
	 * HashMap<process_program,queue_for_process_program>
	 * 
	 */
	private ArrayList<RedditTask> taskQueue;
	private ArrayList<RedditTask> successQueue;
	private ArrayList<RedditTask> errorQueue;
	private ArrayList<RedditTask> savedTaskList;

	private Random rnd = new Random();

	private TaskFactory(){
		taskQueue = new ArrayList<RedditTask>();
		successQueue = new ArrayList<RedditTask>();
		errorQueue = new ArrayList<RedditTask>();
		savedTaskList = new ArrayList<RedditTask>();
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

	public ArrayList<RedditTask> getErrorQueue() {
		return errorQueue;
	}

	public void incRunThreadsCount() {
		int threadCount = runThreadsCount.incrementAndGet();
		log.debug("INC thread: " + threadCount);
	}

	public void decRunThreadsCount(RedditTask task) {
		int threadCount = runThreadsCount.decrementAndGet();
		log.debug("DEC thread: " + threadCount);
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
	public synchronized boolean reprocessingTask(RedditTask task){
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
	public synchronized void putTaskInSuccessQueue(RedditTask result){
		synchronized (this) {
			successQueue.add(result);
		}
	}

	/**
	 * return Request for running
	 * 
	 * @return
	 */
	public synchronized RedditTask getTask(){
		synchronized (this) {
			if(runThreadsCount.get() < MAX_THREAD_COUNT){
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

	public synchronized void fillTaskQueue(	List<String> inputStrs) throws Exception
	{
		for(String str : inputStrs)
		{
			if(str != null && !"".equals(str.trim())){
				String values[] = str.split(";");
				if(values.length < 2){
					throw new Exception(String.format("Invalid input string: '%s'", str));
				}
				taskQueue.add(new RedditTask(values[0],values[1]));
			}
		}

	}

	public synchronized ArrayList<RedditTask> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<RedditTask> getSuccessQueue() {
		return successQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount.get();
	}

	public ArrayList<RedditTask> getSavedTaskList()
	{
		return savedTaskList;
	}
}
