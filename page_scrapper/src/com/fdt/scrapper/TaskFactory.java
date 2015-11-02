package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fdt.scrapper.task.PageTasks;
import com.fdt.scrapper.util.ResultParser;

public class TaskFactory {

	private static final Logger log = Logger.getLogger(TaskFactory.class);

	private static TaskFactory instance = null;

	public static Integer MAX_THREAD_COUNT = 100;
	public static Integer MAX_ATTEMP_COUNT = 50;
	protected AtomicInteger runThreadsCount = new AtomicInteger(0);

	private AtomicInteger successCount = new AtomicInteger(0);
	private AtomicInteger errorCount = new AtomicInteger(0);
	private int totalCount;

	private static final String pattern = "(http[s]?://)?(www[\\d]{0,1}\\.)?([^/]*)/(.*)";
	private static final String ipPattern="[\\d]{0,3}\\.[\\d]{0,3}\\.[\\d]{0,3}\\.[\\d]{0,3}";

	private ICallback onLoadTaskListener = null;

	/**
	 * HashMap<process_program,queue_for_process_program>
	 * 
	 */
	private ArrayList<PageTasks> taskQueue;
	private ArrayList<PageTasks> successQueue;
	private ArrayList<PageTasks> errorQueue;

	private TaskFactory(){
		taskQueue = new ArrayList<PageTasks>();
		successQueue = new ArrayList<PageTasks>();
		errorQueue = new ArrayList<PageTasks>();
	}

	public void clear(){
		taskQueue.clear();
		successQueue.clear();
		errorQueue.clear();
	}

	public void incRunThreadsCount() {
		runThreadsCount.incrementAndGet();
		log.debug("INC thread: " + runThreadsCount);
	}

