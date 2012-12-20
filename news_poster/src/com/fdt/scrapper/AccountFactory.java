package com.fdt.scrapper;

import java.util.ArrayList;

import com.fdt.scrapper.task.Constants;

public class AccountFactory
{
    private ArrayList<Account> accounts = new ArrayList<Account>();
    private ArrayList<Integer> newsCountPoster = new ArrayList<Integer>();
    
    public void fillAccounts(String accListFilePath){
	//TODO read account list
	//TODO getting cookie for each account
    }
    
    public Account getAccount(){
	int index = 0;
	for(Integer count : newsCountPoster){
	    if(count < Constants.NEWS_PER_ACCOUNT){
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
	Integer count = newsCountPoster.get(index);
	count++;
	newsCountPoster.set(index, count);
    }
}
