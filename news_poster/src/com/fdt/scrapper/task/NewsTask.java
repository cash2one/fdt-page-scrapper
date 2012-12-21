package com.fdt.scrapper.task;

public class NewsTask{
    private String keyWords = "";
    private String newsContent = "";

    private int attempsCount = 1;
    //empty result
    protected String result = null;

    public NewsTask(String keyWords, String newsContent) {
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


    public boolean isResultEmpty(){
	if(result == null || "".equals(result.trim())){
	    return true;
	}
	return false;
    }

    public String toString(){
	StringBuilder sb = new StringBuilder();
	//TODO Insert code here
	return sb.toString();
    }
}
