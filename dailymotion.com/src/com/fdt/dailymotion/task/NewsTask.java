package com.fdt.dailymotion.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;


public class NewsTask{
	
	private static final Logger log = Logger.getLogger(NewsTask.class);

	private File inputFile;

	private String videoTitle = "";
	private String videoid = "";
	private String imageUrl = "";

	private String key = "";
	private String snippets = "";

	private String uploadUrl = "";

	private int attempsCount = 1;
	//empty result

	public NewsTask(File inputFileName) {
		super();
		this.inputFile = inputFileName;
		//TODO Read and parse file
		parseFile();
	}

	private void parseFile(){
		//read account list
		FileReader fr = null;
		BufferedReader br = null;
		
		StringBuilder fileAsStr = new StringBuilder();
		
		try {
			int lineIndex = 0;
			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				fileAsStr.append(line);
				if(!"".equals(line)){
					lineIndex++;
					switch(lineIndex){
					case 1:{
						key = line.trim();
						break;
					}
					case 2:{
						videoTitle = line.trim();
						break;
					}
					}
				}
			}
			
			//TODO Get image url
			//TODO Get link for description
			//TODO Generate description
			
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
		return inputFile;
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
