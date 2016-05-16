package com.fdt.dailymotion.util;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

public class ComplexVideoGenerator 
{
	static final Logger log = Logger.getLogger(ComplexVideoGenerator.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";

	private final static String BOOKS_FOLDER_PATH_LABEL = "books_folder_path";

	private final static String IMAGES_FOLDER_PATH_LABEL = "images_folder_path";

	private final static String OUTPUT_FOLDER_PATH_LABEL = "output_folder_path";

	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String PROXY_TYPE_LABEL = "proxy_type";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private String booksFolderPath;

	private final File imagesFolder;

	private final File outputFolder;

	private ProxyFactory proxyFactory;

	private AtomicInteger currentThreadCount = new AtomicInteger(0);

	private Integer maxThreadCount = 1;

	private Long sleepTime = 50L;

	private ArrayList<File> booksFileList = new ArrayList<File>();

	private Random rnd = new Random();


	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_complex_video_generator.xml");
		try{
			if(args.length < 1){
				System.out.println("Some arguments are absent. Please use next list of arguments: 1 config file");
				System.exit(-1);
			}else{
				System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
				ConfigManager.getInstance().loadProperties(args[0]);
				System.out.println(args[0]);
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(
								ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL),
								ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
								);
					}
				});

				ComplexVideoGenerator checker = new ComplexVideoGenerator();
				checker.execute();
			}

		}catch(Exception e){
			log.error("Error occured during replacer executor: ", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public ComplexVideoGenerator() throws IOException {
		super();
		this.booksFolderPath = ConfigManager.getInstance().getProperty(BOOKS_FOLDER_PATH_LABEL);

		this.imagesFolder = new File(ConfigManager.getInstance().getProperty(IMAGES_FOLDER_PATH_LABEL));

		this.outputFolder = new File(ConfigManager.getInstance().getProperty(OUTPUT_FOLDER_PATH_LABEL));

		ProxyFactory.DELAY_FOR_PROXY = Integer.valueOf(ConfigManager.getInstance().getProperty(PROXY_DELAY_LABEL));
		ProxyFactory.PROXY_TYPE = ConfigManager.getInstance().getProperty(PROXY_TYPE_LABEL);
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL));

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
					ComplexVideoGeneratorThread rplcrThrd = new ComplexVideoGeneratorThread(book, this.outputFolder, this.imagesFolder, this, proxyFactory);
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
		private final File outputFolder;
		private final File imagesFolder;
		private ComplexVideoGenerator generator;
		private ProxyFactory proxyFactory;

		private File privateFolder;
		private File audioGenFolder;
		private File imagesGenFolder;

		public ComplexVideoGeneratorThread(File bookFile, File outputFolder, File imagesFolder, ComplexVideoGenerator generator, ProxyFactory proxyFactory) {
			super();
			this.bookFile = bookFile;
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

			ProxyConnector proxyConnector = null;

			int repeatCount = 0;

			//TODO Check that folder was created


			try{
				do{
					repeatCount++;
					isErrorExist = false;
					proxyConnector = proxyFactory.getRandomProxyConnector();

					String proxyTypeStr = ConfigManager.getInstance().getProperty("proxy_type");
					Proxy proxy = null;

					try{
						//TODO Create private folder for book
						/*this.privateFolder.mkdir();
						clearFolder(this.privateFolder);
						this.audioGenFolder.mkdir();
						clearFolder(this.audioGenFolder);
						this.imagesGenFolder.mkdir();
						clearFolder(this.imagesGenFolder);

						//Parse input book file
						ArrayList<String> speech = parseBookFile(this.bookFile, 150);

						//TODO Generate images and Get sentence length
						int ingIdx = 1;
						generateImages(
								new String[]{speech.get(0)}, 
								imagesGenFolder, 
								imagesFolder,
								ingIdx++
								);

						generateImages(
								new String[]{speech.get(1)}, 
								imagesGenFolder, 
								imagesFolder,
								ingIdx++
								);

						for(int i = 2; i < speech.size(); i++){
							generateImages(
									WordUtils.wrap(speech.get(i), 50, System.lineSeparator(), true).split(System.lineSeparator()), 
									imagesGenFolder, 
									imagesFolder,
									ingIdx++
									);
						}
						
						//TODO Generate audio[1..n]
						generateAudio(speech, audioGenFolder);*/

						//TODO Generate compile file for video generator
						VideoCreator.makeVideoByOrder(new File(this.privateFolder, "video.mp4").getAbsolutePath(), this.imagesGenFolder.listFiles(), this.audioGenFolder.listFiles(), 25);
						
						//TODO Generate video
					}
					catch(Exception e){
						isErrorExist = true;
						log.warn("Error occured during generating video for file: " + bookFile.getName(), e);
					}
					finally{
						if(proxyConnector != null){
							proxyFactory.releaseProxy(proxyConnector);
							proxyConnector = null;
						}
					}
				}
				while(isErrorExist && repeatCount <= 10);
			}finally{
				//TODO if process was not successfull - delete private folder
				generator.decThrdCnt();
			}
		}
	}

	/**
	 * 
	 * @param speech - must be wrapped
	 * @throws Exception 
	 * @throws XPathExpressionException 
	 */
	private void generateAudio(ArrayList<String> speech, File audioOutFolder) throws XPathExpressionException, Exception
	{
		//TODO Get correct proxy type
		AudioSpeecherCreator checker = 
				new AudioSpeecherCreator(
						speech, 
						audioOutFolder, 
						ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL), 
						"HTTP"
					);
		
		checker.execute();
	}

	/**
	 * 
	 * @param speech - must be wrapped
	 */
	private void generateImages(String[] speech, File imageOutFodler, File rndImgFolder, int imgIdx){

		try {
			//get rand images
			File[] imgTmplFiles = rndImgFolder.listFiles();

			File rndImgTmplFile = imgTmplFiles[rnd.nextInt(imgTmplFiles.length)];
			File newImgFile = new File(imageOutFodler, String.valueOf(imgIdx) + ".png");

			BufferedImage image = ImageIO.read(rndImgTmplFile);
			Graphics g = image.getGraphics();
			Font myFont = new Font("Serif", Font.BOLD, 12);
			Font newFont = myFont.deriveFont(40F);

			g.setFont(newFont);

			
			for(int i = 0; i < speech.length; i++)
			{
				g.drawString(speech[i], 100, 100 + (i) * 50 );
			}
			
			g.dispose();
			ImageIO.write(image, "png", newImgFile);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private void clearFolder(File folderPath){
		for(File file : folderPath.listFiles()){
			file.delete();
		}
	}

	private ArrayList<String> parseBookFile(File inputBookFile, int wrapLength){
		ArrayList<String> speech = new ArrayList<String>();

		List<String> fileStrs = Utils.loadFileAsStrList(inputBookFile);


		//add title of book
		speech.add(fileStrs.get(0));
		//add author of book
		speech.add(fileStrs.get(2));

		boolean overviewBlock = false;

		for(int i = 3; i < fileStrs.size(); i++){
			String str4Check = cleanDataFile(fileStrs.get(i));
			str4Check = str4Check.trim();

			if(!"".equals(str4Check)){
				if(str4Check.indexOf("Editorial Reviews") > -1){
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
				.replaceAll("<h2>Related Subjects</h2>", " ")
				.replaceAll("<[^>]*>(.*?)</[^>]*>", "$1")
				.replaceAll("<[^>]*?>", " ")
				.replaceAll("</[^>]*?>", " ")
				.replaceAll("[\\.]+", " ")
				.replaceAll("[\\.;\"\\(\\)\\[\\]\\{\\}:<>]+", " ")
				.replaceAll("\\s+", " ");

		return input;
	}
}

