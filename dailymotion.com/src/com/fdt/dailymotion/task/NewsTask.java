package com.fdt.dailymotion.task;


public class NewsTask{
	
	private String inputFileName;
	
	private String title = "";
	private String keyWords = "";
	private String snippets = "";

	private int attempsCount = 1;
	//empty result

	public NewsTask(String inputFileName) {
		super();
		this.inputFileName = inputFileName;
		
		//TODO Read and parse file
	}

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
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

	public String toString(){
		return keyWords;
	}
}
