package com.fdt.imgur.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

public class ImgurTaskFactory {

	private static final Logger log = Logger.getLogger(ImgurTaskFactory.class);

	private static ImgurTaskFactory instance = null;
	private String templateFilePath = "";

	private static Integer MAX_THREAD_COUNT = 100;
	public static Integer MAX_ATTEMP_COUNT = 50;
	protected int runThreadsCount = 0;

	/**
	 * HashMap<process_program,queue_for_process_program>
	 * 
	 */
	private ArrayList<ImgurTask> taskQueue;
	private ArrayList<ImgurTask> successQueue;
	private ArrayList<ImgurTask> errorQueue;
	private ArrayList<ImgurTask> savedTaskList;

	private ArrayList<ImgurPromoTask> promoTaskQueue;
	private ArrayList<ImgurPromoTask> promoSuccessQueue;
	private ArrayList<ImgurPromoTask> promoErrorQueue;
	private ArrayList<ImgurPromoTask> promoSavedTaskList;

	private Random rnd = new Random();

	private ImgurTaskFactory(){
		taskQueue = new ArrayList<ImgurTask>();
		successQueue = new ArrayList<ImgurTask>();
		errorQueue = new ArrayList<ImgurTask>();
		savedTaskList = new ArrayList<ImgurTask>();

		promoTaskQueue = new ArrayList<ImgurPromoTask>();
		promoSuccessQueue = new ArrayList<ImgurPromoTask>();
		promoErrorQueue = new ArrayList<ImgurPromoTask>();
		promoSavedTaskList = new ArrayList<ImgurPromoTask>();
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

	public ArrayList<ImgurTask> getErrorQueue() {
		return errorQueue;
	}

	public synchronized void incRunThreadsCount() {
		runThreadsCount++;
		log.debug("INC thread: " + runThreadsCount);
	}

	public synchronized void decRunThreadsCount(ImgurTask task) {
		runThreadsCount--;
		log.debug("DEC thread: " + runThreadsCount);
		this.notifyAll();
	}

	/**
	 * Singleton for TaskFactory
	 * 
	 * @return
	 */
	public static ImgurTaskFactory getInstance(){
		if(null == instance){
			synchronized (ImgurTaskFactory.class) {
				if(null == instance){
					instance = new ImgurTaskFactory(); 
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
	public synchronized boolean reprocessingTask(ImgurTask task){
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
	public synchronized void putTaskInSuccessQueue(ImgurTask result){
		synchronized (this) {
			successQueue.add(result);
		}
	}

	/**
	 * return Request for running
	 * 
	 * @return
	 */
	public synchronized ImgurTask getTask(){
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
			taskQueue.add(new ImgurTask(file));
		}
	}

	public synchronized void loadPromoFile(File promoFile) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(promoFile);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
				if(!"".equals(line)){
					promoTaskQueue.add(new ImgurPromoTask(line));
				}
			}
		}
		finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while loading promo file", e);
			}
			try {
				if(fr != null)
					fr.close();
			} catch (Throwable e) {
				log.warn("Error while loading promo file", e);
			}
		}
	}

	public synchronized ArrayList<ImgurTask> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<ImgurTask> getSuccessQueue() {
		return successQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount;
	}

	public ArrayList<ImgurTask> getSavedTaskList()
	{
		return savedTaskList;
	}
}
