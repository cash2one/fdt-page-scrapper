package com.fdt.dailymotion.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.WordUtils;
import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

import com.fdt.dailymotion.VideoTaskRunner;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;
import com.xuggle.xuggler.Converter;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.fdt", excludeFilters={
		@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=VideoTaskRunner.class)})
@PropertySource(value="file:${config.file}",ignoreResourceNotFound = true)
public class ComplexVideoGenerator 
{
	static final Logger log = Logger.getLogger(ComplexVideoGenerator.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";

	private final static String BOOKS_FOLDER_PATH_LABEL = "books_folder_path";

	private final static String IMAGES_FOLDER_PATH_LABEL = "images_folder_path";

	private final static String CLICK_FILE_PATH_LABEL = "click_file_path";

	private final static String OUTPUT_FOLDER_PATH_LABEL = "output_folder_path";

	private final static String AUDIO_VOICE_LABEL = "audio_voice";
	private final static String AUDIO_SPEED_LABEL = "audio_speed";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private String booksFolderPath;

	private File imagesFolder;

	private File outputFolder;

	private File clickFile;

	@Autowired
	private ProxyFactory proxyFactory;

	private AtomicInteger currentThreadCount = new AtomicInteger(0);

	private byte audioVoice = 1;

	private byte audioSpeed = 1;

	private Integer maxThreadCount = 1;

	private Long sleepTime = 50L;

	private ArrayList<File> booksFileList = new ArrayList<File>();

	private static Random rnd = new Random();

	@Autowired
	private ConfigManager cfgMgr;


	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_complex_video_generator.xml");
		try{

			System.out.println(System.getProperty("app.home"));
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			ApplicationContext ctx = SpringApplication.run(ComplexVideoGenerator.class, args);

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}

			DOMConfigurator.configure("log4j.xml");
			ComplexVideoGenerator checker = ctx.getBean(ComplexVideoGenerator.class);
			checker.execute();

		}catch(Exception e){
			log.error("Error occured during replacer executor: ", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}


	public ComplexVideoGenerator(){
		super();
	}

	@PostConstruct
	private void init() throws IOException{
		this.booksFolderPath = ConfigManager.getInstance().getProperty(BOOKS_FOLDER_PATH_LABEL);

		this.imagesFolder = new File(ConfigManager.getInstance().getProperty(IMAGES_FOLDER_PATH_LABEL));

		this.clickFile = new File(ConfigManager.getInstance().getProperty(CLICK_FILE_PATH_LABEL));

		this.outputFolder = new File(ConfigManager.getInstance().getProperty(OUTPUT_FOLDER_PATH_LABEL));

		this.audioSpeed = Byte.valueOf(ConfigManager.getInstance().getProperty(AUDIO_SPEED_LABEL));

		this.audioVoice = Byte.valueOf(ConfigManager.getInstance().getProperty(AUDIO_VOICE_LABEL));

		proxyFactory.setDelayProxy(1L);

		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
	}



	private void execute() throws IOException, InterruptedException
	{
		//load list of input files
		this.booksFileList = new ArrayList<File>(Arrays.asList(new File(this.booksFolderPath).listFiles()));

		//

		do{
			while(booksFileList.size() > 0)
			{
				File book = booksFileList.get(0);
				if(currentThreadCount.get() < maxThreadCount){
					ComplexVideoGeneratorThread rplcrThrd = new ComplexVideoGeneratorThread(book, clickFile, this.outputFolder, this.imagesFolder, this, proxyFactory);
					removeFirstFileFromList();
					rplcrThrd.start();
				}else{
					Thread.sleep(500L);
				}
			}
		}while(booksFileList.size() > 0 || currentThreadCount.get() > 0);

		while( currentThreadCount.get() > 0){
			Thread.sleep(sleepTime);
		}
	}

	private void returnFile2List(File file){
		synchronized (booksFileList) {
			booksFileList.add(file);
		}
	}

	private File removeFirstFileFromList(){
		synchronized (booksFileList) {
			return booksFileList.remove(0);
		}
	}

	public class ComplexVideoGeneratorThread extends Thread
	{
		private final File bookFile;
		private final File clickFile;
		private final File outputFolder;
		private final File imagesFolder;
		private ComplexVideoGenerator generator;
		private ProxyFactory proxyFactory;

		private File privateFolder;
		private File audioGenFolder;
		private File imagesGenFolder;

		public ComplexVideoGeneratorThread(File bookFile, File clickFile, File outputFolder, File imagesFolder, ComplexVideoGenerator generator, ProxyFactory proxyFactory) {
			super();
			this.bookFile = bookFile;
			this.clickFile = clickFile;
			this.outputFolder = outputFolder;
			this.imagesFolder = imagesFolder;
			this.generator = generator;
			this.proxyFactory = proxyFactory;

			this.privateFolder = new File(outputFolder,this.bookFile.getName());
			this.audioGenFolder = new File(this.privateFolder,"audio");
			this.imagesGenFolder = new File(this.privateFolder,"images");
		}

		public void start(){
			this.generator.incThrdCnt();
			super.start();
		}

		public void run()
		{
			boolean isErrorExist = false;

			int repeatCount = 0;

			//TODO Check that folder was created


			try{
				do{
					repeatCount++;
					isErrorExist = false;

					try{
						// Create private folder for book
						this.privateFolder.mkdir();
						//clearFolder(this.privateFolder);
						this.audioGenFolder.mkdir();
						//clearFolder(this.audioGenFolder);
						this.imagesGenFolder.mkdir();
						//clearFolder(this.imagesGenFolder);

						//Parse input book file
						ArrayList<String> speech = parseBookFile(this.bookFile, this.clickFile, 150);

						//Generate images and Get sentence length
						int ingIdx = 1;
						/*generateImages(
								WordUtils.wrap(speech.get(0), 50, System.lineSeparator(), true).split(System.lineSeparator()), 
								imagesGenFolder, 
								imagesFolder,
								ingIdx++
								);

						generateImages(
								WordUtils.wrap(speech.get(1), 50, System.lineSeparator(), true).split(System.lineSeparator()), 
								imagesGenFolder, 
								imagesFolder,
								ingIdx++
								);*/

						for(int i = 0; i < speech.size(); i++){
							generateImages(
									WordUtils.wrap(speech.get(i), 50, System.lineSeparator(), true).split(System.lineSeparator()), 
									imagesGenFolder, 
									imagesFolder,
									ingIdx++
									);
						}

						// Generate audio[1..n]
						generateAudio(speech, audioVoice, audioSpeed, audioGenFolder);
						//VideoCreator.makeVideoByOrder(new File(this.privateFolder, "video_new.mov").getAbsolutePath(), this.imagesGenFolder.listFiles(), this.audioGenFolder.listFiles(), 25);
						VideoCreator.recordScreen(new File(this.privateFolder, "video_new.mov").getAbsolutePath(),null, this.imagesGenFolder.listFiles(), this.audioGenFolder.listFiles(), 25);
						//VideoCreator.makeVideoByOrder("video_new_old_gen.mov", this.imagesGenFolder.listFiles(), this.audioGenFolder.listFiles(), 25);
						//transcode(new File("video_new_old_gen.mov"), new File("video_new_old_gen_converted.mov"));
						
						//VideoCreator.makeVideoByOrder(new File(this.privateFolder, "video_new.mp4").getAbsolutePath(), this.imagesGenFolder.listFiles(), this.audioGenFolder.listFiles(), 25);

					}
					catch(Exception e){
						isErrorExist = true;
						log.warn("Error occured during generating video for file: " + bookFile.getName(), e);
					}
					finally{
					}
				}
				while(isErrorExist && repeatCount <= 10);
			}
			catch(Exception e){
				isErrorExist = true;
				log.warn("Error occured during generating video for file: " + bookFile.getName(), e);
			}finally{
				//TODO if process was not successfull - delete private folder
				generator.decThrdCnt();
			}
		}
	}

	public void transcode(File inputFile, File presetsFile) {
		//This is the converter object we will use.
		Converter converter = new Converter();

		//These are the arguments to pass to the converter object.
		//For H264 transcoding, the -vpreset option is very
		//important. Here, presetsFile is a File object corresponding
		//to a libx264 video presets file. These are in the
		// /usr/local/share/ffmpeg directory.
		String[] arguments = {
				inputFile.getAbsolutePath(),
				"-acodec", "libfaac",
				"-asamplerate", "44100",
				"-vcodec", "libx264",
				//"-vpreset", presetsFile.getAbsolutePath(),
				inputFile.getName() + ".mp4"
		};

		try {
			//Finally, we run the transcoder with the options we provided.
			converter.run(
					converter.parseOptions(
							converter.defineOptions(), arguments)
					);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param speech - must be wrapped
	 * @throws Exception 
	 * @throws XPathExpressionException 
	 */
	private void generateAudio(ArrayList<String> speech, int voice, int speed, File audioOutFolder) throws XPathExpressionException, Exception
	{
		//TODO Get correct proxy type
		AudioSpeecherCreator checker = 
				new AudioSpeecherCreator(
						speech, 
						voice,
						speed,
						audioOutFolder, 
						this.maxThreadCount,
						proxyFactory
						);

		checker.execute();
	}

	/**
	 * 
	 * @param speech - must be wrapped
	 */
	public static void generateImages(String[] speech, File imageOutFodler, File rndImgFolder, int imgIdx){

		try {
			//get rand images
			File[] imgTmplFiles = rndImgFolder.listFiles();

			File rndImgTmplFile = imgTmplFiles[rnd.nextInt(imgTmplFiles.length)];
			File newImgFile = new File(imageOutFodler, (String.format("%03d",imgIdx)) + ".png");

			drawSpeech2Img(speech, rndImgTmplFile, newImgFile);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public static void drawSpeech2Img(String[] speech, File rndImgTmplFile, File newImgFile) throws IOException {
		BufferedImage image = ImageIO.read(rndImgTmplFile);
		Graphics g = image.getGraphics();
		Font myFont = new Font("Serif", Font.BOLD, 12);
		Font newFont = myFont.deriveFont(40F);

		g.setFont(newFont);
		g.setColor(Color.BLACK);


		for(int i = 0; i < speech.length; i++)
		{
			g.drawString(speech[i], 100, 100 + (i) * 50 );
		}

		g.dispose();
		ImageIO.write(image, "png", newImgFile);
	}

	private void clearFolder(File folderPath){
		for(File file : folderPath.listFiles()){
			file.delete();
		}
	}

	private ArrayList<String> parseBookFile(File inputBookFile, File clickFile, int wrapLength){
		ArrayList<String> speech = new ArrayList<String>();

		List<String> fileStrs = Utils.loadFileAsStrList(inputBookFile);

		String clickFileStr =  Utils.loadFileAsStrList(clickFile).get(0);

		String title = fileStrs.get(0);
		String author = fileStrs.get(2);

		speech.add(clickFileStr.replaceAll( "\\[KEYWORD\\]",  Matcher.quoteReplacement(title+ " " + author) ));

		/*//add title of book
		speech.add(title);
		//add author of book
		speech.add(author);*/

		boolean overviewBlock = false;

		for(int i = 3; i < fileStrs.size(); i++){
			String str4Check = cleanDataFile(fileStrs.get(i));
			str4Check = str4Check.trim();

			if(!"".equals(str4Check)){
				if(str4Check.indexOf("Editorial Reviews") > -1 || str4Check.indexOf("Related Subjects") > -1){
					break;
				}else if(overviewBlock || str4Check.indexOf("Overview") > -1){
					if(!overviewBlock){
						overviewBlock = true;
						continue;
					}
					speech.addAll(Arrays.asList(WordUtils.wrap(str4Check, wrapLength, System.lineSeparator(), true).split(System.lineSeparator())));
				}
			}
		}

		return speech;
	}

	private synchronized void appendLinesToFile(String line, File file, boolean append) {
		ArrayList<String> lines = new ArrayList<>();
		lines.add(line);
		appendLinesToFile(lines, file, append);
	}

	private  void appendLinesToFile(ArrayList<String> lines, File file, boolean append) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file,append), "UTF8"));
			for(String line: lines){
				bufferedWriter.append(line);
			}
		} catch (IOException e) {
			log.warn(String.format("Error occured during saving collection string to file %s", file.getName()));
		} finally {
			//Close the BufferedWriter
			if (bufferedWriter != null) {
				try {
					bufferedWriter.flush();
					bufferedWriter.close();
				} catch (IOException e) {
					log.warn(String.format("Error occured closing file %s", file.getName()));
				}
			}
		}
	}

	private void incThrdCnt(){
		currentThreadCount.incrementAndGet();
		log.debug("Current thread count: " + currentThreadCount);
	}

	private void decThrdCnt(){
		currentThreadCount.decrementAndGet();
		log.debug("Current thread count: " + currentThreadCount);
	}

	public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params)
		{
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	private String cleanDataFile(String input){

		input = input.replaceAll("<dt>(.*?)</dt>", " ")
				.replaceAll("<dd>(.*?)</dd>", "$1")
				.replaceAll("<h2>Product Details</h2>", " ")
				//.replaceAll("<h2>Overview</h2>", " ")
				.replaceAll("<div><b>From the Publisher</b></div>", " ")
				//.replaceAll("<h2>Editorial Reviews</h2>", " ")
				//.replaceAll("<h2>Related Subjects</h2>", " ")
				.replaceAll("<[^>]*>(.*?)</[^>]*>", "$1")
				.replaceAll("<[^>]*?>", " ")
				.replaceAll("</[^>]*?>", " ")
				.replaceAll("[\\.]+", " ")
				.replaceAll("[\\.;\"\\(\\)\\[\\]\\{\\}:<>]+", " ")
				.replaceAll("\\s+", " ");

		return input;
	}
}

