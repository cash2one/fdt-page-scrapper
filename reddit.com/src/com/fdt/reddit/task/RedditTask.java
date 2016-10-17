package com.fdt.reddit.task;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.fdt.reddit.Account;
import com.fdt.reddit.task.actions.RedditActions;

public class RedditTask
{
	private static final Logger log = Logger.getLogger(RedditTask.class);
	
	private final String link;
	private final String key;
	
	private ArrayList<RedditActions> ACTIONS = new ArrayList<RedditActions>();
	
	private int attempsCount = 1;

	private String snippets;
	
	public RedditTask(String link,  String key) throws Exception 
	{
		super();
		this.link = link;
		this.key = key;
		
		//TODO Randomly fullfill actions list conserning TZ
		ACTIONS.add(RedditActions.ACTION_COMMENT);
	}
	
	public String getLink() {
		return link;
	}

	public String getKey() {
		return key;
	}

	public int getAttempsCount() {
		return attempsCount;
	}

	public void incAttempsCount() {
		this.attempsCount++;
	}

	public boolean isResultEmpty(){
		if(snippets == null || "".equals(snippets.trim())){
			return true;
		}
		return false;
	}

	public void setSnippets(String snippets) {
		//Delete external url
		snippets = snippets.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.]*)*\\/?", "");

		this.snippets = snippets;
	}


	@Override
	public String toString() {
		return String.format("ReditTask [link='%s'; key='%s']", link, key);
	}

	public ArrayList<RedditActions> getACTIONS() {
		return ACTIONS;
	}
}
