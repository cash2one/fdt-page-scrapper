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

import com.fdt.scrapper.task.PageTasks;
import com.fdt.scrapper.util.ResultParser;

public class TaskFactory {

	private static final Logger log = Logger.getLogger(TaskFactory.class);

	private static TaskFactory instance = null;
	public ArrayList<PageTasks> getErrorQueue() {
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
	private ArrayList<PageTasks> taskQueue;
	private ArrayList<PageTasks> resultQueue;
	private ArrayList<PageTasks> errorQueue;

	private TaskFactory(){
		taskQueue = new ArrayList<PageTasks>();
		resultQueue = new ArrayList<PageTasks>();
		errorQueue = new ArrayList<PageTasks>();
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

	public synchronized void decRunThreadsCount(PageTasks tasks) {
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
	public synchronized boolean reprocessingTask(PageTasks task){
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
	public synchronized void putTaskInSuccessQueue(PageTasks result){
		synchronized (this) {
			resultQueue.add(result);
		}
	}

	/**
	 * return Request for running
	 * 
	 * @return
	 */
	public synchronized PageTasks getTask(){
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

	public void loadTaskQueue(String pathToTaskList, String successResultFile) {
		
		ResultParser resultParser = null;
		ArrayList<PageTasks> successResult = new ArrayList<PageTasks>();
		
		if(successResultFile != null && !"".equals(successResultFile.trim())){
			resultParser = new ResultParser();
			successResult = resultParser.parseResultFile(successResultFile);
		}
		
		HashMap<String, Domain> domainList = loadDomainsList(pathToTaskList);
		
		//filter domainList
		if(successResult.size() > 0){
			for(PageTasks task : successResult){
				String key = task.getDomain().getName();
				if(domainList.containsKey(key)){
					domainList.remove(key);
				}
			}
		}
		
		fillTaskQueue(domainList);
		domainList.clear();
	}

	private synchronized HashMap<String, Domain> loadDomainsList(String cfgFilePath) {
		HashMap<String, Domain> domainList = new HashMap<String, Domain>();
		synchronized (this){ 
			FileReader fr = null;
			BufferedReader br = null;
			Domain domain = null;
			String line = null;
			try {
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
								domain = new Domain(mainDomain);
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
		return domainList;
	}

	private synchronized ArrayList<PageTasks> fillTaskQueue(HashMap<String, Domain> domainList){
		for(Domain domain : domainList.values()){
			taskQueue.add(new PageTasks(domain));
		}
		
		return taskQueue;
	}

	public synchronized ArrayList<PageTasks> getTaskQueue() {
		return taskQueue;
	}

	public synchronized ArrayList<PageTasks> getResultQueue() {
		return resultQueue;
	}

	public synchronized int getRunThreadsCount() {
		return runThreadsCount;
	}
}
