package com.fdt.imgur.task;

import java.util.Random;

import org.apache.log4j.Logger;


public class ImgurPromoTask{

	private static final Logger log = Logger.getLogger(ImgurPromoTask.class);

	private String imageUrl;
	
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

	private String getRndImageFilename(){
		Random random = new Random();
		int length = 4 + random.nextInt(3);
		String characters = "abcdefghijklmnopqrstuvwxyz";
		char[] text = new char[length];
		for (int i = 0; i < length; i++)
		{
			text[i] = characters.charAt(random.nextInt(characters.length()));
		}
		return new String(text) + String.valueOf(System.currentTimeMillis());
	}
}
