package com.fdt.jimbo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

public class AccountFactory
{
	private static final Logger log = Logger.getLogger(AccountFactory.class);
	private HashMap<String, Account> accounts = new HashMap<String, Account>();
	//count of posted news for each account
	private HashMap<String, Integer> newsPostedCount = new HashMap<String, Integer>();
	//count of thread where accounts are used
	private HashMap<String, Integer> accountUsedInThreadCount = new HashMap<String, Integer>();

	private final static String NEWS_PER_ACCOUNT_LABEL = "news_per_account";

	private static int NEWS_PER_ACCOUNT = 200;
	
	private ProxyFactory proxyFactory;
	
	private ConfigManager configManager;
	
	//contain rejected account's login - rejection time
	private Map<String, Long> rejectedAccount = new HashMap<String, Long>();

	public AccountFactory(ProxyFactory proxyFactory, ConfigManager configManager){
		this.proxyFactory = proxyFactory;
		this.configManager = configManager;
	}

	public void fillAccounts(String accListFilePath) throws Exception{
		
		//read account list
		NEWS_PER_ACCOUNT = Integer.valueOf(configManager.getProperty(NEWS_PER_ACCOUNT_LABEL));
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
					accounts.put(account[0], new Account(account[3],account[0],account[1], account[2], account[4], this));
					newsPostedCount.put(account[0],0);
					accountUsedInThreadCount.put(account[0],0);
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

	public boolean loginAccount(Account account, ProxyConnector proxy){
		//getting cookie for each account
		HttpURLConnection conn = null;

		long srtTm = System.currentTimeMillis();

		try {
			String cstok = executerequestToGetParams("http://a.jimdo.com/app/auth/signin/authenticate", "GET", new IResultExtractor() {
				private String responseStr;

				public void init(String responseStr){
					this.responseStr = responseStr;
				}

				@Override
				public String getResult() {
					Document html = Jsoup.parse(responseStr.toString());

					String cstok = html.select("input[name=cstok]").get(0).attr("value");

					return cstok;
				}
			}, null, account, proxy.getConnect(proxyFactory.getProxyType()));

			String postUrl = "http://a.jimdo.com/app/auth/signin/authenticate";
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(false);
			conn = (HttpURLConnection) url.openConnection(proxy.getConnect(proxyFactory.getProxyType()));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);


			conn.addRequestProperty("Host", "a.jimdo.com");
			//conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.addRequestProperty("Referer", "http://a.jimdo.com/app/auth/signin/authenticate"); 
			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			//conn.addRequestProperty("Cookie","PHPSESSID=" + account.getCookie("PHPSESSID")); 

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("cstok", cstok));
			nameValuePairs.add(new BasicNameValuePair("safemode", "0"));
			nameValuePairs.add(new BasicNameValuePair("popup", "0"));
			nameValuePairs.add(new BasicNameValuePair("page", ""));
			nameValuePairs.add(new BasicNameValuePair("url", account.getLogin()));
			nameValuePairs.add(new BasicNameValuePair("passwd", account.getPass()));

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();

			int rescpCode = conn.getResponseCode();
			
			StringBuilder responseStr = Utils.getResponseAsString(conn);
			
			File respFile = new File("responce.html");
			respFile.delete();
			Utils.appendStringToFile(responseStr.toString(), respFile);

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
					account.addCookie(singleCookei[0].trim(), singleCookei.length > 1?singleCookei[1].trim():"");
				}
			}

			String newLocation = cookies.get("Location") != null?cookies.get("Location").get(0):null;

			while(newLocation != null && !"".equals(newLocation.trim()))
			{
				if("/app/cms/notavailable".equals(newLocation))
				{
					throw new LoginBannedException(String.format("This website %s is not available right now. Account[%s] Proxy[%s]", account.getSite(), account.getLogin(), proxy.toString())); 
				}
				newLocation = executerequestToGetCookies(newLocation, "GET", proxy, null, account);
			}

			return true;
		} catch (LoginBannedException e) {
			account.setLoginErr(false);
			log.error("Account is banned.",e);
			return false;
		}catch (Exception e) {
			account.setLoginErr(true);
			log.error("Error during login/getting cookies for account",e);
			return false;
		}finally{
			log.debug(String.format("Time spent to login account '%s' is %dms", account.getLogin(), (System.currentTimeMillis()-srtTm)/1));
			if(conn!=null){
				conn.disconnect();
			}
		}
	}

	private String executerequestToGetParams(String postUrl, String requestMethod, IResultExtractor resultExtractor, String postParams, Account account, Proxy proxy) throws IOException{
		HttpURLConnection conn = null;
		//post news
		try{
			URL url = new URL(postUrl);
			log.info("URL: " + url);
			HttpURLConnection.setFollowRedirects(false);
			//TODO Uncomment
			//HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
			conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod(requestMethod);
			conn.setDoInput(true);

			if(requestMethod.equalsIgnoreCase("POST")){
				conn.setDoOutput(true);
			}else{
				conn.setDoOutput(false);
			}

			conn.addRequestProperty("Host","a.jimdo.com");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.addRequestProperty("DNT","1");


			//conn.setRequestProperty("Cookie", account.getCookies());
			//conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			//conn.setRequestProperty("Host", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
			//conn.setRequestProperty("Referer", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			if(requestMethod.equalsIgnoreCase("POST") && postParams != null){
				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(postParams);
				writer.flush();
				writer.close();
				os.close();
			}

			int respCode = conn.getResponseCode();


			StringBuilder responseStr = Utils.getResponseAsString(conn);

			// Execute HTTP Post Request
			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();
			if(cookies.get("Set-Cookie") == null || (cookies.get("Set-Cookie") != null && cookies.get("Set-Cookie").toString().contains("notexists"))){
				log.error("Can't getting cookies for account.Account doesn't exist: \""+ account.getLogin() + "\" or banned, or error occured during login. Please check email and password.");
			}

			for(String cookieOne: cookies.get("Set-Cookie"))
			{
				String cookiesValues[] = cookieOne.split(";");
				for(String cookiesArrayItem : cookiesValues){
					String singleCookei[] = cookiesArrayItem.split("=");
					account.addCookie(singleCookei[0].trim(), singleCookei.length > 1?singleCookei[1].trim():"");
				}
			}

			//log.debug(responseStr.toString());

			if(resultExtractor != null){
				resultExtractor.init(responseStr.toString());
			}
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return resultExtractor != null ? resultExtractor.getResult():"";
	}

	private String executerequestToGetCookies(String postUrl, String requestMethod, ProxyConnector proxy, String postParams, Account account) throws IOException, XPathExpressionException{

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
		conn.setRequestProperty("Cookie", account.getCookies());
		//conn.setRequestProperty("Host", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
		//conn.setRequestProperty("Referer", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/ru");

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
					if(account.getCookie(singleCookei[0]) == null){
						account.addCookie(singleCookei[0].trim(), singleCookei.length > 1?singleCookei[1].trim():"");
					}
				}
			}
		}

		conn.disconnect();

		//return Location for 302 responce
		return cookies.get("Location") != null?cookies.get("Location").get(0):null;
	}

	public synchronized Account getAccount()
	{
		Account account = null;

		for(String login : accountUsedInThreadCount.keySet())
		{
			account = null;

			if(!rejectedAccount.containsKey(login))
			{
				int runningCount = accountUsedInThreadCount.get(login);
				int postedCount = newsPostedCount.get(login);

				if( runningCount < (NEWS_PER_ACCOUNT-postedCount))
				{
					int currentCount = accountUsedInThreadCount.get(login);
					accountUsedInThreadCount.put(login, ++currentCount);
					log.debug(String.format("Used account '%s' size incremented to %d",login, currentCount));
					account = accounts.get(login);
					break;
				}
			}
		}

		return account;
	}

	public synchronized void removeNotLoggedAccount(Account account){
		String login = account.getLogin();
		log.error(String.format("Account '%s' was not logged and will be removed from account list.", login));
		Account removed = accounts.remove(login);
		newsPostedCount.remove(login);
		accountUsedInThreadCount.remove(login);

		if(removed != null){
			Utils.appendStringToFile(removed.toString(), new File("account_banned.txt"));
		}

		log.warn(String.format("Account '%s' was removed from account list.", login));
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
		log.debug(String.format("News was posted for account %s. Total posted news for current account is: %d", account.getLogin(), count));
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
			log.warn(String.format("MAX news count for account were posted ( news were posted). Account %s was excluded from request at all",newsPostedCount.get(account.getLogin()), account.getLogin()));
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

	public boolean isAccountRejected(Account account)
	{
		if(!rejectedAccount.containsKey(account.getLogin()) )
		{
			return false;
		}else{
			return true;
		}
	}
}
