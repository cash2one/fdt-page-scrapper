package com.fdt.dailymotion.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.Vector;

import javax.media.MediaLocator;

public class VideoCreator {
	
	public static void main(String... args){
		AudioVideoMerger avMerger = new AudioVideoMerger();
		
		try {
			VideoCreator.makeVideo("test_video_wa.mov", new File("images/_frame.jpg"), new File("images/_preview.jpg"), 3599, 3600);
			
			MediaLocator ivml = JpegImagesToMovie.createMediaLocator("test_video_wa.mov");
			MediaLocator aml = JpegImagesToMovie.createMediaLocator("08.wav");
			MediaLocator ovml = JpegImagesToMovie.createMediaLocator("test_video.mov");
			
			avMerger.mergeFiles(ivml, aml, ovml);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Integer[] makeVideo(String fileName, File imageFile, File previewFile, int minDur, int maxDur) throws IOException {
	    Vector<String> imgLst = new Vector<String>();
	    
	    Random rnd = new Random();
	    int framePerSec = 1;
	    int frameCount = (minDur + rnd.nextInt(maxDur-minDur))*framePerSec;
	    //TODO comment
	   // frameCount = 30;
	    
	    for(int i = 0; i < frameCount; i++){
	    	imgLst.add(imageFile.getAbsolutePath());
	    }
	    
	    imgLst.add(1, previewFile.getAbsolutePath());
	    
	    JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
	    MediaLocator oml;
	    if ((oml = JpegImagesToMovie.createMediaLocator(fileName)) == null) {
	        System.err.println("Cannot build media locator from: " + fileName);
	        System.exit(0);
	    }
	    imageToMovie.doIt(1280, 720, framePerSec, imgLst, oml);
	    //imageToMovie.doIt(640, 480, 1, imgLst, oml);
	    //imageToMovie.doIt(320, 240, (100 / interval), imgLst, oml);
	    
	    return new Integer[]{frameCount, framePerSec};
	}
}
