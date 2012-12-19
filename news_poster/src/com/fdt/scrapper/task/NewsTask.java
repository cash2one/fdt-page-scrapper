package com.fdt.scrapper.task;

public class NewsTask{
	private String login = "";
	private String password = "";
	private String keyWords = "";
	private String newsContent = "";
	
	private int attempsCount = 1;
	//empty result
	protected String result = null;
	
	public NewsTask(String login, String password, String keyWords, String newsContent) {
		super();
		this.login = login;
		this.password = password;
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

	public boolean isResultEmpty(){
		if(result == null){
			return true;
		}
		return false;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNewsTitle() {
		return keyWords;
	}

	public void setNewsTitle(String newsTitle) {
		this.keyWords = newsTitle;
	}

	public String getNewsContent() {
		return newsContent;
	}

	public void setNewsContent(String newsContent) {
		this.newsContent = newsContent;
	}
	
	public int getAttempsCount() {
		return attempsCount;
	}

	public void incAttempsCount() {
		this.attempsCount++;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		//TODO Insert code here
		return sb.toString();
	}
}
