/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.Random;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;

/**
 *
 * @author Administrator
 */
public class VoteRunner{

	private static final Logger log = Logger.getLogger(VoteRunner.class);
	private static final Logger logNew = Logger.getLogger("com.fdt.scrapper.NewsPoster");

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	protected static Long WAIT_TIME_SUCCESS = 60000L;
	protected static Long WAIT_TIME = 33137L;

	private String proxyFilePath;
	private String accListFilePath;
	private Properties config = new Properties();

	private ProxyFactory proxyFactory;

	private Random rnd = new Random();

	private String ID = "19";
	
	private static String[] USER_AGENTS = new String[]{"Opera/9.25 (Windows NT 5.1; U; ru",
	    "Opera/9.26 (Windows NT 5.1; U; ru)",
	    "Opera/9.20 (Windows NT 5.1; U; ru)",
	    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; MathPlayer 2.10b; .NET CLR 2.0.50727; .NET CLR 1.1.4322)",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
	    "Opera/9.52 (Windows NT 5.1; U; ru)",
	    "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.0.7) Gecko/20060909 Firefox/1.5.0.7",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)",
	    "Mozilla/5.0 (X11; U; FreeBSD i386; en-US; rv:1.8.0.9) Gecko/20070204 Firefox/1.5.0.9",
	    "Mozilla/4.0 (compatible; MSIE 5.5; Windows 98; Win 9x 4.90) ",
	    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; InfoPath.2) ",
	    "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.6) Gecko/20040206 Firefox/0.8 Mnenhy/0.6.0.103 ",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; ru) Opera 8.50 ",
	    "Opera/9.0 (Windows NT 5.1; U; en) ",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 1.1.4322) ",
	    "Mozilla/1.22 (compatible; MSIE 2.0d; Windows NT) ",
	    "Opera/9.52 (Windows NT 6.0; U; ru) ",
	    "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1 ",
	    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506) ",
	    "Mozilla/5.0 (X11; I; Linux 2.6.22-gentoo-r8 x86_64) Gecko/20071115 Firefox/2.0.0.10 ",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; MRA 5.0 (build 02094)) ",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) ",
	    "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1 ",
	    "Mozilla/5.0 (compatible; Refer.Ru; +http://www.refer.ru) ",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; FREE; .NET CLR 1.1.4322) ",
	    "Opera/8.54 (Windows NT 5.1; U; en) ",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30) ",
	    "Opera/9.02 (Windows NT 5.1; U; ru) ",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; Q312461) ",
	    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727) ",
	    "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4"};
	
	private static String userAgent = USER_AGENTS[0];

	//private ArrayList<Thread> threads = new ArrayList<Thread>();

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String ACCOUNTS_LIST_FILE_PATH_LABEL = "accounts_file_path";

	public VoteRunner(String cfgFilePath){
		ConfigManager.getInstance().loadProperties(cfgFilePath);
		this.proxyFilePath = ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.accListFilePath = ConfigManager.getInstance().getProperty(ACCOUNTS_LIST_FILE_PATH_LABEL);

		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL), ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
						);
			}
		});
	}

	public static void main(String[] args) {
		try{
			VoteRunner taskRunner = new VoteRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
			taskRunner.run();
			System.out.print("Program execution finished successfully");
		}catch(Throwable e){
			log.error("Error during main stream",e);
			e.printStackTrace();
			System.out.print("Program execution finished with errors");
		}
	}


	public void run(){
		try{
			synchronized (this) {


				ProxyFactory.DELAY_FOR_PROXY = 20000L; 
				proxyFactory = ProxyFactory.getInstance();
				proxyFactory.init(proxyFilePath);

				//load account list
				AccountFactory accountFactory = new AccountFactory(proxyFactory);
				accountFactory.fillAccounts(accListFilePath);

				Account account = accountFactory.getAccount();

				while(account != null){
					ID = "19";
					/*int rndValue = rnd.nextInt(7);
				    if(rndValue == 0 || rndValue == 1){
					ID = "9";
				    }*/userAgent = USER_AGENTS[rnd.nextInt(USER_AGENTS.length)];
					getCookie(account);
					if(account.getCookie() == null || "".equals(account.getCookie().trim())){
						accountFactory.releaseAccount(account);
						account = accountFactory.getAccount();
						continue;
					}
					signIn(account);
					openPage(account);
					String resultVote = vote(account);

					if("Ваш голос принят!".equals(resultVote)){
						//wait ~15 min
						System.out.println("Start waiting after success...");
						wait(WAIT_TIME_SUCCESS + rnd.nextInt(112314));
					}else{
						System.out.println("Start waiting after error...");
						wait(WAIT_TIME);
					}
					accountFactory.releaseAccount(account);
					account = accountFactory.getAccount();
				}

				//TaskFactory.setMAX_THREAD_COUNT(1);
				/*while((!taskFactory.isTaskFactoryEmpty() && ((account = accountFactory.getAccount()) != null)) || taskFactory.runThreadsCount > 0){
		    if(taskFactory.getSuccessQueue().size() >= 3){
			TaskFactory.setMAX_THREAD_COUNT(maxThreadCount);
		    }
		    log.debug("Try to get request from RequestFactory queue.");
		    log.debug("Account: " + account);
		    if(account != null){
			NewsTask task = taskFactory.getTask();
			log.debug("Task: " + task);
			if(task != null){
			    log.debug("Pending tasks: " + taskFactory.getTaskQueue().size()+ ". Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());
			    newThread = new PosterThread(task, account, taskFactory, proxyFactory, accountFactory);
			    newThread.start();
			    continue;
			}else{
			    accountFactory.releaseAccount(account);
			}
		    }
		    try {
			this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
		    } catch (InterruptedException e) {
			log.error("InterruptedException occured during RequestRunner process",e);
		    }
		}

		log.debug("Task factory is empty: "+taskFactory.isTaskFactoryEmpty()+". Current working threads count is " + taskFactory.runThreadsCount);
		log.debug("Success tasks: "+taskFactory.getSuccessQueue().size()+". Error tasks: " + taskFactory.getErrorQueue().size());

		BufferedWriter bufferedWriter = null;

		//save success tasks
		try {
		    log.debug("Starting saving success results...");
		    //Construct the BufferedWriter object
		    bufferedWriter = new BufferedWriter(new FileWriter(resultFile,false));
		    for(NewsTask task : taskFactory.getSuccessQueue()){
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
		    }*/
			}
		}
		catch (InterruptedException e) {
			log.error(e);
		}finally{
			try{
			}catch(Throwable e){
				log.error(e);
			}
			try{
			}catch(Throwable e){
				log.error(e);
			}

		}
	}

	public void getCookie(Account account){
		//getting cookie for each account
		try {
			//String postUrl = ConfigManager.getInstance().getProperty(MAIN_URL_LABEL) + ConfigManager.getInstance().getProperty(LOGIN_URL_LABEL);
			String postUrl = ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL);
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(account.getProxyConnector().getConnect("HTTP"));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("Host","beautifulplace.by");
			conn.addRequestProperty("User-Agent",userAgent);
			conn.addRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.addRequestProperty("Referer","http://beautifulplace.by/");
			conn.addRequestProperty("Connection","keep-alive");
			conn.addRequestProperty("Content-Type","application/x-www-form-urlencoded");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			/*nameValuePairs.add(new BasicNameValuePair("destination", "/"));
		nameValuePairs.add(new BasicNameValuePair("credential_0", account.getLogin()));
		nameValuePairs.add(new BasicNameValuePair("credential_1", account.getPass()));*/

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(AccountFactory.getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();

			String cookieValue = "";
			// Execute HTTP Post Request
			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();
			if(cookies != null) {
				if(cookies.get("Set-Cookie") != null && cookies.get("Set-Cookie").toString().contains("notexists")){
					log.error("Account doesn't exist: \""+ account.getLogin() + "\". Please check email and password.");
				}

				if(cookies.get("Set-Cookie") != null){
					for(String cookieOne: cookies.get("Set-Cookie"))
					{
						if(cookieOne.contains("29247347af66cd4c162d459012dd90e4")){
							cookieValue = cookieOne;
						}
					}
				}
			}

			account.setCookie(cookieValue);

			HtmlCleaner cleaner = new HtmlCleaner();
			InputStream is = conn.getInputStream();

			TagNode responceBody = cleaner.clean(is,"UTF-8");
			Object[] formName = responceBody.evaluateXPath("//form/fieldset/input[5]/@name");
			if(formName != null && formName.length > 0){
				account.setFormName(formName[0].toString());
			}
			if(is != null){
				is.close();
			}

			conn.disconnect();
		} catch (ClientProtocolException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (IOException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (XPathExpressionException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		}
		catch (XPatherException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		}
	}

	public void signIn(Account account){
		//getting cookie for each account
		try {
			account.setProxyConnector(proxyFactory.getProxyConnector());
			String postUrl = ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + ConfigManager.getInstance().getProperty(AccountFactory.LOGIN_URL_LABEL);
			//String postUrl = ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL);
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(account.getProxyConnector().getConnect("HTTP"));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("Host","beautifulplace.by");
			conn.addRequestProperty("User-Agent",userAgent);
			conn.addRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.addRequestProperty("Referer","http://beautifulplace.by/");
			conn.addRequestProperty("Connection","keep-alive");
			conn.addRequestProperty("Content-Type","application/x-www-form-urlencoded");
			conn.addRequestProperty("Cookie",account.getCookie());

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username",account.getLogin()));
			nameValuePairs.add(new BasicNameValuePair("password",account.getPass()));
			nameValuePairs.add(new BasicNameValuePair("Submit","Войти"));
			nameValuePairs.add(new BasicNameValuePair("option","com_users"));
			nameValuePairs.add(new BasicNameValuePair("task","user.login"));
			nameValuePairs.add(new BasicNameValuePair("return","aW5kZXgucGhwP0l0ZW1pZD0xMDE="));
			nameValuePairs.add(new BasicNameValuePair(account.getFormName(),"1"));

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(AccountFactory.getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();

			int rescCode = conn.getResponseCode();

			conn.disconnect();
		} catch (ClientProtocolException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (IOException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (XPathExpressionException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		}
	}

	public String vote(Account account){
		//getting cookie for each account
		try {


			String postUrl = "http://www.beautifulplace.by/articles?&task=vote_article&id="+ID;
			//String postUrl = ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL);
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(account.getProxyConnector().getConnect("HTTP"));


			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("Host","www.beautifulplace.by");
			conn.addRequestProperty("User-Agent",userAgent);
			conn.addRequestProperty("Accept","*/*");
			conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.addRequestProperty("X-Requested-With","XMLHttpRequest");
			conn.addRequestProperty("Referer","http://www.beautifulplace.by/articles?&task=article&id="+ID);
			conn.addRequestProperty("Connection","keep-alive");
			conn.addRequestProperty("Pragma","no-cache");
			conn.addRequestProperty("Cache-Control","no-cache");
			conn.addRequestProperty("Cookie",account.getCookie());

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			/*nameValuePairs.add(new BasicNameValuePair("&task","vote_article"));
	    nameValuePairs.add(new BasicNameValuePair("id","19"));*/

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(AccountFactory.getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();

			InputStream is = conn.getInputStream();
			BufferedReader br = new BufferedReader( new InputStreamReader(is,"UTF-8"));

			StringBuilder sb = new StringBuilder();

			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			} 

			if(is != null){
				is.close();
			}

			conn.disconnect();

			//release proxy
			proxyFactory.releaseProxy(account.getProxyConnector());
			account.setProxyConnector(proxyFactory.getProxyConnector());
			String msg = "ID="+ID+"|"+account.getLogin()+":"+account.getPass()+"|"+account.getProxyConnector().getProxyKey()+"|"+sb.toString().trim();
			logNew.info(msg);
			System.out.println(msg);

			return sb.toString().trim();
		} catch (ClientProtocolException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (IOException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (XPathExpressionException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		}
		return "";
	}

	public void openPage(Account account){
		//getting cookie for each account
		try {
			String postUrl = "http://www.beautifulplace.by/articles?&task=article&id="+ID;
			//String postUrl = ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL);
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(account.getProxyConnector().getConnect("HTTP"));


			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			conn.addRequestProperty("Host","beautifulplace.by");
			conn.addRequestProperty("User-Agent",userAgent);
			conn.addRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.addRequestProperty("Referer","http://beautifulplace.by/");
			conn.addRequestProperty("Connection","keep-alive");
			conn.addRequestProperty("Content-Type","application/x-www-form-urlencoded");
			conn.addRequestProperty("Cookie",account.getCookie());

			conn.connect();

			int rescCode = conn.getResponseCode();
			rescCode = conn.getResponseCode();

			conn.disconnect();
		} catch (ClientProtocolException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (IOException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (XPathExpressionException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
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