	public void decRunThreadsCount() {
		runThreadsCount.decrementAndGet();
		log.debug("DEC thread: " + runThreadsCount);
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
	public boolean reprocessingTask(PageTasks tasks){

		if(tasks.getAttempsCount() < MAX_ATTEMP_COUNT){
			synchronized (taskQueue) {
				tasks.incAttempsCount();
				taskQueue.add(tasks);
				log.info("Task returned to queue for reprocessing: " + tasks.toString());
				taskQueue.notifyAll();
			}
			return true;
		}
		else{
			synchronized (errorQueue) {
				errorCount.incrementAndGet();
				errorQueue.add(tasks);
				log.error("Task was put to error queue: " + tasks.toString());
				errorQueue.notifyAll();
			}
			return false;
		}
	}

	/**
	 * Save request into TaskFactory
	 * 
	 * @param request
	 */
	public void putTaskInSuccessQueue(PageTasks result){
		synchronized (successQueue) {
			successCount.incrementAndGet();
			successQueue.add(result);
			successQueue.notifyAll();
		}
	}

	/**
	 * return Request for running
	 * 
	 * @return
	 */
	public PageTasks getTask(){
		synchronized (taskQueue) {
			if(runThreadsCount.get() < MAX_THREAD_COUNT){
				if(!isTaskFactoryEmpty()){
					return taskQueue.remove(0);
				}
			}
			return null;
		}
	}

	public boolean isTaskFactoryEmpty() {
		return taskQueue.isEmpty();
	}

	public void loadTaskQueue(String pathToTaskList, String successResultFile) {
		loadTaskQueue(pathToTaskList, successResultFile, 0);
	}

	public void loadTaskQueue(String pathToTaskList, String successResultFile, int topCountForScan) {
		ResultParser resultParser = null;
		ArrayList<PageTasks> successResult = new ArrayList<PageTasks>();

		if(successResultFile != null && !"".equals(successResultFile.trim())){
			resultParser = new ResultParser();
			successResult = resultParser.parseResultFile(successResultFile);
		}

		HashMap<String, Domain> domainList = loadDomainsList(pathToTaskList);

		//filter domainList
		log.debug("Skipped task count:" + successResult.size());
		if(successResult.size() > 0){
			for(PageTasks task : successResult){
				String key = task.getDomain().getName();
				if(domainList.containsKey(key)){
					//remove processed domains
					domainList.remove(key);
				}
			}
		}

		log.debug("Task for execution count:" + domainList.size());
		fillTaskQueue(domainList);
		domainList.clear();

		sort(topCountForScan);

		if(onLoadTaskListener != null){
			onLoadTaskListener.callback();
		}
	}

	private  HashMap<String, Domain> loadDomainsList(String cfgFilePath) {
		HashMap<String, Domain> domainList = new HashMap<String, Domain>();
		FileReader fr = null;
		BufferedReader br = null;
		Domain domain = null;
		String line = null;
		try {
			log.debug("Starting load domain list...");
			fr = new FileReader(new File(cfgFilePath));
			br = new BufferedReader(fr);

			Pattern r = Pattern.compile(pattern);
			Pattern ip = Pattern.compile(ipPattern);

			line = br.readLine();
			while(line != null){
				String mainDomain = null;
				String subDomain = null;
				// Now create matcher object.
				Matcher m = r.matcher(line);
				if (m.find()) {
					String fullUrl = m.group(3);
					if(!ip.matcher(fullUrl).find()){
						mainDomain = Domain.extractSecondaryDomain(fullUrl);
						//if sub domain exist
						if(fullUrl.length() > mainDomain.length()){
							subDomain = fullUrl.substring(0,fullUrl.lastIndexOf(mainDomain)-1);
						}
					}
					else{
						mainDomain = fullUrl;
					}

					if(mainDomain != null){
						if(domainList.containsKey(mainDomain)){
							domain = domainList.get(mainDomain);
							domain.incCount();
						}else{
							domain = new Domain(mainDomain,false);
						}
					}

					if(subDomain != null){
						domain.addSubDomain(subDomain);
					}
					domainList.put(domain.getName(), domain);

				} else {
					log.error("Can't extract domain from value: " + domain);
				}
				line = br.readLine();
			}
			log.debug("Domain list LOADED.");
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
		return domainList;
	}

	private  ArrayList<PageTasks> fillTaskQueue(HashMap<String, Domain> domainList){
		synchronized(taskQueue){
			successCount.set(0);

			errorCount.set(0);
			for(Domain domain : domainList.values()){
				taskQueue.add(new PageTasks(domain));
			}
			totalCount = taskQueue.size();
			return taskQueue;
		}
	}

	public void sort(int topCountForScan){
		Collections.sort(taskQueue, new Comparator<PageTasks>(){
			@Override
			public int compare(PageTasks arg0, PageTasks arg1) {
				return arg1.getDomainCount() - arg0.getDomainCount();
			}

		});

		if(topCountForScan > 0){
			ArrayList<PageTasks> newTaskQueue = new ArrayList<PageTasks>();
			int i = 0;
			for(i = 0; i < topCountForScan; i++){
				if(taskQueue.size() > 0){
					newTaskQueue.add(taskQueue.get(i));
				}
			}

			if(taskQueue.size() > 0){
				while(newTaskQueue.get(topCountForScan-1).getDomainCount() == taskQueue.get(++i).getDomainCount()){
					newTaskQueue.add(taskQueue.get(i));
				}
			}

			taskQueue = newTaskQueue;
			totalCount = taskQueue.size();
		}
	}

	public ArrayList<PageTasks> getTaskQueue() {
		return taskQueue;
	}

	public ArrayList<PageTasks> getResultQueue() {
		return successQueue;
	}

	public  void reinitResultQueue() {
		synchronized(successQueue){
			successQueue = new ArrayList<PageTasks>();
		}
	}

	public ArrayList<PageTasks> getErrorQueue() {
		return errorQueue;
	}

	public void reinitErrorQueue() {
		synchronized(errorQueue){
			errorQueue = new ArrayList<PageTasks>();
		}
	}

	public int getRunThreadsCount() {
		return runThreadsCount.get();
	}

	public int getSuccessCount() {
		return successCount.get();
	}

	public int getErrorCount() {
		return errorCount.get();
	}

	public int getTotalCount() {
		return totalCount;
	}

	public ICallback getOnLoadTaskListener() {
		return onLoadTaskListener;
	}

	public void setOnLoadTaskListener(ICallback onLoadTaskListener) {
		this.onLoadTaskListener = onLoadTaskListener;
	}
}
