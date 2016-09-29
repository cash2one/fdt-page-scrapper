package com.fdt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fdt.scrapper.task.Snippet;

public class Utils {

	private static final Logger log = Logger.getLogger(Utils.class);
	
	private static final Random rnd = new Random();

	public static void addLinkToSnippetContent(Snippet snippet, String link, int MIN_WORDS_COUNT, int MAX_WORDS_COUNT){
		StringBuilder newContent = new StringBuilder(snippet.getContent());
		//find random
		String[] words = snippet.getContent().split(" ");
		//all snippet will be as link
		if(words.length == 1 || words.length == 2){
			//insert link here
			newContent = new StringBuilder();
			newContent.append("<a href=\""+link+"\">");

			for(int i = 0; i < words.length; i++){
				newContent.append(words[i]).append(" ");
			}
			newContent.setLength(newContent.length()-1);
			newContent.append("</a>");
		}else if(words.length > 2){
			int randomValue = getRandomValue(MIN_WORDS_COUNT, MAX_WORDS_COUNT);
			int startStringIndex = getRandomValue(0, words.length-randomValue);
			newContent = new StringBuilder();
			for(int i = 0; i < words.length; i++)
			{
				if(startStringIndex == i){
					newContent.append("<a href=\""+link+"\">").append(words[i]).append(" ");
					continue;
				}else if((startStringIndex + randomValue-1) == i){
					newContent.append(words[i]).append("</a>").append(" ");
					continue;
				}
				newContent.append(words[i]).append(" ");
			}
			if(newContent.length() > 0){
				newContent.setLength(newContent.length()-1);
			}
		}
		snippet.setContent(newContent.toString());
	}
	
	public static Integer getRandomValue(Integer minValue, Integer maxValue){
		rnd.nextInt();
		return  minValue + rnd.nextInt(maxValue - minValue+1);
	}
	
	public static synchronized List<String> loadFileAsStrList(String cfgFilePath)
	{
		return loadFileAsStrList(new File(cfgFilePath));
	}
	
	public static String getFirstSmblUpper(String input)
	{
		StringBuffer output = new StringBuffer();
		if(input != null && input.length() > 1){
			output = new StringBuffer(input.substring(1).toLowerCase());
			output.insert(0, input.substring(0, 1).toUpperCase());
		}

		return output.toString();
	}
	
	public static synchronized List<String> loadFileAsStrList(File cfgFile){
		return loadFileAsStrList(cfgFile, false);
	}

	public static synchronized List<String> loadFileAsStrList(File cfgFile, boolean returnEmptyString){
		ArrayList<String> linkList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader( new FileInputStream(cfgFile), "UTF8" ));

			String line = br.readLine();

			while(line != null)
			{
				//String utf8Line = new String(line.getBytes(),"UTF-8");

				if(returnEmptyString || !"".equals(line.trim()))
				{
					linkList.add(line.trim());
				}

				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.error("Reading file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
		return linkList;
	}
	
	public static synchronized String loadFileAsString(File inputFile)
	{
		return loadFileAsString(inputFile, new ArrayList<Integer>());
	}
	
	public static synchronized String loadFileAsString(File inputFile, List<Integer> skipStrNum)
	{
		StringBuffer output  = new StringBuffer();
		
		BufferedReader br = null;
		int strIndex = 1;
		
		try {
			br = new BufferedReader(new InputStreamReader( new FileInputStream(inputFile), "UTF8" ));

			String line = br.readLine();

			while(line != null)
			{
				if(!skipStrNum.contains(strIndex)){
					output.append(line).append(Constants.LINE_FEED);
				}
				
				line = br.readLine();
				strIndex++;
			}
		} catch (FileNotFoundException e) {
			log.error("Reading file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
		return output.toString();
	}

	public static StringBuilder getResponseAsString(HttpURLConnection conn)
			throws IOException {
		InputStream is = conn.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder responseStr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			responseStr.append(line).append(Constants.LINE_FEED);
		}
		is.close();
		return responseStr;
	}
	
	public static void appendStringToFile(String str, File file) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();
		} catch (FileNotFoundException ex) {
			log.error("Error during saving string to file",ex);
		} catch (IOException ex) {
			log.error("Error during saving string to file",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error during closing output stream",ex);
			}
		}
	}
	
	public static void saveStringToFile(String str, File file, boolean append) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, append), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();
		} catch (FileNotFoundException ex) {
			log.error("Error during saving string to file",ex);
		} catch (IOException ex) {
			log.error("Error during saving string to file",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error during closing output stream",ex);
			}
		}
	}
	
	public static void replaceStrInFile(String str2Replace, String replacement, String filePath) {
		try {
			Path path = Paths.get(filePath);
			Charset charset = StandardCharsets.UTF_8;

			String content = new String(Files.readAllBytes(path), charset);
			content = content.replaceAll(str2Replace, replacement);
			Files.write(path, content.getBytes(charset));
		} catch (IOException ex) {
			log.error("Error during saving string to file",ex);
		}
	}
	
	
	public static String synonymizeText(String text){
		Pattern ptrn = Pattern.compile("(\\{([^\\{\\}]+)\\})");
		Matcher mtch = ptrn.matcher(text);

		while(mtch.find()){
			String[] array = mtch.group(2).split("\\|");
			text = text.replace(mtch.group(1), array[rnd.nextInt(array.length)]);
			mtch = ptrn.matcher(text);
		}
		
		text = text.replaceAll("\r\n", "</br>\r\n");

		return text;
	}
	

	public static String fillTemplate(String template, String keyWord, HashMap<Integer,List<String>> preTitles, String PRETITLES_REGEXP){

		//TODO Fill template
		String output = template.replaceAll("\\[keyword\\]", keyWord);

		for(int idx : preTitles.keySet()){
			int prettlLstSz = preTitles.get(idx).size();
			output = output.replaceAll("\\["+ PRETITLES_REGEXP + idx + "\\]", preTitles.get(idx).get(rnd.nextInt(prettlLstSz)));
		}

		return output;
	}
	
	public static HashMap<Integer,List<String>> loadPreTtls(File pretitlesFolder, final String PRETITLES_FILE_NAMES_REGEXP){

		HashMap<Integer,List<String>> preTitles = new HashMap<Integer,List<String>>();
		
		FilenameFilter flNmFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return new File(dir,name).isFile() && name.matches(PRETITLES_FILE_NAMES_REGEXP);
			}
		};

		for(File preTtlFl : pretitlesFolder.listFiles(flNmFilter)){
			preTitles.put(getFileIdx(preTtlFl.getName(), PRETITLES_FILE_NAMES_REGEXP), Utils.loadFileAsStrList(preTtlFl));
		}

		return preTitles;
	}
	

	private static int getFileIdx(String fileName, String PRETITLES_FILE_NAMES_REGEXP)
	{
		Pattern ptrn = Pattern.compile(PRETITLES_FILE_NAMES_REGEXP);
		Matcher m = ptrn.matcher(fileName); 
		m.find();
		
		return Integer.valueOf(m.group(1));
	}
}
