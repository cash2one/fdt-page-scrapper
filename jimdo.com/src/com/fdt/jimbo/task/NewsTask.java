package com.fdt.jimbo.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import com.fdt.filemapping.RowMappingData;
import com.fdt.utils.Constants;

public class NewsTask
{

	private static final Logger log = Logger.getLogger(NewsTask.class);

	private static final String KEYWORD = "KEYWORD";
	private static final String KEYWORD_SEARCH = "KEYWORD_SEARCH";
	private static final String TITLE = "TITLE";
	private static final String TEXT = "TEXT";

	private int attempsCount = 1;

	private File inputFile;
	
	private RowMappingData data;
	
	private String snippets;
	
	private File templateFile;
	private File templateFileWOPic;
	
	public NewsTask(File inputFile, RowMappingData data, String rndImgUrl, String title, File templateFile, File templateFileWOPic, String jpgUrl, String buttonUrl) throws Exception 
	{
		super();
		this.inputFile = inputFile;
		this.data = data;
		
		data.setDataByName("JPG", jpgUrl);
		data.setDataByName("BUTTON", buttonUrl);
		
		this.templateFile = templateFile;
		this.templateFileWOPic = templateFileWOPic;
		
		prepareData(rndImgUrl, title);
	}
	
	private void prepareData(String rndImgUrl, String title)
	{
		String textStr = data.getDataByName(TEXT);
		textStr = textStr.replaceAll("\\[STARS HERE\\]",String.format("<img src=\"%s\"/>", rndImgUrl));
		textStr = textStr.replaceAll("Description:","<h3>Description:</h3>");
		textStr = textStr.replaceAll("eBook Details:","<h3>eBook Details:</h3>");
		data.updateDataByName(TEXT, textStr);
		
		//Set title
		String newTitle = title.replaceAll("\\[Book\\]", Matcher.quoteReplacement(data.getDataByName(KEYWORD)));
		data.setDataByName(TITLE, newTitle);
	}

	/*private void extractPostLink(String fileContent) throws Exception{
		//Pattern imgPattern =Pattern.compile("href=\"((http(s)?://)?(www.)?([\\.a-z0-9/\\-]+))\"");
		Pattern imgPattern =Pattern.compile("(http://(" + shortUrlList + "){1,1}/([a-zA-Z0-9/]+))");
		Matcher matcher = imgPattern.matcher(fileContent);
		if(matcher.find()){
			log.debug("Link found: " + matcher.group(1));
			System.out.println("Link found: " + matcher.group(1));
			this.postLink = matcher.group(1);
		}else{
			throw new Exception("Post link NOT found");
		}
	}*/

	private String getFileNameWOExt(String fullName){
		return fullName.substring(0,fullName.lastIndexOf('.'));
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


	public File getInputFile() {
		return inputFile;
	}
	
	public String getKey(){
		return data.getDataByName(KEYWORD);
	}
	
	public String getKey4Search(){
		return data.getDataByName(KEYWORD_SEARCH);//.replaceAll("\t+", " ");
	}
	
	public String getTitle(){
		return data.getDataByName(TITLE);
	}


	public void setSnippets(String snippets) {
		//Delete external url
		snippets = snippets.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.]*)*\\/?", "");

		this.snippets = snippets;
	}
	
	
	public String getDescription() throws Exception
	{
		String description = null;
		String pic = data.getDataByName("PICTURE");
		
		if(pic != null && !"".equals(pic.trim()))
		{
			description = getFileAsString(this.templateFile);
		}else{
			description = getFileAsString(this.templateFileWOPic);
		}
		
		
		for(String keyName : data.getKeysList()){
			description = description.replaceAll("\\[" + keyName + "\\]", Matcher.quoteReplacement(data.getDataByName(keyName)));
		}
		
		description = description.replaceAll("\\[SNIPPETS\\]", Matcher.quoteReplacement(this.snippets));

		return description;
	}

	private String getFileAsString(File file) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;

		StringBuilder fileAsStr = new StringBuilder();

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
				fileAsStr.append(line).append(Constants.LINE_FEED);
			}
		}
		finally {
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

		return fileAsStr.toString();
	}

	@Override
	public String toString() {
		return "NewsTask [inputFile=" + inputFile + "]";
	}
}
