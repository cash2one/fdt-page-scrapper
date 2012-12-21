package com.fdt.scrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.Constants;
import com.fdt.scrapper.task.NewsTask;

/**
 * @author VarenKoks
 */
public class PosterTaskRunner {
    private static final Logger log = Logger.getLogger(PosterTaskRunner.class);

    protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

    private String proxyFilePath;
    private String keyWordsFilePath;
    private String accListFilePath;
    private int maxThreadCount;
    private long proxyDelay;
    private String resultFile;
    
    private Properties config = new Properties();

    //private ArrayList<Thread> threads = new ArrayList<Thread>();
    
    private final static String PROXY_LOGIN_LABEL = "proxy_login";
    private final static String PROXY_PASS_LABEL = "proxy_pass";
    private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
    private final static String KEY_WORDS_FILE_PATH_LABEL = "key_words_file_path";
    private final static String ACCOUNTS_LIST_FILE_PATH_LABEL = "account_list_file_path";
    private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
    private final static String PROXY_DELAY_LABEL = "proxy_delay";
    private final static String RESULT_FILE_LABEL = "result_file";

    public PosterTaskRunner(String cfgFilePath){
	
	Constants.getInstance().loadProperties(cfgFilePath);
	this.proxyFilePath = Constants.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
	this.keyWordsFilePath = Constants.getInstance().getProperty(KEY_WORDS_FILE_PATH_LABEL);
	this.accListFilePath = Constants.getInstance().getProperty(ACCOUNTS_LIST_FILE_PATH_LABEL);
	this.maxThreadCount = Integer.valueOf(Constants.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
	this.proxyDelay = Integer.valueOf(Constants.getInstance().getProperty(PROXY_DELAY_LABEL));
	this.resultFile = Constants.getInstance().getProperty(RESULT_FILE_LABEL);
	Authenticator.setDefault(new Authenticator() {
	    @Override
	    protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(
			Constants.getInstance().getProperty(PROXY_LOGIN_LABEL), Constants.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
		);
	    }
	});
    }

    public static void main(String[] args) {
	try{
	    PosterTaskRunner taskRunner = new PosterTaskRunner("config.ini");
	    taskRunner.run();
	}catch(Throwable e){
	    e.printStackTrace();
	}
    }

    public void run(){
	synchronized (this) {
	    TaskFactory.MAX_THREAD_COUNT = maxThreadCount;
	    TaskFactory taskFactory = TaskFactory.getInstance();
	    taskFactory.clear();
	    //taskFactory.loadTaskQueue(urlsFilePath);
	    taskFactory.loadTaskQueue(keyWordsFilePath);

	    ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
	    ProxyFactory proxyFactory = ProxyFactory.getInstance();
	    proxyFactory.init(proxyFilePath);

	    //load account list
	    AccountFactory accountFactory = new AccountFactory();
	    accountFactory.fillAccounts(accListFilePath);

	    PosterThread newThread = null;
	    log.debug("Total tasks: "+taskFactory.getTaskQueue().size());
	    while((!taskFactory.isTaskFactoryEmpty() && accountFactory.isCanGetNewAccounts()) || taskFactory.runThreadsCount > 0){
		log.debug("Try to get request from RequestFactory queue.");
		//getting account
		Account account = accountFactory.getAccount();
		NewsTask task = taskFactory.getTask();
		if(null != task && account != null){
		    log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getResultQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
		    newThread = new PosterThread(task, account, taskFactory, proxyFactory, accountFactory);
		    newThread.start();
		}
		else{
		    try {
			this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
		    } catch (InterruptedException e) {
			log.error("InterruptedException occured during RequestRunner process",e);
		    }
		}
	    }

	    log.debug("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.runThreadsCount);
	    log.debug("Success tasks: "+taskFactory.getResultQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());

	    BufferedWriter bufferedWriter = null;

	    //save success tasks
	    try {
		log.debug("Starting saving success results...");
		//Construct the BufferedWriter object
		bufferedWriter = new BufferedWriter(new FileWriter(resultFile,false));
		for(NewsTask task : taskFactory.getResultQueue()){
		    bufferedWriter.write(task.toString());
		    bufferedWriter.newLine();
		}
		log.debug("Success results was saved successfully.");

	    } catch (FileNotFoundException ex) {
		log.error("Error occured during saving sucess result",ex);
	    } catch (IOException ex) {
		log.error("Error occured during saving sucess result",ex);
	    } finally {
		//Close the BufferedWriter
		try {
		    if (bufferedWriter != null) {
			bufferedWriter.flush();
			bufferedWriter.close();
		    }
		} catch (IOException ex) {
		    log.error("Error occured during closing output streams during saving success results",ex);
		}
	    }
	}
    }
    
    public void loadProperties(String cfgFilePath){
	synchronized (this){ 
	    InputStream is = null;
	    try {
		is = new FileInputStream(new File(cfgFilePath));
		config.load(is);
	    } catch (FileNotFoundException e) {
		log.error("Reading PROPERTIES file: FileNotFoundException exception occured: " + e.getMessage());
	    } catch (IOException e) {
		log.error("Reading PROPERTIES file: IOException exception occured: " + e.getMessage());
	    } finally {
		try {
		    is.close();
		} catch (Throwable e) {
		    log.warn("Error while initializtion", e);
		}
	    }
	}
    }
}
