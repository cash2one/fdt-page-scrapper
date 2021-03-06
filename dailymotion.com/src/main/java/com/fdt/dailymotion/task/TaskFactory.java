package com.fdt.dailymotion.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.utils.Utils;

public class TaskFactory {

	private static final Logger log = Logger.getLogger(TaskFactory.class);

	private static TaskFactory instance = null;
	private String templateFilePath = "";

	private static Integer MAX_THREAD_COUNT = 100;
	public static Integer MAX_ATTEMP_COUNT = 50;
	protected int runThreadsCount = 0;

	/**
	 * HashMap<process_program,queue_for_process_program>
	 * 
	 */
	private ArrayList<NewsTask> taskQueue;
	private ArrayList<NewsTask> successQueue;
	private ArrayList<NewsTask> errorQueue;
	private ArrayList<NewsTask> savedTaskList;

	private Random rnd = new Random();

	private TaskFactory(){
		taskQueue = new ArrayList<NewsTask>();
		successQueue = new ArrayList<NewsTask>();
		errorQueue = new ArrayList<NewsTask>();
		savedTaskList = new ArrayList<NewsTask>();
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

	public ArrayList<NewsTask> getErrorQueue() {
		return errorQueue;
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
			successQueue.add(result);
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
					return taskQueue.remove(rnd.nextInt(taskQueue.size()));
				}
			}
			return null;
		}
	}

	public synchronized boolean isTaskFactoryEmpty() {
		return taskQueue.isEmpty();
	}

	public synchronized void fillTaskQueue(File[] fileList, File templateFile, String shortUrlsList, boolean isUseImageFromLinkOnly, File[] randImgFiles, boolean isUsePreview) throws Exception{
		for(File file : fileList){
			taskQueue.add(new NewsTask(file, templateFile, shortUrlsList, isUseImageFromLinkOnly, randImgFiles, isUsePreview));
		}
	}
	
	public synchronized void fillTaskQueue(File file, File templateFile, String shortUrlsList, File pregenFile) throws Exception{
		for(String line : loadFileAsStrList(file)){
			taskQueue.add(new NewsTask(line, templateFile, shortUrlsList, pregenFile));
		}
	}
	
	private List<String> loadFileAsStrList(File cfgFile){
		ArrayList<String> linkList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader( new FileInputStream(cfgFile), "UTF8" ));

			String line = br.readLine();

			while(line != null)
			{

				if( !"".equals(line.trim()))
				{
					linkList.add(line.trim());
				}

				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.error("Reading file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
		return linkList;
	}

	public synchronized ArrayList<NewsTask> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<NewsTask> getSuccessQueue() {
		return successQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount;
	}

	public ArrayList<NewsTask> getSavedTaskList()
	{
		return savedTaskList;
	}
}
