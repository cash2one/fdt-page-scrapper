package com.fdt.dailymotion.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class VideoCreator {

	private static final int VIDEO_WIDTH = 1280;
	private static final int VIDEO_HEIGHT = 720;

	private static final int MIN_FRAME_COUNT = 3599;
	private static final int MAX_FRAME_COUNT = 3600;

	private static final Logger log = Logger.getLogger(VideoCreator.class);

	private final static int audioStreamIndex = 1;
	private final static int audioStreamId = 0;
	private final static int channelCount = 2;

	//public final static int successBitrate = 262144;
	public final static int successBitrate = 886432;

	public static void main(String... args){

		DOMConfigurator.configure("log4j.xml");

		File image;
		File video;

		try {
			/*for(int i = 150; i < 299; i++){
				File videoFile = new File("images/article_"+i+".jpg");
				if(videoFile.exists()){
					VideoCreator.makeVideo("test_video/article_"+i+"_.mp4", new File("images/article_"+i+".jpg"), new File("images/preview_article_"+i+".jpg"), new File("08.wav"), MIN_FRAME_COUNT, MAX_FRAME_COUNT);
					image = new File("images/article_"+i+".jpg");
					video = new File("article_"+i+"_.mp4");
					log.info(String.format("Image file %s; size %s; video bitrate %.0f(%.0f); compression rate: %.3f", image.getName(), image.length(), (double)(video.length()/MAX_FRAME_COUNT),(double)(8*video.length()/MAX_FRAME_COUNT), (double)(MAX_FRAME_COUNT*image.length())/video.length()));
					System.out.println(String.format("Image file %s; size %s; video bitrate %.0f(%.0f); compression rate: %.3f", image.getName(), image.length(), (double)(video.length()/MAX_FRAME_COUNT),(double)(8*video.length()/MAX_FRAME_COUNT), (double)(MAX_FRAME_COUNT*image.length())/video.length()));
					image = null;
					video = null;
				}
			}*/
			VideoCreator.makeVideo("test_video/h264_test.mov", new File[]{new File("images_rand/zala.jpg"), new File("images_rand/zala1.jpg")}, new File("images_rand/zala1.jpg"), false, new File("08.wav"), 150, 180);
			//VideoCreator.mergeVideoAndAudio("test_video_wa.mp4", "08.wav", "test_video.mp4");
			/*MediaLocator ivml = JpegImagesToMovie.createMediaLocator("test_video_wa.mov");
			MediaLocator aml = JpegImagesToMovie.createMediaLocator("08.wav");
			MediaLocator ovml = JpegImagesToMovie.createMediaLocator("test_video.mov");

			avMerger.mergeFiles(ivml, aml, ovml);*/
		} catch (MalformedURLException e) {
			log.error("Error occured during video creation", e);
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Error occured during video creation", e);
			e.printStackTrace();
		}
	}

	/*public static Integer[] makeVideo(String fileName, File imageFile, File previewFile, int minDur, int maxDur) throws IOException {
	    Vector<String> imgLst = new Vector<String>();

	    Random rnd = new Random();
	    int framePerSec = 1;
	    int frameCount = (minDur + rnd.nextInt(maxDur-minDur))*framePerSec;
	    //TODO comment
	   // frameCount = 30;

	    for(int i = 0; i < frameCount-1; i++){
	    	imgLst.add(imageFile.getAbsolutePath());
	    }

	    if(previewFile != null && previewFile.exists()){
	    	imgLst.add(frameCount-1, previewFile.getAbsolutePath());
	    }

	    JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
	    MediaLocator oml;
	    if ((oml = JpegImagesToMovie.createMediaLocator(fileName)) == null) {
	        log.error("Cannot build media locator from: " + fileName);
	        System.exit(0);
	    }
	    imageToMovie.doIt(VIDEO_WIDTH, VIDEO_HEIGHT, framePerSec, imgLst, oml);
	    //imageToMovie.doIt(640, 480, 1, imgLst, oml);
	    //imageToMovie.doIt(320, 240, (100 / interval), imgLst, oml);

	    return new Integer[]{frameCount, framePerSec};
	}*/

	/**
	 * 
	 * @param filePath
	 * @param imageFile
	 * @param previewFile
	 * @param audioFile
	 * @param minDur
	 * @param maxDur - will be displayed as total value of video;
	 * @return
	 * @throws IOException
	 */
	public static Integer[] makeVideo(String filePath, File[] imageFiles, File previewFile, boolean addAudio, File audioFile, int minDur, int maxDur) throws IOException{
		//int framePerSec = calculateFrameRate(imageFile);
		int framePerSec = 25;
		return makeVideo(filePath, imageFiles, previewFile, addAudio, audioFile, minDur, maxDur, framePerSec);
	}

	/**
	 * 
	 * @param filePath
	 * @param imageFile
	 * @param previewFile
	 * @param audioFile
	 * @param minDur
	 * @param maxDur - will be displayed as total value of video;
	 * @return
	 * @throws IOException
	 */
	public static Integer[] makeVideo(String filePath, File[] imageFiles, File previewFile, boolean addAudio, File audioFile, int minDur, int maxDur, int framePerSec) throws IOException {

		long startTime = System.currentTimeMillis();
		IMediaWriter writer = ToolFactory.makeWriter(filePath);

		Random rnd = new Random();
		//framePerSec = 15;

		int frameCount = (minDur + rnd.nextInt(maxDur-minDur))*framePerSec;

		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, VIDEO_WIDTH, VIDEO_HEIGHT);

		if(addAudio){
			IContainer containerAudio = IContainer.make();

			if (containerAudio.open(audioFile.getPath(), IContainer.Type.READ, null) < 0)
				throw new IllegalArgumentException("Cant find " + audioFile.getPath());

			int audioStreamt = 0;

			IStream stream = null;
			IStreamCoder code = null;

			for(int i=0; i<containerAudio.getNumStreams(); i++){
				stream = containerAudio.getStream(i);
				code = stream.getStreamCoder();

				if(code.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
				{
					audioStreamt = i;
					break;
				}

				stream = null;
				code = null;
			}
			IStreamCoder audioCoder = containerAudio.getStream(audioStreamt).getStreamCoder();
			audioCoder.open();

			writer.addAudioStream(audioStreamIndex, audioStreamId, audioCoder.getChannels(), audioCoder.getSampleRate());

			IAudioSamples samples = IAudioSamples.make(audioCoder.getSampleRate(), audioCoder.getChannels(),IAudioSamples.Format.FMT_S32);  

			IPacket packetaudio = IPacket.make();

			int bytesDecodedaudio = -1;
			int offset = 0;

			while(containerAudio.readNextPacket(packetaudio) >= 0){
				offset = 0;
				while(offset<packetaudio.getSize())
				{
					bytesDecodedaudio = audioCoder.decodeAudio(samples, 
							packetaudio,
							offset);
					if (bytesDecodedaudio < 0)
						throw new RuntimeException("could not detect audio");
					offset += bytesDecodedaudio;

					if (samples.isComplete()){
						writer.encodeAudio(audioStreamIndex, samples);
					}
				}
			}

			packetaudio = null;
			
			samples.delete();
			/*audioCoder.release();
			containerAudio.release();*/
			audioCoder.close();
			containerAudio.close();
			audioCoder = null;
			containerAudio = null;
		}

		//for (int index = 0; index < SECONDS_TO_RUN_FOR * FRAME_RATE; index++) {
		BufferedImage screen = ImageIO.read(imageFiles[rnd.nextInt(imageFiles.length)]);
		BufferedImage bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);
		screen = null;

		BufferedImage screenPreview = ImageIO.read(previewFile);
		BufferedImage bgrScreenPreview = convertToType(screenPreview, BufferedImage.TYPE_3BYTE_BGR);
		screenPreview = null;

		/*int initialInt = minDur < 1000?30:300;
		int initialRandInt = minDur < 1000?20:200;*/
		int initialInt = 10;
		int initialRandInt = 5;
		
		int rndTimeIntrvl = framePerSec * initialInt + framePerSec *(int)Math.pow(-1, rnd.nextInt(2))*rnd.nextInt(initialRandInt);
		
		for(long i = 0; i < frameCount-2*framePerSec; i++){
			//swap images
			if(--rndTimeIntrvl <= 0)
			{
				screen = ImageIO.read(imageFiles[rnd.nextInt(imageFiles.length)]);
				bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);
				screen = null;
				
				rndTimeIntrvl = framePerSec * initialInt + framePerSec * (int)Math.pow(-1, rnd.nextInt(2))*rnd.nextInt(initialRandInt);
			}
			writer.encodeVideo(0, bgrScreen, ((i*1000)/framePerSec), TimeUnit.MILLISECONDS);
		}

		for(long i = frameCount-2*framePerSec; i < frameCount; i++){
			writer.encodeVideo(0, bgrScreenPreview, ((i*1000)/framePerSec), TimeUnit.MILLISECONDS);
		}
		writer.encodeVideo(0, bgrScreenPreview, ((frameCount*1000)/framePerSec), TimeUnit.MILLISECONDS);

		bgrScreen = null;
		bgrScreenPreview = null;

		/*for(long i = 1; i < frameCount-2*framePerSec; i++){
			writer.encodeVideo(0, bgrScreen, i, TimeUnit.SECONDS);
		}
		for(long i = frameCount-2; i < frameCount; i++){
			writer.encodeVideo(0, bgrScreen2, i, TimeUnit.SECONDS);
		}
		writer.encodeVideo(0, bgrScreen2, frameCount, TimeUnit.SECONDS);*/

		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// tell the writer to close and write the trailer if needed

		writer.flush();
		writer.close();
		writer = null;

		log.info(String.format("Video Created: %s",filePath));
		long endTime = System.currentTimeMillis();
		log.info(String.format("File for %s was generated for %s second(s)", filePath, ((endTime-startTime)/1000)));

		return new Integer[]{frameCount, framePerSec};
	}

	private static int calculateFrameRate(File inputFile){
		long bitRate = 0;

		for(int i = 15; i <= 30; i +=5){
			bitRate = (inputFile.length()*8*i*100)/1500;
			if(bitRate > successBitrate){
				log.info(String.format("Calculated bitrate: %d, frameRate: %d",bitRate, i));
				System.out.println(String.format("Calculated bitrate: %d, frameRate: %d",bitRate, i));
				return i;
			}
		}

		return 30;
	}

	public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
		BufferedImage image = new BufferedImage(VIDEO_WIDTH, VIDEO_HEIGHT, targetType);

		//TODO scale image
		int[] sclSz = scaleSize(sourceImage.getWidth(),sourceImage.getHeight(),VIDEO_WIDTH,VIDEO_HEIGHT);
		Graphics2D g = image.createGraphics();
		double scaleFactor = getScaleFactor(sourceImage.getWidth(),sourceImage.getHeight(),VIDEO_WIDTH,VIDEO_HEIGHT);
		g.scale(scaleFactor, scaleFactor);
		g.drawImage(sourceImage, (int)Math.round(sclSz[0]/scaleFactor), (int)Math.round(sclSz[1]/scaleFactor), sourceImage.getWidth(), sourceImage.getHeight(), null);
		g.dispose();
		return image;
	}

	private static double getScaleFactor(int width, int height, int destWidth, int destHeight){
		double fitFactorSource = (double)height/width;
		double firFactorDest = (double)destHeight/destWidth;

		double scaleFactor = 1;

		if(fitFactorSource > firFactorDest){
			//fit to height
			scaleFactor = (double)destHeight/height;
		}else{
			//fir to width
			scaleFactor= (double)destWidth/width;
		}

		return scaleFactor;
	}

	private static int[] scaleSize(int width, int height, int destWidth, int destHeight){
		int[] scaleSizes =  new int[]{0,0,VIDEO_WIDTH,VIDEO_HEIGHT};

		double fitFactorSource = (double)height/width;
		double firFactorDest = (double)destHeight/destWidth;

		if(fitFactorSource > firFactorDest){
			//fit to height
			double scaleFactor = (double)destHeight/height;
			int x = (destWidth - (int)Math.round(scaleFactor * width))/2; 
			int y = 0;
			scaleSizes = new int[]{x,y,destWidth-x, destHeight - y};
		}else{
			//fir to width
			double scaleFactor = (double)destWidth/width;
			int x = 0;
			int y = (destHeight - (int)Math.round(scaleFactor * height))/2; 
			scaleSizes = new int[]{x,y,destWidth-x, destHeight - y};
		}

		return scaleSizes;
	}
}
