package com.fdt.emonsite.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.fdt.emonsite.scrapper.task.Constants;
import com.fdt.scrapper.proxy.ProxyFactory;

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

	public AccountFactory(ProxyFactory proxy){
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
		if(accountUsedInThreadCount.containsKey(account.getLogin())){
			Integer count = newsPostedCount.get(account.getLogin());
			count++;
			newsPostedCount.put(account.getLogin(), count);
			log.debug("Posted account news incremented: " + count);
			releaseAccount(account);
		}
	}

	/**
	 * Release account using
	 * @param account
	 */
	public synchronized void releaseAccount(Account account){
		if(accountUsedInThreadCount.containsKey(account.getLogin())){
			int count = accountUsedInThreadCount.get(account.getLogin());
			count--;
			accountUsedInThreadCount.put(account.getLogin(), count);
			log.debug("Used account size decremented: " + count);
		}
	}

	public void resetPostedCount(){
		for(String login:newsPostedCount.keySet()){
			newsPostedCount.put(login, 0);
		}
	}
	
	public int getAccountCount(){
		return accounts.size();
	}
	
	public void deleteAccount(Account account){
		if(accountUsedInThreadCount.containsKey(account.getLogin())){
			accountUsedInThreadCount.remove(account.getLogin());
		}
		if(accounts.containsKey(account.getLogin())){
			accounts.remove(account.getLogin());
		}
		if(newsPostedCount.containsKey(account.getLogin())){
			newsPostedCount.remove(account.getLogin());
		}
	}
}
