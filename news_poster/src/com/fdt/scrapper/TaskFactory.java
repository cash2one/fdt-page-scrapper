package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.fdt.scrapper.task.Constants;
import com.fdt.scrapper.task.NewsTask;

public class TaskFactory {

    private static final Logger log = Logger.getLogger(TaskFactory.class);

    private static TaskFactory instance = null;
    private String templateFilePath = "";

    private static Integer MAX_THREAD_COUNT = 100;
    public static Integer MAX_ATTEMP_COUNT = 50;
    protected int runThreadsCount = 0;

    private Template bottomTemplate;

    private static final String ORG_APACHE_VELOCITY_RUNTIME_LOG_NULL_LOG_SYSTEM = "org.apache.velocity.runtime.log.NullLogSystem";
    private static final String RUNTIME_LOG_LOGSYSTEM_CLASS = "runtime.log.logsystem.class";

    private final static String NEWS_CONTENT_TEMPLATE_FILE_PATH_LABEL = "news_content_template_file_path";

    private final static String PROMO_URL_START_LABEL = "promo_url_start";
    private final static String PROMO_URL_END_LABEL = "promo_url_end";
    private final static String PROMO_URL_START_LABEL_2 = "promo_url_start_2";
    private final static String PROMO_URL_END_LABEL_2 = "promo_url_end_2";
    private final static String IMAGE_URL_LABEL = "image_url";

    private final static String FAKE_IMAGE_URL_LABEL = "fake_image_url";

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
	this.bottomTemplate = Velocity.getTemplate(Constants.getInstance().getProperty(NEWS_CONTENT_TEMPLATE_FILE_PATH_LABEL), "UTF8");
    }

    public void clear(){
	taskQueue.clear();
	successQueue.clear();
	errorQueue.clear();
    }

    public Template getBottomTemplate()
    {
	return bottomTemplate;
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

    public void loadTaskQueue(String pathToTaskList) {
	ArrayList<String> keyWordsList = loadKeyWordsList(pathToTaskList);
	fillTaskQueue(keyWordsList,false);
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

    private synchronized void fillTaskQueue(ArrayList<String> keyWordsList){
	fillTaskQueue(keyWordsList, false);
    }

    private synchronized void fillTaskQueue(ArrayList<String> keyWordsList, boolean loadToSuccessQueue){
	VelocityContext content = generateTemplateContent();
	VelocityContext content2 = generateTemplateContent();

	content.put("PROMO_URL_START", Constants.getInstance().getProperty(PROMO_URL_START_LABEL));
	content.put("PROMO_URL_END", Constants.getInstance().getProperty(PROMO_URL_END_LABEL));
	content2.put("PROMO_URL_START", Constants.getInstance().getProperty(PROMO_URL_START_LABEL_2));
	content2.put("PROMO_URL_END", Constants.getInstance().getProperty(PROMO_URL_END_LABEL_2));
	boolean flag = true;
	for(String keyWords : keyWordsList){
	    if(!loadToSuccessQueue){
		if(flag){
		    taskQueue.add(new NewsTask(keyWords, content));
		}else{
		    taskQueue.add(new NewsTask(keyWords, content2));	
		}
		flag = !flag;
	    }else{
		//load links from file
		NewsTask task = new NewsTask("", null);
		task.setResult(keyWords);
		successQueue.add(task);
	    }
	}
    }

    private VelocityContext generateTemplateContent(){
	//disable velocity log
	Properties props = new Properties();
	props.setProperty(RUNTIME_LOG_LOGSYSTEM_CLASS,ORG_APACHE_VELOCITY_RUNTIME_LOG_NULL_LOG_SYSTEM);
	Velocity.init(props);
	VelocityContext vc = new VelocityContext();
	//init context
	vc.put("IMAGE_URL", Constants.getInstance().getProperty(IMAGE_URL_LABEL));
	vc.put("FAKE_IMAGE_URL", Constants.getInstance().getProperty(FAKE_IMAGE_URL_LABEL));
	return vc;
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
