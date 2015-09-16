package com.fdt.scrapper.task;

import java.util.ArrayList;

public abstract class Task{
	
	private String url = "";
	private ArrayList<String> xPath = new ArrayList<String>();
	
	private int resultCount = 0;
	
	private boolean xmlParce = false;
	//empty result
	protected ArrayList<String> result = null;

	public ArrayList<String> getResult() {
		return result;
	}
	
	public String toCsv() {
		StringBuilder resultCsv = new StringBuilder();
		for(String value : result){
			resultCsv.append(value.isEmpty()?"-1":value).append(":");
		}
		
		if(resultCsv.length() > 1){
			resultCsv.setLength(resultCsv.length()-1);
		}
		return resultCsv.toString();
	}

	//Parse result there
	public abstract boolean setResult(ArrayList<String> result);

	public Task(ArrayList<String> xPath, boolean xmlParce){
		super();
		this.xPath = xPath;
		this.resultCount = xPath.size();
		this.xmlParce = xmlParce;
	}

	public Task(ArrayList<String> xPath){
		super();
		this.xPath = xPath;
		this.resultCount = xPath.size();
		this.xmlParce = false;
	}
	
	public String getUrlToScrap(){
		return url;
	}

	protected void setUrlToScrap(String url){
		this.url = url;
	}

	public int getResultCount() {
		return resultCount;
	}

	public String getxPath(int index){
		return xPath.get(index);
	}

	public void setxPath(String xPath, int index){
		this.xPath.set(index, xPath);
	}

	public boolean isXmlParce()
	{
		return xmlParce;
	}

	public void setXmlParce(boolean xmlParce) {
		this.xmlParce = xmlParce;
	}

	public  void setResultAsIs(ArrayList<String> result){
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

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("URL: ").append(url);
		sb.append("; XPATH: ").append(xPath);
		sb.append("; Source is XML: ").append(xmlParce);
		return sb.toString();
	}
}
