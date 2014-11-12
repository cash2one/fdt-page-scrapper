package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.dailymotion.task.NewsTask;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;

/**
 * @author VarenKoks
 */
public class TaskRunner {
	private static final Logger log = Logger.getLogger(TaskRunner.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;
	
	public final static String MAIN_URL_LABEL = "main_url";

	private String proxyFilePath;
	private String keyWordsFilePath;
	private String listFilePath;
	private String accListFilePath;
	private int maxThreadCount;
	private long proxyDelay;

	private Properties config = new Properties();

	//private ArrayList<Thread> threads = new ArrayList<Thread>();

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String KEY_WORDS_FILE_PATH_LABEL = "key_words_file_path";
	private final static String ACCOUNTS_LIST_FILE_PATH_LABEL = "account_list_file_path";
	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";


	private final static String LIST_FILE_PATH_LABEL = "list_file_path";


	private ArrayList<String> linksList = new ArrayList<String>();

	public TaskRunner(String cfgFilePath){

		Constants.getInstance().loadProperties(cfgFilePath);
		this.proxyFilePath = Constants.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.keyWordsFilePath = Constants.getInstance().getProperty(KEY_WORDS_FILE_PATH_LABEL);
		this.listFilePath = Constants.getInstance().getProperty(LIST_FILE_PATH_LABEL);
		this.accListFilePath = Constants.getInstance().getProperty(ACCOUNTS_LIST_FILE_PATH_LABEL);
		this.maxThreadCount = Integer.valueOf(Constants.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
		this.proxyDelay = Integer.valueOf(Constants.getInstance().getProperty(PROXY_DELAY_LABEL));

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
			TaskRunner taskRunner = new TaskRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
			taskRunner.run();
			System.out.print("Program execution finished successfully");
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}


	public void run(){
		try{
			synchronized (this) {
				ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
				ProxyFactory proxyFactory = ProxyFactory.getInstance();
				proxyFactory.init(proxyFilePath);

				//load account list
				AccountFactory accountFactory = new AccountFactory(proxyFactory);
				accountFactory.fillAccounts(accListFilePath);

				//load links from file
				//TODO this.linksList= loadLinkList(listFilePath) ;

				Account account = null;
				
				//TODO Loop of accounts
				try {
					NewsPoster nPoster = new NewsPoster(new NewsTask(""), proxyFactory.getProxyConnector().getConnect(), accountFactory.getAccounts().get(0));
					nPoster.executePostNews();
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}finally{
			
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

	public synchronized ArrayList<String> loadLinkList(String cfgFilePath){
		ArrayList<String> linkList = new ArrayList<String>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(new File(cfgFilePath));
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				String utf8Line = new String(line.getBytes(),"UTF-8");
				linkList.add(utf8Line.trim());
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.error("Reading PROPERTIES file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading PROPERTIES file: IOException exception occured", e);
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
		return linkList;
	}
}
