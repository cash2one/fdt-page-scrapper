package com.fdt.scrapper;

import java.util.ArrayList;

import com.fdt.scrapper.task.Constants;

public class AccountFactory
{
    private ArrayList<Account> accounts = new ArrayList<Account>();
    //count of posted news for each account
    private ArrayList<Integer> newsPostedCount = new ArrayList<Integer>();
    //count of thread where accounts are used
    private ArrayList<Integer> accountUsedInThreadCount = new ArrayList<Integer>();
    
    public void fillAccounts(String accListFilePath){
	//TODO read account list
	//TODO getting cookie for each account
    }
    
    public Account getAccount(){
	int index = 0;
	for(Integer count : accountUsedInThreadCount){
	    if(count < Constants.NEWS_PER_ACCOUNT){
		int currentCount = accountUsedInThreadCount.get(index);
		accountUsedInThreadCount.set(index, ++currentCount);
		return accounts.get(index);
	    }
	    index++;
	}
	return null;
    }
    
    /**
     * Increment news counter for success news posting
     * 
     * @param account
     */
    public void incrementCounter(Account account){
	int index = accounts.indexOf(account);
	Integer count = newsPostedCount.get(index);
	count++;
	newsPostedCount.set(index, count);
    }
    
    public boolean isCanGetNewAccounts(){
	for(Integer count : newsPostedCount){
	    if(count < Constants.NEWS_PER_ACCOUNT){
		return true;
	    }
	}
	return false;
    }
}
