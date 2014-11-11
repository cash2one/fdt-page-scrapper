package com.fdt.dailymotion.util;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.media.MediaLocator;

public class VideoCreator {
	
	public static void main(String... args){
		VideoCreator vCreator = new VideoCreator();
		AudioVideoMerger avMerger = new AudioVideoMerger();
		
		
		try {
			vCreator.makeVideo("test_video.mov");
			
			MediaLocator vml = JpegImagesToMovie.createMediaLocator("test_video.mov");
			MediaLocator aml = JpegImagesToMovie.createMediaLocator("08.wav");
			
			avMerger.mergeFiles(vml, aml);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void makeVideo(String fileName) throws MalformedURLException {
	    Vector<String> imgLst = new Vector<String>();
	    
	    File imageFile = new File("./images/123.jpg");
	    
	    for(int i = 0; i < 1000; i++){
	    	imgLst.add(imageFile.getAbsolutePath());
	    }
	    
	    JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
	    MediaLocator oml;
	    if ((oml = imageToMovie.createMediaLocator(fileName)) == null) {
	        System.err.println("Cannot build media locator from: " + fileName);
	        System.exit(0);
	    }
	    int interval = 50;
	    imageToMovie.doIt(320, 240, (1000 / interval), imgLst, oml);

	}
}
