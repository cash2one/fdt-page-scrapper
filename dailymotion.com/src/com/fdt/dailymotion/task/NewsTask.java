package com.fdt.dailymotion.task;

import java.io.File;


public class NewsTask{
	
	private File inputFileName;
	
	private String videoTitle = "Video title";
	private String videoid = "";
	private String imageUrl = "";

	private String key = "";
	private String snippets = "Desc";
	
	private String uploadUrl = "";

	private int attempsCount = 1;
	//empty result

	public NewsTask(File inputFileName) {
		super();
		this.inputFileName = inputFileName;
		
		//TODO Read and parse file
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

	public String getUploadUrl() {
		return uploadUrl;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}

	public File getInputFileName() {
		return inputFileName;
	}

	public String getVideoid() {
		return videoid;
	}

	public void setVideoId(String videoid) {
		this.videoid = videoid;
	}

	public String getSnippets() {
		return snippets;
	}

	public void setSnippets(String snippets) {
		this.snippets = snippets;
	}

	public String getVideoTitle() {
		return videoTitle;
	}

	public String getTags() {
		return videoTitle;
	}
	
	public String getDescription() {
		return videoTitle;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getImageUrl() {
		return imageUrl;
	}
}
