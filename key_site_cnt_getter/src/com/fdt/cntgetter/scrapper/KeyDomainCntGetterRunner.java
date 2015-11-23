package com.fdt.cntgetter.scrapper;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.IResultFormatter;
import com.fdt.scrapper.SaverThread;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.scrapper.task.TaskFactory;

/**
 *
 * @author Administrator
 */
public class KeyDomainCntGetterRunner{
	private static final String LANG_LABEL = "lang";

	private static final String SOURCE_LABEL = "source";

	private static final Logger log = Logger.getLogger(KeyDomainCntGetterRunner.class);

	private String proxyFilePath;
	private String keyWordsFilePath;
	private int maxThreadCount;
	private long proxyDelay;

	private String source = null;
	private String lang = null;
	private int[] frequencies = null;
	
	private Boolean isCountDomain = false;

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";

	protected final static String KEY_WORDS_FILE_PATH_LABEL = "key_words_file_path";
	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	
	private final static String COUNT_DOMAIN_LABEL = "count_domain";
	
	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	Random rnd = new Random();

	private TaskFactory taskFactory;
	private ProxyFactory proxyFactory;
	
	private SaverThread saver;

	/**
	 * args[0] - language
	 * args[1] - path to config file
	 */
	public static void main(String[] args){
		if(args.length < 1){
			System.out.print("Not enought arguments....");
		}else{
			System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
			ConfigManager.getInstance().loadProperties(args[0]);
			System.out.println(args[0]);
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
							ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL),
							ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
							);
				}
			});
			DOMConfigurator.configure("log4j.xml");
			
			KeyDomainCntGetterRunner taskRunner = null;
			try {
				taskRunner = new KeyDomainCntGetterRunner(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			taskRunner.execute();
		}
	}

	public KeyDomainCntGetterRunner(String cfgFilePath) throws Exception{

		ConfigManager.getInstance().loadProperties(cfgFilePath);
		this.taskFactory = TaskFactory.getInstance();
		this.proxyFilePath = ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.keyWordsFilePath = ConfigManager.getInstance().getProperty(KEY_WORDS_FILE_PATH_LABEL);
		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
		this.proxyDelay = Integer.valueOf(ConfigManager.getInstance().getProperty(PROXY_DELAY_LABEL));

		this.source = ConfigManager.getInstance().getProperty(SOURCE_LABEL);
		this.lang = ConfigManager.getInstance().getProperty(LANG_LABEL);
		
		frequencies = new int[]{1};

		//init task factory
		TaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
		taskFactory = TaskFactory.getInstance();
		taskFactory.clear();
		
		taskFactory.loadTaskQueue(keyWordsFilePath, source, frequencies, lang);

		//init proxy factory
		ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(proxyFilePath);
		
		String countDomainValue = ConfigManager.getInstance().getProperty(COUNT_DOMAIN_LABEL);
		if(countDomainValue != null && !"".equals(countDomainValue.trim())){
			isCountDomain = Boolean.valueOf(countDomainValue);
		}
		
		//run saver thread
		//TODO Get output file from config
		saver = new SaverThread(
				taskFactory, 
				ConfigManager.getInstance().getProperty(KEY_WORDS_FILE_PATH_LABEL), 
				new IResultFormatter() {
					@Override
					public String formatResult(SnippetTask task) {
						StringBuffer strBuf = new StringBuffer();
						strBuf.append(task.getResult()).append(":").append(isCountDomain?task.getKeyWordsOrig().substring(5):task.getKeyWordsOrig());
						return strBuf.toString();
					}
				},
				"res_output.txt");
		
		saver.start();

		//set authentication params
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL), ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
						);
			}
		});
	}

	/*public static void main(String[] args) {
		try{
			PosterTaskRunner taskRunner = new PosterTaskRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
			taskRunner.run();
			System.out.print("Program execution finished successfully");
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}*/


	public void execute(){
		try{
			synchronized (this) {
				ExecutorService executor = Executors.newFixedThreadPool(taskFactory.getMAX_THREAD_COUNT());
				
				KeyDomainCntGetterThread newThread = null;
				log.info("Total tasks: "+taskFactory.getTaskQueue().size());

				while(!taskFactory.isTaskFactoryEmpty() || taskFactory.getRunThreadsCount() > 0){
					log.debug("Try to get request from RequestFactory queue.");
					SnippetTaskWrapper task = taskFactory.getTask();
					log.debug("Task: " + task);
					if(task != null){
						log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
						
						newThread = new KeyDomainCntGetterThread(task, proxyFactory, taskFactory, isCountDomain);
						executor.submit(newThread);
						continue;
					}
					try {
						this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
					} catch (InterruptedException e) {
						log.error("InterruptedException occured during RequestRunner process",e);
					}
				}
				executor.shutdown();
				saver.stopped = true;
				while(saver.isAlive()){
					try {
						this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					saver.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				log.info("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.getRunThreadsCount());
				log.info("Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
			}
		}finally{
		}
	}
}
