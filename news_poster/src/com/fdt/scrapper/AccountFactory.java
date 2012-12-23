package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

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

	public AccountFactory(){
		super();
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
			for(Account account : accounts.values()){
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(Constants.getInstance().getProperty(MAIN_URL_LABEL) + Constants.getInstance().getProperty(LOGIN_URL_LABEL));
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("destination", "/"));
				nameValuePairs.add(new BasicNameValuePair("credential_0", account.getLogin()));
				nameValuePairs.add(new BasicNameValuePair("credential_1", account.getPass()));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				account.setCookie(response.getFirstHeader("Set-Cookie").getValue());
				nameValuePairs.clear();
			}
		} catch (ClientProtocolException e) {
			log.error("Error during filling account from list and getting cookies for account",e);
		} catch (IOException e) {
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
		count = accountUsedInThreadCount.get(account.getLogin());
		count--;
		//releace account
		accountUsedInThreadCount.put(account.getLogin(), count);
		log.debug("Used account size decremented: " + count);
	}

	/**
	 * Release account using
	 * @param account
	 */
	public synchronized void decrementUsedCounter(Account account){
		int count = accountUsedInThreadCount.get(account.getLogin());
		count--;
		accountUsedInThreadCount.put(account.getLogin(), count);
		log.debug("Used account size decremented: " + count);
	}

	public synchronized boolean isCanGetNewAccounts(){
		for(Integer count : newsPostedCount.values()){
			if(count < NEWS_PER_ACCOUNT){
				return true;
			}
		}
		return false;
	}
}
