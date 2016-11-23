package com.fdt.dailymotion.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Assert;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

import io.humble.video.AudioChannel.Layout;
import io.humble.video.AudioFormat.Type;
import io.humble.video.Codec;
import io.humble.video.Codec.ID;
import io.humble.video.Coder.Flag;
import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.Encoder;
import io.humble.video.MediaAudio;
import io.humble.video.MediaAudioResampler;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import io.humble.video.javaxsound.AudioFrame;
import io.humble.video.javaxsound.MediaAudioConverter;
import io.humble.video.javaxsound.MediaAudioConverterFactory;

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

	private static final Random rnd = new Random();

	/**
	 * @param args
	 */
	public static void main(String... args){

		DOMConfigurator.configure("log4j.xml");

		try {
			flvMp3Test();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		File image;
		File video;

		if(args.length < 12){
			System.out.println("НЕ хватает аргументов для запуска. Используйте слудующий порядок аргументов:\r\n" + 
					"<имя_видое_файла>\r\n" +
					"<путь_к_рандомным_файлам>\r\n" +
					"<файл для превью>\r\n" +
					"<использовать_ли_аудио_true/false>\r\n" +
					"<аудио_файл>\r\n" +
					"<минимальная_продолжительность_в_секундах>\r\n" +
					"<максимальная_продолжительность_в_секундах>\r\n" +
					"<количество_кадров_в_секунду>\r\n" +
					"<использовать_ли_превью_true/false>\r\n" +
					"<минимальное_количество_перелкючений_картинки>\r\n" +
					"<максимальное_количество_перелкючений_картинки>\r\n" +
					"<брать_картинки_по_порядковым_номерам>\r\n"
					);
			return;
		}

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
			Random rnd = new Random();

			String videoFileNm = args[0];
			File[] rndListFiles = getFileList(args[1]);
			File previewFile = new File(args[2]);
			boolean useAudio = Boolean.valueOf(args[3]);
			String[] audioFilesStrArr = args[4].split(";");
			File[] audioFiles = new File[audioFilesStrArr.length];
			for(int i = 0; i < audioFilesStrArr.length; i++){
				audioFiles[i] = new File(audioFilesStrArr[i]);
			}
			int minDur = Integer.valueOf(args[5]);
			int maxDur = Integer.valueOf(args[6]);
			int framePerSec = Integer.valueOf(args[7]);
			boolean usePreview = Boolean.valueOf(args[8]);
			int minIntrvl = Integer.valueOf(args[9]);
			int maxIntrvl = Integer.valueOf(args[10]);
			boolean useFileOrder = Boolean.valueOf(args[11]);

			int intrvlRnd = minIntrvl + rnd.nextInt(maxIntrvl - minIntrvl + 1);

			VideoCreator.makeVideo(videoFileNm,rndListFiles, previewFile, useAudio, audioFiles, minDur, maxDur, framePerSec, usePreview, intrvlRnd, useFileOrder, false);
			//VideoCreator.makeVideo("test_video/h264_test.mov", new File[]{new File("images_rand/zala.jpg"), new File("images_rand/zala1.jpg")}, new File("images_rand/zala1.jpg"), false, new File("08.wav"), 150, 180, true);
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

	private static class FileOrder 
	{
		private File file;
		private int order;

		public FileOrder(File file, int order) {
			super();
			this.file = file;
			this.order = order;
		}
		public File getFile() {
			return file;
		}
		public int getOrder() {
			return order;
		}
	}

	private static File[] getFileList(String dir){

		File[] files;
		File dirFile = new File(dir);

		if(dirFile.exists() && dirFile.isDirectory()){
			files = dirFile.listFiles();
		}else{
			String[] filesStr = dir.split(";");
			files = new File[filesStr.length];
			for(int i = 0; i < filesStr.length; i++){
				files[i] = new File(filesStr[i]);
			}
		}

		File[] outputList;
		//return new File(dir).listFiles();

		ArrayList<FileOrder> filesList = new ArrayList<FileOrder>(files.length);

		for(int i = 0; i < files.length; i++){
			filesList.add(new FileOrder(files[i], extractOrderNumber(files[i].getName()))) ;
		}

		Collections.sort(filesList, new Comparator<FileOrder>() {

			@Override
			public int compare(FileOrder arg0, FileOrder arg1) {
				// TODO Auto-generated method stub
				return arg0.getOrder() - arg1.getOrder();
			}
		});

		outputList = new File[filesList.size()];

		for(int i = 0; i < filesList.size(); i++){
			outputList[i] = filesList.get(i).getFile();
		}

		return outputList;
	}

	private static int extractOrderNumber(String fileName) {
		Pattern ptrn =  Pattern.compile("(.*?)(\\d+)\\.(png|jpg)");
		Matcher mtchr = ptrn.matcher(fileName);

		int result = -1;

		if(mtchr.matches()){
			result = Integer.valueOf(mtchr.group(2));
		}

		return result;
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
	public static Integer[] makeVideo(String filePath, File[] imageFiles, File previewFile, boolean addAudio, File[] audioFiles, int minDur, int maxDur, boolean isUsePreview) throws IOException{
		//int framePerSec = calculateFrameRate(imageFile);
		int framePerSec = 25;
		return makeVideo(
				filePath, 
				imageFiles, 
				previewFile, 
				addAudio, 
				new File[]{audioFiles[rnd.nextInt(audioFiles.length)]}, 
				minDur, 
				maxDur, 
				framePerSec, 
				isUsePreview, 
				1
				);
	}

	public static Integer[] makeVideo(String filePath, File[] imageFiles, File previewFile, boolean addAudio, File[] audioFiles, int minDur, int maxDur, int framePerSec, boolean isUsePreview, int intrvlCount) throws IOException {
		return makeVideo(
				filePath, 
				imageFiles, 
				previewFile, 
				addAudio, 
				new File[]{audioFiles[rnd.nextInt(audioFiles.length)]}, 
				minDur, 
				maxDur, 
				framePerSec, 
				isUsePreview, 
				intrvlCount, 
				false,
				false
				);
	}

	/**
	 * 
	 * @param filePath
	 * @param imageFile
	 * @param previewFile
	 * @param audioFiles
	 * @param minDur
	 * @param maxDur - will be displayed as total value of video;
	 * @return
	 * @throws IOException
	 */
	public static Integer[] makeVideo(String filePath, File[] imageFiles, File previewFile, boolean addAudio, File[] audioFiles, int minDur, int maxDur, int framePerSec, boolean isUsePreview, int intrvlCount, boolean useFileOrder, boolean useRndAudioTimeShift) throws IOException {

		long startTime = System.currentTimeMillis();
		IMediaWriter writer = ToolFactory.makeWriter(filePath);

		Random rnd = new Random();
		//framePerSec = 15;

		int frameCount = (minDur + rnd.nextInt(maxDur-minDur + 1))*framePerSec;

		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, VIDEO_WIDTH, VIDEO_HEIGHT);

		//adding audion files
		if(addAudio){
			addAudios(audioFiles, writer, frameCount/framePerSec, useRndAudioTimeShift);
		}


		int frameOrderIndex = 0;

		//for (int index = 0; index < SECONDS_TO_RUN_FOR * FRAME_RATE; index++) {
		BufferedImage screen = ImageIO.read(getFrameImage(imageFiles, useFileOrder, frameOrderIndex));
		BufferedImage bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);
		screen = null;

		BufferedImage bgrScreenPreview = null;
		BufferedImage screenPreview = null;
		if(isUsePreview){
			screenPreview = ImageIO.read(previewFile);
			bgrScreenPreview = convertToType(screenPreview, BufferedImage.TYPE_3BYTE_BGR);
			screenPreview = null;
		}

		int rndTimeIntrvl = calculateInterval(frameCount, intrvlCount);

		int totalFrameCnt = 0;

		if(useFileOrder){
			for(long i = 0; frameOrderIndex < imageFiles.length && totalFrameCnt < frameCount; i++){
				//swap images
				if(--rndTimeIntrvl <= 0)
				{
					screen = ImageIO.read(getFrameImage(imageFiles, useFileOrder, frameOrderIndex));
					frameOrderIndex++;
					bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);
					screen = null;

					rndTimeIntrvl = calculateInterval(frameCount, intrvlCount);
				}
				writer.encodeVideo(0, bgrScreen, ((i*1000)/framePerSec), TimeUnit.MILLISECONDS);
				totalFrameCnt++;
			}
		}else{
			for(long i = 0; i < frameCount-2*framePerSec; i++){
				//swap images
				if(--rndTimeIntrvl <= 0)
				{
					screen = ImageIO.read(getFrameImage(imageFiles, useFileOrder, frameOrderIndex));
					bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);
					screen = null;

					rndTimeIntrvl = calculateInterval(frameCount, intrvlCount);
				}
				writer.encodeVideo(0, bgrScreen, ((i*1000)/framePerSec), TimeUnit.MILLISECONDS);
			}
		}


		//TODO Uncomment for using preview
		if(!isUsePreview){
			bgrScreenPreview = bgrScreen;
		}

		for(long i = frameCount-2*framePerSec; i < frameCount + framePerSec; i++){
			writer.encodeVideo(0, bgrScreenPreview, ((i*1000)/framePerSec), TimeUnit.MILLISECONDS);
		}
		//writer.encodeVideo(0, bgrScreenPreview, ((frameCount*1000)/framePerSec), TimeUnit.MILLISECONDS);

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

	public static Integer[] makeVideoByOrder(String filePath, File[] imageFiles, File[] audioFiles, int framePerSec) throws Exception {

		long startTime = System.currentTimeMillis();
		IMediaWriter writer = ToolFactory.makeWriter(filePath);

		Random rnd = new Random();
		//framePerSec = 15;

		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, VIDEO_WIDTH, VIDEO_HEIGHT);

		//adding audion files
		//addAudios(audioFiles, writer);

		//for (int index = 0; index < SECONDS_TO_RUN_FOR * FRAME_RATE; index++) {
		BufferedImage screen = null;
		BufferedImage bgrScreen = null;
		screen = null;

		long frameCnt = 0;
		long frameDur = 0;

		long totalDur = 0;

		for(int i = 0; i < imageFiles.length; i++){
			//swap images
			long durationAudio = getAudioFileDurationInMilliSec(audioFiles[i]);

			frameCnt = durationAudio * framePerSec / 1000;
			frameDur = 1000/framePerSec;

			screen = ImageIO.read(imageFiles[i]);
			bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);
			screen = null;

			for(int j = 0; j < frameCnt; j++){
				writer.encodeVideo(0, bgrScreen, totalDur + j*frameDur, TimeUnit.MILLISECONDS);
			}

			totalDur += durationAudio;
		}

		bgrScreen = null;

		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// tell the writer to close and write the trailer if needed
		writer.setForceInterleave(false);
		writer.flush();
		writer.close();
		writer = null;

		log.info(String.format("Video Created: %s",filePath));
		long endTime = System.currentTimeMillis();
		log.info(String.format("File for %s was generated for %s second(s)", filePath, ((endTime-startTime)/1000)));

		return new Integer[]{(int)totalDur, framePerSec};
	}


	/*private static void recordScreen(String filename, String formatname,
			String codecname, int duration, int snapsPerSecond) throws AWTException, InterruptedException, IOException {*/
	public static void recordScreen(String filename, String formatname, File[] imageFiles, File[] audioFiles, int framePerSec) throws Exception {
		/**
		 * Set up the AWT infrastructure to take screenshots of the desktop.
		 */

		final Rational framerate = Rational.make(1, framePerSec);

		/** First we create a muxer using the passed in filename and formatname if given. */
		final Muxer muxer = Muxer.make(filename, null, formatname);

		/** Now, we need to decide what type of codec to use to encode video. Muxers
		 * have limited sets of codecs they can use. We're going to pick the first one that
		 * works, or if the user supplied a codec name, we're going to force-fit that
		 * in instead.
		 */
		final MuxerFormat format = muxer.getFormat();


		final Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
		//final Codec codec = Codec.findDecodingCodec(ID.CODEC_ID_MPEG2VIDEO);
		/**
		 * Now that we know what codec, we need to create an encoder
		 */
		Encoder encoder = Encoder.make(codec);

		/**
		 * Video encoders need to know at a minimum:
		 *   width
		 *   height
		 *   pixel format
		 * Some also need to know frame-rate (older codecs that had a fixed rate at which video files could
		 * be written needed this). There are many other options you can set on an encoder, but we're
		 * going to keep it simpler here.
		 */
		encoder.setWidth(VIDEO_WIDTH);
		encoder.setHeight(VIDEO_HEIGHT);
		// We are going to use 420P as the format because that's what most video formats these days use
		final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
		encoder.setPixelFormat(pixelformat);
		encoder.setTimeBase(framerate);
		//encoder.setChannels(1);

		Encoder audioEncoder = Encoder.make(Codec.findEncodingCodec(Codec.ID.CODEC_ID_AAC));

		Type findType = null;

		for(Type type : audioEncoder.getCodec().getSupportedAudioFormats()) {
			if(findType == null) {
				findType = type;
			}
			if(type == Type.SAMPLE_FMT_S16) {
				findType = type;
				break;
			}
		}

		log.info(findType.toString());

		int sampleRate = 16000;

		audioEncoder.setSampleRate(sampleRate);
		audioEncoder.setChannels(1);
		audioEncoder.setChannelLayout(Layout.CH_LAYOUT_MONO);
		audioEncoder.setSampleFormat(findType);
		audioEncoder.setFlag(Flag.FLAG_GLOBAL_HEADER, true);

		/** An annoynace of some formats is that they need global (rather than per-stream) headers,
		 * and in that case you have to tell the encoder. And since Encoders are decoupled from
		 * Muxers, there is no easy way to know this beyond 
		 */
		if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
			encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);

		/** Open the encoder. */
		encoder.open(null, null);

		/** Open audio encoder. */
		audioEncoder.open(null, null);
		
		/** Add this stream to the muxer. */
		muxer.addNewStream(encoder);

		//add audio encoder
		muxer.addNewStream(audioEncoder);

		/** And open the muxer for business. */
		muxer.open(null, null);

		
		/** Next, we need to make sure we have the right MediaPicture format objects
		 * to encode data with. Java (and most on-screen graphics programs) use some
		 * variant of Red-Green-Blue image encoding (a.k.a. RGB or BGR). Most video
		 * codecs use some variant of YCrCb formatting. So we're going to have to
		 * convert. To do that, we'll introduce a MediaPictureConverter object later. object.
		 */
		MediaPictureConverter videoConverter = null;
		final MediaPicture picture = MediaPicture.make(
				encoder.getWidth(),
				encoder.getHeight(),
				pixelformat);
		picture.setTimeBase(framerate);

		/**
		 * Start by creating a container object, in this case a demuxer since
		 * we are reading, to get audio data from.
		 */
		Demuxer demuxer = Demuxer.make();

		/*
		 * Open the demuxer with the filename passed on.
		 */
		demuxer.open(audioFiles[0].getAbsolutePath(), null, false, true, null, null);

		/*
		 * Query how many streams the call to open found
		 */
		int numStreams = demuxer.getNumStreams();

		/*
		 * Iterate through the streams to find the first audio stream
		 */
		int audioStreamId = -1;
		Decoder audioDecoder = null;
		for(int i = 0; i < numStreams; i++)
		{
			final DemuxerStream stream = demuxer.getStream(i);
			final Decoder decoder = stream.getDecoder();
			if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO) {
				audioStreamId = i;
				audioDecoder = decoder;
				// stop at the first one.
				break;
			}
		}
		if (audioStreamId == -1)
			throw new RuntimeException("could not find audio stream in container: "+filename);

		demuxer.close();

		/*
		 * We allocate a set of samples with the same number of channels as the
		 * coder tells us is in this buffer.
		 */
		MediaAudio samples = MediaAudio.make(
				audioDecoder.getFrameSize(),
				audioDecoder.getSampleRate(),
				audioDecoder.getChannels(),
				audioDecoder.getChannelLayout(),
				audioDecoder.getSampleFormat());

		/*
		 * Now, we start walking through the container looking at each packet. This
		 * is a decoding loop, and as you work with Humble you'll write a lot
		 * of these.
		 * 
		 * Notice how in this loop we reuse all of our objects to avoid
		 * reallocating them. Each call to Humble resets objects to avoid
		 * unnecessary reallocation.
		 */


		/** Now begin our main loop of taking screen snaps.
		 * We're going to encode and then write out any resulting packets. */
		MediaPacket packetVideo = MediaPacket.make();

		long totalAddedFrames = 0;

		for (int i = 0; i < imageFiles.length; i++) {
			/** Make the screen capture && convert image to TYPE_3BYTE_BGR */
			BufferedImage imageBI = ImageIO.read(imageFiles[i]);
			BufferedImage screen = convertToType(imageBI, BufferedImage.TYPE_3BYTE_BGR);

			long durationAudio = getAudioFileDurationInMilliSec(audioFiles[i]);

			/** This is LIKELY not in YUV420P format, so we're going to convert it using some handy utilities. */
			if (videoConverter == null)
				videoConverter = MediaPictureConverterFactory.createConverter(screen, picture);

			for(int j = 0; j < (durationAudio / 1000.0) / framerate.getDouble(); j++){
				videoConverter.toPicture(picture, screen, totalAddedFrames);
				totalAddedFrames++;
			}

			do {
				encoder.encode(packetVideo, picture);	
				if (packetVideo.isComplete())
					muxer.write(packetVideo, false);
			} while (packetVideo.isComplete());
		}
		/** Encoders, like decoders, sometimes cache pictures so it can do the right key-frame optimizations.
		 * So, they need to be flushed as well. As with the decoders, the convention is to pass in a null
		 * input until the output is not complete.
		 */
		do {
			encoder.encode(packetVideo, null);
			if (packetVideo.isComplete())
				muxer.write(packetVideo,  false);
		} while (packetVideo.isComplete());
		
		final MediaAudioConverter converter =
		        MediaAudioConverterFactory.createConverter(
		            MediaAudioConverterFactory.DEFAULT_JAVA_AUDIO,
		            samples);


		
		MediaPacket packetAudio = MediaPacket.make();
		MediaPacket ioPacket = MediaPacket.make();

		MediaAudio resampled = null;

		/*for (int i = 0; i < audioFiles.length; i++) {
			*//**
			 * Open the demuxer with the filename passed on.
			 *//*
			demuxer = Demuxer.make();
			demuxer.open(audioFiles[i].getCanonicalPath(), null, false, true, null, null);

			while(demuxer.read(ioPacket) >= 0) {
				if (ioPacket.getStreamIndex() == audioStreamId)
				{
					
					 * A packet can actually contain multiple sets of samples (or frames of samples
					 * in audio-decoding speak).  So, we may need to call decode audio multiple
					 * times at different offsets in the packet's data.  We capture that here.
					 
					int offset = 0;
					int bytesRead = 0;
					do {
						bytesRead += audioDecoder.decode(samples, ioPacket, offset);

						if (samples.isComplete()) {
							if(samples.getSampleRate() != audioEncoder.getSampleRate()
									|| samples.getFormat() != audioEncoder.getSampleFormat()
									|| samples.getChannelLayout() != audioEncoder.getChannelLayout()) {
								final MediaAudioResampler resampler = MediaAudioResampler.make(
										audioEncoder.getChannelLayout(), audioEncoder.getSampleRate(), audioEncoder.getSampleFormat(),
										samples.getChannelLayout(), samples.getSampleRate(), samples.getFormat());
								resampler.open();
								MediaAudio spl = MediaAudio.make(samples.getNumSamples(), audioEncoder.getSampleRate(), audioEncoder.getChannels(), audioEncoder.getChannelLayout(), audioEncoder.getSampleFormat());
								resampler.resample(spl, samples);
								log.info(spl.toString());
								//log.info("{}", spl.getNumSamples());
								//Assert.assertEquals(spl.getNumSamples(), samples.getNumSamples());
								resampled = spl;
							}else{
								resampled = samples;
							}

							do {
								audioEncoder.encode(packetAudio, resampled);	
								if (packetAudio.isComplete())
									muxer.write(packetAudio, false);
							} while (packetAudio.isComplete());
						}
						offset += bytesRead;
					} while (offset < ioPacket.getSize());
				}
			}

			do {
				audioEncoder.encode(packetAudio, resampled);	
				if (packetAudio.isComplete())
					muxer.write(packetAudio, false);
			} while (packetAudio.isComplete());
		}
		demuxer.close();*/
		
		/** Finally, let's clean up after ourselves. */
		muxer.close();
	}

	public static void flvMp3Test() throws Exception {
		log.info("flvMp3Test");
		Muxer muxer = Muxer.make("flvMp3Test.flv_mp3.flv", null, null);

		Encoder audioEncoder = Encoder.make(Codec.findEncodingCodec(Codec.ID.CODEC_ID_MP3));

		Type findType = null;

		for(Type type : audioEncoder.getCodec().getSupportedAudioFormats()) {
			if(findType == null) {
				findType = type;
			}
			if(type == Type.SAMPLE_FMT_S16) {
				findType = type;
				break;
			}
		}

		log.info(findType.toString());

		int sampleRate = 44100;

		audioEncoder.setSampleRate(sampleRate);
		Rational encoderTimeBase = Rational.make(1, sampleRate);
		audioEncoder.setTimeBase(encoderTimeBase);
		audioEncoder.setChannels(1);
		audioEncoder.setChannelLayout(Layout.CH_LAYOUT_MONO);
		audioEncoder.setSampleFormat(findType);
		audioEncoder.setFlag(Flag.FLAG_GLOBAL_HEADER, true);
		audioEncoder.open(null, null);
		muxer.addNewStream(audioEncoder);
		processConvert(muxer, audioEncoder);
		log.info("done");
	}
	private static void processConvert(Muxer muxer, Encoder encoder) throws Exception {
		muxer.open(null, null);
		MediaPacket packet = MediaPacket.make();
		MediaAudio samples = beepSamples();
		log.info(samples.toString());
		if(samples.getSampleRate() != encoder.getSampleRate()
				|| samples.getFormat() != encoder.getSampleFormat()
				|| samples.getChannelLayout() != encoder.getChannelLayout()) {
			final MediaAudioResampler resampler = MediaAudioResampler.make(
					encoder.getChannelLayout(), encoder.getSampleRate(), encoder.getSampleFormat(),
					samples.getChannelLayout(), samples.getSampleRate(), samples.getFormat());
			resampler.open();
			MediaAudio spl = MediaAudio.make(samples.getNumSamples(), encoder.getSampleRate(), encoder.getChannels(), encoder.getChannelLayout(), encoder.getSampleFormat());
			resampler.resample(spl, samples);
			log.info(spl.toString());
			//log.info("{}", spl.getNumSamples());
			Assert.assertEquals(spl.getNumSamples(), samples.getNumSamples());
			samples = spl;
		}
		log.info(samples.toString());
		
		samples.setTimeStamp(0L);

		// we only have one set of samples.
		encoder.encodeAudio(packet, samples);
		//log.info("{}", packet);
		if(packet.isComplete())
			muxer.write(packet, false);

		// Flush the encoders
		do {
			encoder.encodeAudio(packet, null);
			//log.info("{}", packet);
			if(packet.isComplete()) {
				muxer.write(packet, false);
			}
		} while (packet.isComplete());
		muxer.close();
	}

	/**
	 * make sine wave humble MediaAudio.
	 * @return
	 */
	private static MediaAudio beepSamples() {
		int sampleRate = 44100; // 44.1KHz
		int sampleNum  = 44100; // 44100 samples(1sec)
		int channel    = 2;     // 2channel(stereo)
		int tone       = 440;   // 440Hz tone.
		int bit        = 16;    // 16bit
		ByteBuffer buffer = ByteBuffer.allocate((int)sampleNum * bit * channel / 8);
		double rad = tone * 2 * Math.PI / sampleRate; // radian for each sample.
		double max = (1 << (bit - 2)) - 1; // ampletude
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		for(int i = 0;i < sampleNum;i ++) {
			short data = (short)(Math.sin(rad * i) * max);
			for(int j = 0;j < channel;j ++) {
				buffer.putShort(data);
			}
		}
		buffer.flip();

		log.info("data size for 1sec buffer.:" + buffer.remaining());
		MediaAudio samples = MediaAudio.make(sampleNum, sampleRate, channel, Layout.CH_LAYOUT_STEREO, Type.SAMPLE_FMT_S16);
		samples.getData(0).put(buffer.array(), 0, 0, buffer.remaining());
		//log.info("{}", samples.getDataPlaneSize(0)); // why this size is little bit bigger than original buffer?
		samples.setComplete(true);
		samples.setTimeBase(Rational.make(1, 44100));
		samples.setTimeStamp(0);
		samples.setNumSamples(sampleNum);
		return samples;
	}

	private static void playSound(String filename) throws InterruptedException, IOException {
		/*
		 * Start by creating a container object, in this case a demuxer since
		 * we are reading, to get audio data from.
		 */
		Demuxer demuxer = Demuxer.make();

		/*
		 * Open the demuxer with the filename passed on.
		 */
		demuxer.open(filename, null, false, true, null, null);

		/*
		 * Query how many streams the call to open found
		 */
		int numStreams = demuxer.getNumStreams();

		/*
		 * Iterate through the streams to find the first audio stream
		 */
		int audioStreamId = -1;
		Decoder audioDecoder = null;
		for(int i = 0; i < numStreams; i++)
		{
			final DemuxerStream stream = demuxer.getStream(i);
			final Decoder decoder = stream.getDecoder();
			if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO) {
				audioStreamId = i;
				audioDecoder = decoder;
				// stop at the first one.
				break;
			}
		}
		if (audioStreamId == -1)
			throw new RuntimeException("could not find audio stream in container: "+filename);

		/*
		 * Now we have found the audio stream in this file.  Let's open up our decoder so it can
		 * do work.
		 */
		audioDecoder.open(null, null);

		/*
		 * We allocate a set of samples with the same number of channels as the
		 * coder tells us is in this buffer.
		 */
		final MediaAudio samples = MediaAudio.make(
				audioDecoder.getFrameSize(),
				audioDecoder.getSampleRate(),
				audioDecoder.getChannels(),
				audioDecoder.getChannelLayout(),
				audioDecoder.getSampleFormat());

		/*
		 * A converter object we'll use to convert Humble Audio to a format that
		 * Java Audio can actually play. The details are complicated, but essentially
		 * this converts any audio format (represented in the samples object) into
		 * a default audio format suitable for Java's speaker system (which will
		 * be signed 16-bit audio, stereo (2-channels), resampled to 22,050 samples
		 * per second).
		 */

		final MediaAudioConverter converter =
				MediaAudioConverterFactory.createConverter(
						MediaAudioConverterFactory.DEFAULT_JAVA_AUDIO,
						samples);

		/*
		 * An AudioFrame is a wrapper for the Java Sound system that abstracts away
		 * some stuff. Go read the source code if you want -- it's not very complicated.
		 */
		final AudioFrame audioFrame = AudioFrame.make(converter.getJavaFormat());

		/* We will use this to cache the raw-audio we pass to and from
		 * the java sound system.
		 */
		ByteBuffer rawAudio = null;

		/*
		 * Now, we start walking through the container looking at each packet. This
		 * is a decoding loop, and as you work with Humble you'll write a lot
		 * of these.
		 * 
		 * Notice how in this loop we reuse all of our objects to avoid
		 * reallocating them. Each call to Humble resets objects to avoid
		 * unnecessary reallocation.
		 */
		final MediaPacket packet = MediaPacket.make();

		while(demuxer.read(packet) >= 0) {
			/*
			 * Now we have a packet, let's see if it belongs to our audio stream
			 */
			if (packet.getStreamIndex() == audioStreamId)
			{
				/*
				 * A packet can actually contain multiple sets of samples (or frames of samples
				 * in audio-decoding speak).  So, we may need to call decode audio multiple
				 * times at different offsets in the packet's data.  We capture that here.
				 */
				int offset = 0;
				int bytesRead = 0;
				do {
					bytesRead += audioDecoder.decode(samples, packet, offset);
					if (samples.isComplete()) {
						rawAudio = converter.toJavaAudio(rawAudio, samples);
						audioFrame.play(rawAudio);
					}
					offset += bytesRead;
				} while (offset < packet.getSize());
			}
		}

		// Some audio decoders (especially advanced ones) will cache
		// audio data before they begin decoding, so when you are done you need
		// to flush them. The convention to flush Encoders or Decoders in Humble Video
		// is to keep passing in null until incomplete samples or packets are returned.
		do {
			audioDecoder.decode(samples, null, 0);
			if (samples.isComplete()) {
				rawAudio = converter.toJavaAudio(rawAudio, samples);
				audioFrame.play(rawAudio);
			}
		} while (samples.isComplete());

		// It is good practice to close demuxers when you're done to free
		// up file handles. Humble will EVENTUALLY detect if nothing else
		// references this demuxer and close it then, but get in the habit
		// of cleaning up after yourself, and your future girlfriend/boyfriend
		// will appreciate it.
		demuxer.close();

		// similar with the demuxer, for the audio playback stuff, clean up after yourself.
		audioFrame.dispose();
	}

	private static void addAudios(File[] audioFiles, IMediaWriter writer) 
	{
		addAudios(audioFiles, writer, Long.MAX_VALUE, false);
	}

	private static void addAudios(File[] audioFiles, IMediaWriter writer, long maxDurInSec, boolean useTimeShift) 
	{

		IContainer[] containerAudio =  new IContainer[audioFiles.length];
		IStreamCoder[] audioCoders = new IStreamCoder[audioFiles.length];

		for(int i = 0; i < audioFiles.length; i++){

			int audioStreamt = 0;

			IStream stream = null;
			IStreamCoder code = null;

			containerAudio[i] = IContainer.make();
			if (containerAudio[i].open(audioFiles[i].getPath(), IContainer.Type.READ, null) < 0)
				throw new IllegalArgumentException("Cant find " + audioFiles[i].getPath());

			for(int j=0; i<containerAudio[i].getNumStreams(); j++){
				//for(int j=0; j<containerAudio[j].getNumStreams(); j++){
				stream = containerAudio[j].getStream(j);
				code = stream.getStreamCoder();

				if(code.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
				{
					audioStreamt = j;
					break;
				}

				stream = null;
				code = null;
			}

			audioCoders[i] = containerAudio[i].getStream(audioStreamt).getStreamCoder();
			audioCoders[i].open(null,null);
		}


		writer.addAudioStream(audioStreamIndex, audioStreamId, audioCoders[0].getChannels(), audioCoders[0].getSampleRate());

		for(int i = 0; i < audioFiles.length; i++){

			IAudioSamples samples = IAudioSamples.make(audioCoders[i].getSampleRate(), audioCoders[i].getChannels(),IAudioSamples.Format.FMT_S32);  
			IPacket packetaudio = IPacket.make();

			int bytesDecodedaudio = -1;
			int offset = 0;
			int totalReadBites = 0;

			int bitRate = audioCoders[i].getBitRate()/8;
			long fileSize = audioFiles[i].length();

			//using time shift
			int audioShiftTime = 0;
			if(useTimeShift){
				audioShiftTime = rnd.nextInt((int)fileSize - (int)(maxDurInSec * bitRate));
			}

			while(containerAudio[i].readNextPacket(packetaudio) >= 0){

				offset = 0;

				while( offset < packetaudio.getSize() )
				{
					bytesDecodedaudio = audioCoders[i].decodeAudio(samples,	packetaudio, offset);

					if (bytesDecodedaudio < 0)
						throw new RuntimeException("could not detect audio");

					offset += bytesDecodedaudio;
					totalReadBites += bytesDecodedaudio;

					if(audioShiftTime <= totalReadBites){
						if (samples.isComplete())
						{
							//start endode audio from audioShiftTime
							writer.encodeAudio(audioStreamIndex, samples);
						}
					}
				}

				if( totalReadBites > audioShiftTime + maxDurInSec*bitRate ){
					break;
				}
			}

			packetaudio = null;

			samples.delete();
			/*audioCoder.release();
			containerAudio.release();*/
			audioCoders[i].close();
			containerAudio[i].close();
		}
	}



	private static long getAudioFileDurationInMilliSec(File audioFile) throws Exception{
		AudioFileFormat fileFormat = null;
		Long duration = 0L;

		try{
			/*AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
		AudioFormat format = audioInputStream.getFormat();
		long frames = audioInputStream.getFrameLength();
		double durationInSeconds = (frames+0.0) / format.getFrameRate();

		return durationInSeconds;*/
			/*fileFormat = AudioSystem.getAudioFileFormat(audioFile);
			log.info(String.format("File '%s' has format '%s'", audioFile.getName(), fileFormat));
			if (fileFormat instanceof TAudioFileFormat) {
				Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
				String key = "duration";
				microseconds = (Long) properties.get(key);
			} else {
				throw new UnsupportedAudioFileException();
			}*/
			Mp3File mp3file = new Mp3File(audioFile);
			duration = mp3file.getLengthInMilliseconds();
		}catch(IOException | UnsupportedTagException | InvalidDataException e){
			log.error(String.format("File '%s' has format '%s'. Error:", audioFile.getName(), fileFormat), e);
			throw e;
		}

		return duration;
	}

	private static File getFrameImage(File[] files, boolean useFileOrder, int frameOrderIndex){
		Random rnd = new Random();
		rnd.nextInt();

		if(!useFileOrder){
			return files[rnd.nextInt(files.length)];
		}else{
			return files[frameOrderIndex];
		}
	}

	/**
	 * 
	 * @param duration - total frame count
	 * @param intCount - interval count
	 * @return
	 */
	private static int calculateInterval(int duration, int intCount){
		Random rnd = new Random();
		int hardInt = duration/intCount;
		return hardInt + (int)Math.pow(-1, rnd.nextInt(2)) * rnd.nextInt(hardInt+1)/10;
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
