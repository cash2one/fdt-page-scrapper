package com.fdt.imgur.task;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


public class ImgurPromoTask implements IImgurTask{

	private static final Logger log = Logger.getLogger(ImgurPromoTask.class);

	private String imageUrl;
	private String imageFormat;
	
	private File imageFile;
	
	private String uploadUrl;
	
	private int attempsCount = 1;

	public ImgurPromoTask(String imageUrl) throws Exception {
		super();
		this.imageUrl = imageUrl;
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

	public String getUploadUrl() {
		return uploadUrl;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}
	
	public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

	public void extractImage() throws Exception{
		Pattern imgPattern =Pattern.compile("((http://)?(www.)?([\\.\\,\\-\\_\\+\\(\\)@/a-zA-Z0-9]+(jpg|png)))");
		Matcher matcher = imgPattern.matcher(imageUrl);
		if(matcher.find()){
			log.debug("Image found: " + matcher.group(1));
			System.out.println("Image found: " + matcher.group(1));
			imageUrl = matcher.group(1);
			imageFormat = matcher.group(5);
		}else{
			throw new Exception("Image URL NOT found in file string: " + imageUrl);
		}
	}
	
	
}
