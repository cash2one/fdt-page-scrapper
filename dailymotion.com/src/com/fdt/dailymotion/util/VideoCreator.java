package com.fdt.dailymotion.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.media.MediaLocator;

public class VideoCreator {
	
	public static void main(String... args){
		AudioVideoMerger avMerger = new AudioVideoMerger();
		
		try {
			VideoCreator.makeVideo("test_video.mov", new File("images/123.jpg"));
			
			MediaLocator vml = JpegImagesToMovie.createMediaLocator("test_video.mov");
			MediaLocator aml = JpegImagesToMovie.createMediaLocator("08.wav");
			
			//avMerger.mergeFiles(vml, aml);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void makeVideo(String fileName, File imageFile) throws IOException {
	    Vector<String> imgLst = new Vector<String>();
	    
	    for(int i = 0; i < 100; i++){
	    	imgLst.add(imageFile.getAbsolutePath());
	    }
	    
	    JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
	    MediaLocator oml;
	    if ((oml = JpegImagesToMovie.createMediaLocator(fileName)) == null) {
	        System.err.println("Cannot build media locator from: " + fileName);
	        System.exit(0);
	    }
	    int interval = 10;
	    imageToMovie.doIt(320, 240, (100 / interval), imgLst, oml);

	}
}
