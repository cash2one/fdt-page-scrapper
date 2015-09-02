package com.fdt.imgur.task;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;


public class ImgurTask{

	private static final Logger log = Logger.getLogger(ImgurTask.class);

	private static final String LINE_FEED = "\r\n";

	private File file;
	private String imageUrl;
	private String imageFormat;
	private int attempsCount = 1;

	public ImgurTask(File file) throws Exception {
		super();
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	public int getAttempsCount() {
		return attempsCount;
	}

	public void incAttempsCount() {
		this.attempsCount++;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageFormat() {
		return imageFormat;
	}

	public void setImageFormat(String imageFormat) {
		this.imageFormat = imageFormat;
	}

	public void parseFile() throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;

		StringBuilder fileAsStr = new StringBuilder();

		try {
			/*int lineIndex = 0;
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
				fileAsStr.append(line).append(LINE_FEED);
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
*/

			log.debug("File content: " + fileAsStr.toString());
			extractImage(fileAsStr.toString());
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
	}

	private void extractImage(String fileContent) throws Exception{
		Pattern imgPattern =Pattern.compile("((http://)?(www.)?([\\.\\,\\-\\_\\+\\(\\)@/a-zA-Z0-9]+(jpg|png)))");
		Matcher matcher = imgPattern.matcher(fileContent);
		if(matcher.find()){
			log.debug("Image found: " + matcher.group(1));
			System.out.println("Image found: " + matcher.group(1));
			imageUrl = matcher.group(1);
			imageFormat = matcher.group(5);
		}else{
			throw new Exception("Image URL NOT found");
		}
	}
	
	public static String getFileAsString(File file) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;

		StringBuilder fileAsStr = new StringBuilder();

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
				fileAsStr.append(line).append(LINE_FEED);
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
}
