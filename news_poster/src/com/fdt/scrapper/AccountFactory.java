package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
	private ArrayList<Account> accounts = new ArrayList<Account>();
	//count of posted news for each account
	private ArrayList<Integer> newsPostedCount = new ArrayList<Integer>();
	//count of thread where accounts are used
	private ArrayList<Integer> accountUsedInThreadCount = new ArrayList<Integer>();

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
					accounts.add(new Account(account[0],account[1],account[2]));
					newsPostedCount.add(0);
					accountUsedInThreadCount.add(0);
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
			for(Account account : accounts){
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized Account getAccount(){
		int index = 0;
		for(Integer count : accountUsedInThreadCount){
			if(count < NEWS_PER_ACCOUNT){
				int currentCount = accountUsedInThreadCount.get(index);
				accountUsedInThreadCount.set(index, ++currentCount);
				return accounts.get(index);
			}
			index++;
		}
		return null;
	}

	/**
	 * Increment news counter for success news posting and decrement accountUsedInThreadCount.
	 * 
	 * @param account
	 */
	public synchronized void incrementCounter(Account account){
		int index = accounts.indexOf(account);
		Integer count = newsPostedCount.get(index);
		count++;
		newsPostedCount.set(index, count);
		count = accountUsedInThreadCount.get(index);
		count--;
		accountUsedInThreadCount.set(index, count);
	}

	public synchronized boolean isCanGetNewAccounts(){
		for(Integer count : newsPostedCount){
			if(count < NEWS_PER_ACCOUNT){
				return true;
			}
		}
		return false;
	}
}
