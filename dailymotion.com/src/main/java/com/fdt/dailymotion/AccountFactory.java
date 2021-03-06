package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Autowired;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;

public class AccountFactory
{
	private static final Logger log = Logger.getLogger(AccountFactory.class);
	private HashMap<String, Account> accounts = new HashMap<String, Account>();
	//count of posted news for each account
	private HashMap<String, Integer> newsPostedCount = new HashMap<String, Integer>();
	//count of thread where accounts are used
	private HashMap<String, Integer> accountUsedInThreadCount = new HashMap<String, Integer>();
	
	public final static String MAIN_URL_LABEL = "dailymotion.main_url";
	private final static String LOGIN_URL_LABEL = "dailymotion.login_url";

	private final static String NEWS_PER_ACCOUNT_LABEL = "dailymotion.news_per_account";
	private final static String NOT_REJECT_TIME_LABEL = "dailymotion.not_reject_time";

	private static int NEWS_PER_ACCOUNT = 200;

	private ProxyFactory proxyFactory = null;

	//contain rejected account's login - rejection time
	private Map<String, Long> rejectedAccount = new HashMap<String, Long>();
	private Long NOT_REJECT_TIME = 900000L;

	public AccountFactory(ProxyFactory proxy){
		super();
		this.proxyFactory = proxy;
		NEWS_PER_ACCOUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(NEWS_PER_ACCOUNT_LABEL));
		NOT_REJECT_TIME = Long.valueOf(ConfigManager.getInstance().getProperty(NOT_REJECT_TIME_LABEL, "900000"));
	}

	public void fillAccounts(String accListFilePath) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(new File(accListFilePath));
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				//parse proxy adress
				if(line.contains(";")){
					String[] account = line.trim().split(";");
					accounts.put(account[2], new Account(account[0],account[2],account[1], this));
					newsPostedCount.put(account[2],0);
					accountUsedInThreadCount.put(account[2],0);
				}
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

		log.debug("Total account count: " + accounts.size());
	}

	public boolean loginAccount(Account account){
		//getting cookie for each account
		HttpURLConnection conn = null;
		ProxyConnector proxy = null;
		boolean isTimeout = false;

		try{
			do{
				isTimeout = false;
				try {
					proxy = proxyFactory.getRandomProxyConnector();

					executerequestToGetCookies(ConfigManager.getInstance().getProperty(MAIN_URL_LABEL) + "/ru", "GET", proxy, null, account);
					executerequestToGetCookies( ConfigManager.getInstance().getProperty(MAIN_URL_LABEL) + "/pageitem/authenticationContainer?request=/login?&from_request=%2Fru&_csrf_l=" + account.getCookie("_csrf/link"), "GET", proxy, null, account);

					String postUrl = ConfigManager.getInstance().getProperty(MAIN_URL_LABEL) + ConfigManager.getInstance().getProperty(LOGIN_URL_LABEL);
					URL url = new URL(postUrl);
					HttpURLConnection.setFollowRedirects(false);
					conn = (HttpURLConnection) url.openConnection(proxy.getConnect(proxyFactory.getProxyType()));
					conn.setReadTimeout(60000);
					conn.setConnectTimeout(60000);
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);

					conn.addRequestProperty("Referer","http://www.dailymotion.com/ru");
					conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:18.0) Gecko/20100101 Firefox/18.0"); 
					//conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
					conn.setRequestProperty("Accept", "*/*");
					conn.setRequestProperty("X-Requested-With",	"XMLHttpRequest");
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("form_name", "dm_pageitem_authenticationform"));
					nameValuePairs.add(new BasicNameValuePair("username", account.getEmail()));
					nameValuePairs.add(new BasicNameValuePair("password", account.getPass()));
					nameValuePairs.add(new BasicNameValuePair("_csrf", account.getCookie("_csrf/form")));
					nameValuePairs.add(new BasicNameValuePair("_fid", ""));
					nameValuePairs.add(new BasicNameValuePair("authChoice", "login"));
					nameValuePairs.add(new BasicNameValuePair("from_request", "/RedBull"));

					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
					writer.write(getQuery(nameValuePairs));
					writer.flush();
					writer.close();
					os.close();


					// Execute HTTP Post Request
					Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();
					if(cookies.get("Set-Cookie") == null || (cookies.get("Set-Cookie") != null && cookies.get("Set-Cookie").toString().contains("notexists"))){
						log.error("Can't getting cookies for account.Account doesn't exist: \""+ account.getLogin() + "\" or banned, or error occured during login. Please check email and password.");
						return false;
					}

					for(String cookieOne: cookies.get("Set-Cookie"))
					{
						String cookiesValues[] = cookieOne.split(";");
						for(String cookiesArrayItem : cookiesValues){
							String singleCookei[] = cookiesArrayItem.split("=");
							account.addCookie(singleCookei[0].trim(), singleCookei[1].trim());
						}
					}

					return true;
				} catch(SocketTimeoutException e){
					log.error("Error during login/getting cookies for account",e);
					isTimeout = true;
				} finally{
					if(conn!=null){
						conn.disconnect();
					}
					if(proxy != null){
						proxyFactory.releaseProxy(proxy);
					}
				}
			}while(isTimeout);
			
		}
		catch (Exception e) {
			log.error("Error during login/getting cookies for account",e);
			return false;
		}
		
		return false;
	}

	private void executerequestToGetCookies(String postUrl, String requestMethod, ProxyConnector proxy, String postParams, Account account) throws IOException, XPathExpressionException{

		//post news
		URL url = new URL(postUrl);
		log.info("URL: " + url);
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy.getConnect(proxyFactory.getProxyType()));
		conn.setReadTimeout(60000);
		conn.setConnectTimeout(60000);
		conn.setRequestMethod(requestMethod);
		if(requestMethod.equalsIgnoreCase("POST")){
			conn.setDoOutput(true);
		}else{
			conn.setDoOutput(false);
		}
		conn.setDoInput(true);

		conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
		conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		//conn.setRequestProperty("Cookie", account.getCookies());
		conn.setRequestProperty("Host", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
		conn.setRequestProperty("Referer", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/ru");

		if(requestMethod.equalsIgnoreCase("POST") && postParams != null){
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(postParams);
			writer.flush();
			writer.close();
			os.close();
		}

		Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();

		int code = conn.getResponseCode();

		if(cookies.get("Set-Cookie") != null){
			for(String cookieOne: cookies.get("Set-Cookie"))
			{
				String cookiesValues[] = cookieOne.split(";");
				for(String cookiesArrayItem : cookiesValues){
					String singleCookei[] = cookiesArrayItem.split("=");
					account.addCookie(singleCookei[0].trim(), singleCookei[1].trim());
				}
			}
		}

		conn.disconnect();
	}

	public synchronized Account getAccount()
	{
		Account account = null;

		List<String> notLogged = new ArrayList<String>();

		for(String login : accountUsedInThreadCount.keySet())
		{
			account = null;
			if(!rejectedAccount.containsKey(login)){
				int runningCount = accountUsedInThreadCount.get(login);
				int postedCount = newsPostedCount.get(login);
				if( runningCount < (NEWS_PER_ACCOUNT-postedCount)){
					int currentCount = accountUsedInThreadCount.get(login);
					accountUsedInThreadCount.put(login, ++currentCount);
					log.trace(String.format("Used account ' %s 'size incremented to %d",login, currentCount));
					account = accounts.get(login);
					if(account.isLogged() || loginAccount(account)){
						account.setLogged(true);
						break;
					}else{
						notLogged.add(account.getLogin());
						log.error(String.format("Account '%s' was added to remove list", login));
						continue;
					}
				}
			}
		}

		//remove not logged account
		for(String login : notLogged){
			log.error(String.format("Account '%s' was not logged and will be removed from account list.", login));
			accounts.remove(login);
			newsPostedCount.remove(login);
			accountUsedInThreadCount.remove(login);
			log.warn(String.format("Account '%s' was removed from account list.", login));
		}

		notLogged.clear();

		return account;
	}

	/**
	 * Increment news counter for success news posting and decrement accountUsedInThreadCount.
	 * 
	 * @param account
	 */
	public synchronized void incrementPostedCounter(Account account){
		Integer count = newsPostedCount.get(account.getLogin());
		count++;
		newsPostedCount.put(account.getLogin(), count);
		if(count == NEWS_PER_ACCOUNT){
			accounts.remove(account.getLogin());
		}
		log.trace("Posted account news incremented: " + count);
		releaseAccount(account);
	}

	/**
	 * Release account using
	 * @param account
	 */
	public synchronized void releaseAccount(Account account){
		int count = accountUsedInThreadCount.get(account.getLogin());
		count--;
		accountUsedInThreadCount.put(account.getLogin(), count);
		log.debug("Used account size decremented: " + count);

		//check for account excluding
		if(accountUsedInThreadCount.get(account.getLogin()) == 0 && newsPostedCount.get(account.getLogin()) >= NEWS_PER_ACCOUNT){
			accounts.remove(account.getLogin());
			log.warn(String.format("Account %s was excluded from request at all",account.getLogin()));
		}
	}

	/**
	 * Release account using
	 * @param account
	 */
	public synchronized void markAccountForExclude(Account account){
		//now account will not be return for processing
		newsPostedCount.put(account.getLogin(), NEWS_PER_ACCOUNT);
	}

	/*	public synchronized boolean isCanGetNewAccounts(){
		for(String login : accountUsedInThreadCount.keySet()){
			int runningCount = accountUsedInThreadCount.get(login);
			int postedCount = newsPostedCount.get(login);
			if( runningCount < (NEWS_PER_ACCOUNT-postedCount)){
				return true;
			}
		}
		return false;
	}*/

	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params)
		{
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	public HashMap<String, Account> getAccounts(){
		return accounts;
	}

	public void rejectAccount(Account account){
		if(!rejectedAccount.containsKey(account.getLogin())){
			rejectedAccount.put(account.getLogin(), System.currentTimeMillis());
		}
	}

	public boolean isAccountRejected(Account account){
		long curTime = System.currentTimeMillis();
		if(!rejectedAccount.containsKey(account.getLogin()) || ((rejectedAccount.get(account.getLogin()) + NOT_REJECT_TIME) > curTime) ){
			return false;
		}else{
			return true;
		}
	}
}
