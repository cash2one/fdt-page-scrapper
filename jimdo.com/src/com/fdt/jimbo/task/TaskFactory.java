package com.fdt.jimbo.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.filemapping.RowMapping;
import com.fdt.filemapping.RowMappingData;

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

	public synchronized void fillTaskQueue(
			File[] fileList, 
			RowMapping rowMapping, 
			File templateFile, 
			File templateFileWOPic, 
			List<String> randImgUrlList, 
			List<String> rndTitles, 
			List<String> jpgUrlList, 
			List<String> buttonUrlList) throws Exception
			{
		RowMappingData data;

		for(File file : fileList)
		{
			if(file.isFile()){
				data = new RowMappingData(file, rowMapping);
				taskQueue.add(
						new NewsTask(
								file, 
								data, 
								randImgUrlList.get(rnd.nextInt(randImgUrlList.size())),
								rndTitles.get(rnd.nextInt(rndTitles.size())),
								templateFile,
								templateFileWOPic,
								jpgUrlList.get(rnd.nextInt(jpgUrlList.size())),
								buttonUrlList.get(rnd.nextInt(buttonUrlList.size()))
								) 
						);
			}
		}

			}

	private ArrayList<String> readFile(File file) throws IOException{

		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<String> fileTitleList = new ArrayList<String>();

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				fileTitleList.add(line.trim());
				line = br.readLine();
			}

			//fileTitleArray = fileTitleList.toArray(new String[fileTitleList.size()]);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
			}
			try {
				if(fr != null)
					fr.close();
			} catch (Throwable e) {
			}
		}

		return fileTitleList;
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
