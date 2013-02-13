package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.Constants;

public class AccountFactory
{
	private static final Logger log = Logger.getLogger(AccountFactory.class);
	private HashMap<String, Account> accounts = new HashMap<String, Account>();
	//count of posted news for each account
	private HashMap<String, Integer> newsPostedCount = new HashMap<String, Integer>();
	//count of thread where accounts are used
	private HashMap<String, Integer> accountUsedInThreadCount = new HashMap<String, Integer>();

	public final static String MAIN_URL_LABEL = "main_url";
	private final static String LOGIN_URL_LABEL = "login_url";
	public final static String POST_NEWS_URL_LABEL = "post_news_url";
	private final static String NEWS_PER_ACCOUNT_LABEL = "news_per_account";

	private static int NEWS_PER_ACCOUNT = 200;

	private ProxyFactory proxyFactory = null;

	public AccountFactory(ProxyFactory proxy){
		super();
		this.proxyFactory = proxy;
		NEWS_PER_ACCOUNT = Integer.valueOf(Constants.getInstance().getProperty(NEWS_PER_ACCOUNT_LABEL));
	}

	public void fillAccounts(String accListFilePath){
		//read account list
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(new File(accListFilePath));
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				//parse proxy adress
				if(line.contains(":")){
					String[] account = line.trim().split(":");
					accounts.put(account[0], new Account(account[0],account[1],account[2]));
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
		//getting cookie for each account
		try {
			ArrayList<Account> accountToRemove = new ArrayList<Account>();
			ProxyConnector proxy = proxyFactory.getProxyConnector();
			for(Account account : accounts.values()){
				String postUrl = Constants.getInstance().getProperty(MAIN_URL_LABEL) + Constants.getInstance().getProperty(LOGIN_URL_LABEL);
				URL url = new URL(postUrl);
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy.getConnect());
				conn.setReadTimeout(60000);
				conn.setConnectTimeout(60000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
				conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
				conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("destination", "/"));
				nameValuePairs.add(new BasicNameValuePair("credential_0", account.getLogin()));
				nameValuePairs.add(new BasicNameValuePair("credential_1", account.getPass()));

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(NewsPoster.getQuery(nameValuePairs));
				writer.flush();
				writer.close();
				os.close();

				// Execute HTTP Post Request
				Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();
				if(cookies.get("Set-Cookie").toString().contains("notexists")){
					log.error("Account doesn't exist: \""+ account.getLogin() + "\". Please check email and password.");
					accountToRemove.add(account);
					continue;
				}

				String cookieValue = "";
				for(String cookieOne: cookies.get("Set-Cookie"))
				{
					if(cookieOne.contains("SSO_login")){
						cookieValue = cookieOne;
					}
				}

				account.setCookie(cookieValue);
				conn.disconnect();
			}
			proxyFactory.releaseProxy(proxy);

			for(Account account : accountToRemove){
				accounts.remove(account.getLogin());
				newsPostedCount.remove(account.getLogin());
				accountUsedInThreadCount.remove(account.getLogin());
			}
		} catch (ClientProtocolException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (IOException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (XPathExpressionException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		}
	}

	public synchronized Account getAccount(){
		for(String login : accountUsedInThreadCount.keySet()){
			int runningCount = accountUsedInThreadCount.get(login);
			int postedCount = newsPostedCount.get(login);
			if( runningCount < (NEWS_PER_ACCOUNT-postedCount)){
				int currentCount = accountUsedInThreadCount.get(login);
				accountUsedInThreadCount.put(login, ++currentCount);
				log.debug("Used account size incremented: " + currentCount);
				return accounts.get(login);
			}
		}
		return null;
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
		log.debug("Posted account news incremented: " + count);
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
}
