package com.fdt.newposter.scrapper.task;

import org.apache.velocity.VelocityContext;

public class NewsTask{
	private String keyWords = "";
	private VelocityContext newsContent = null;

	private int attempsCount = 1;
	//empty result
	protected String result = null;

	public NewsTask(String keyWords, VelocityContext newsContent) {
		super();
		this.keyWords = keyWords;
		this.newsContent = newsContent;
	}

	public String getResult() {
		return result;
	}

	//Parse result there
	public void setResult(String result){
		this.result = result;
	}

	public  void setResultAsIs(String result){
		if(result != null){
			this.result = result;
		}
		else{
			this.result = null;
		}
	}

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	public VelocityContext getNewsContent() {
		return newsContent;
	}

	public void setNewsContent(VelocityContext newsContent) {
		this.newsContent = newsContent;
	}

	public int getAttempsCount() {
		return attempsCount;
	}

	public void incAttempsCount() {
		this.attempsCount++;
	}


	public boolean isResultEmpty(){
		if(result == null || "".equals(result.trim())){
			return true;
		}
		return false;
	}

	public String toString(){
		return keyWords;
	}
}
