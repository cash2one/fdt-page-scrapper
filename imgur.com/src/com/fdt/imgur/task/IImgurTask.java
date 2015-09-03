package com.fdt.imgur.task;

import java.io.File;

public interface IImgurTask 
{
	public int getAttempsCount();

	public void incAttempsCount();
		
	public String getImageUrl();

	public void setImageUrl(String imageUrl);
	
	public String getImageFormat();
	
	public void setImageFormat(String imageFormat);

	public String getUploadUrl();

	public void setUploadUrl(String uploadUrl);
	
	public File getImageFile();

	public void setImageFile(File imageFile);
}