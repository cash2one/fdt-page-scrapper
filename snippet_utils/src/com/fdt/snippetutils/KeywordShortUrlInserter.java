package com.fdt.snippetutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONObject;

import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.PreConditionCheck;

public class KeywordShortUrlInserter {

	private static final Logger log = Logger.getLogger(KeywordShortUrlInserter.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";

	private final static String SHORT_URL_SITE_LABEL = "short_url_site";
	private final static String SHORT_URL_LOGIN_LABEL = "short_url_login";
	private final static String SHORT_URL_PASS_LABEL = "short_url_pass";
	
	private final static String PROMO_URL_SITE_LABEL = "promo_url_site";

	private final static String INPUT_FILE_FOLDER_PATH_LABEL = "input_file_folder_path";
	private final static String OUTPUT_FOLDER_PATH_LABEL = "output_folder_path";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private String inputFileFolderPath;
	private String outputFolderPath;

	private String shortUrlSite = "";
	private String shortUrlLogin = "";
	private String shortUrlPass = "";
	
	private String promoUrl = "";

	private AtomicInteger currentThreadCount = new AtomicInteger(0);
	private Integer maxThreadCount = 1;
	private Long sleepTime = 50L;

	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_short_url.xml");
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

				KeywordShortUrlInserter inserter = new KeywordShortUrlInserter();
				inserter.execute();
			}

		}catch(Exception e){
			log.error("Error occured during replacer executor: ", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public KeywordShortUrlInserter() throws IOException {
		super();
		this.inputFileFolderPath = ConfigManager.getInstance().getProperty(INPUT_FILE_FOLDER_PATH_LABEL);
		PreConditionCheck.notEmpty(this.inputFileFolderPath,"Не указан путь к папке с файлами");

		this.outputFolderPath = ConfigManager.getInstance().getProperty(OUTPUT_FOLDER_PATH_LABEL);
		PreConditionCheck.notEmpty(this.outputFolderPath,"Не указан путь к папке куда будут сохраняться результаты");

		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));
		//set default value 10 for thread count
		this.maxThreadCount = this.maxThreadCount > 0? this.maxThreadCount:10;
		

		this.shortUrlSite = ConfigManager.getInstance().getProperty(SHORT_URL_SITE_LABEL);
		PreConditionCheck.notEmpty(this.shortUrlSite,"Не указан сайт для SHORTURL");

		this.shortUrlLogin = ConfigManager.getInstance().getProperty(SHORT_URL_LOGIN_LABEL);
		PreConditionCheck.notEmpty(this.shortUrlLogin,"Не указан логин для доступа к SHORTURL сайту");

		this.shortUrlPass = ConfigManager.getInstance().getProperty(SHORT_URL_PASS_LABEL);
		PreConditionCheck.notEmpty(this.shortUrlPass,"Не указан пароль для доступа к SHORTURL сайту");
		
		
		this.promoUrl = ConfigManager.getInstance().getProperty(PROMO_URL_SITE_LABEL);
		PreConditionCheck.notEmpty(this.promoUrl,"Не указан PROMO URL");

	}

	private void execute() throws IOException, InterruptedException{

		File[] filesArray = new File(this.inputFileFolderPath).listFiles();
		ArrayList<File> files= new ArrayList<File>(Arrays.asList(filesArray));

		ExecutorService extSrv = Executors.newFixedThreadPool(maxThreadCount);

		while(files.size() > 0){
			InserterThread rplcrThrd = new InserterThread(this, files.remove(0));
			extSrv.submit(rplcrThrd);
		}

		while( currentThreadCount.get() > 0 ){
			Thread.sleep(sleepTime);
		}
		
		extSrv.shutdown();
	}

	private class InserterThread implements Runnable{

		private File file;
		private KeywordShortUrlInserter replacer;

		public InserterThread(KeywordShortUrlInserter replacer, File file) {
			super();
			this.replacer = replacer;
			this.file = file;
		}

		@Override
		public void run() {
			replacer.incThrdCnt();

			try{
				//TODO replace [KEYWORD] with short url
				ArrayList<String> lines = parseFile(file);
				String key = lines.get(0).replaceAll("[^a-zA-Z0-9\\s]+","").replaceAll("\\s+", "+");
				
				String shortUrl = getShortUrl(
						replacer.shortUrlSite, 
						replacer.promoUrl+key, 
						replacer.shortUrlLogin, 
						replacer.shortUrlPass
					);
				
				PreConditionCheck.notEmpty(shortUrl, "Short url could not be empty");
				
				for(int i = 0; i < lines.size(); i++){
					if(lines.get(i).equalsIgnoreCase("[KEYWORD]")){
						lines.set(i, shortUrl);
					}
				}
				//Save file
				File fileToSave = new File(outputFolderPath, file.getName());
				appendLinesToFile(lines, fileToSave, false);
				file.delete();
			} catch (Exception e) {
				log.error("Error occured during getting of short url for file" + file.getName() , e);
			}finally{
				replacer.decThrdCnt();
			}
		}
	}

	private String getShortUrl(String shortUrlSite, String url4Short, String userName, String userPass) throws Exception{
		HttpURLConnection conn = null;
		String uploadUrl = ""; 
		try{
			//post news
			URL url = new URL(shortUrlSite + "yourls-api.php");
			HttpURLConnection.setFollowRedirects(false);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getShorturlParamString(url4Short, userName, userPass));
			writer.flush();
			writer.close();
			os.close();

			StringBuilder responseStr = getResponseAsString(conn);

			log.trace(responseStr.toString());

			conn.disconnect();

			JSONObject jsonObj = new JSONObject(responseStr.toString());
			uploadUrl = jsonObj.getString("shorturl");

			log.info("Upload URL: " + uploadUrl);
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return uploadUrl;
	}

	private String getShorturlParamString(String urlForShort, String userName, String userPass) throws UnsupportedEncodingException{
		StringBuilder params = new StringBuilder();
		params.append("format=json").append("&");
		params.append("action=shorturl").append("&");
		params.append("username=").append(userName).append("&");
		params.append("password=").append(userPass).append("&");
		params.append("url=").append(URLEncoder.encode(urlForShort, "UTF8"));

		return params.toString();
	}

	private StringBuilder getResponseAsString(HttpURLConnection conn)
			throws IOException {
		InputStream is = conn.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder responseStr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			responseStr.append(line).append("\r\n");
		}
		is.close();
		return responseStr;
	}

	public ArrayList<String> parseFile(File inputFile) throws IOException {
		//read account list
		FileReader fr = null;
		BufferedReader br = null;

		ArrayList<String> fileAsStr = new ArrayList<String>();

		try {
			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
				fileAsStr.add(line);
			}
			log.trace("File content: " + fileAsStr.toString());

			return fileAsStr;
		}
		finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
			try {
				if(fr != null)
					fr.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
	}

	private synchronized static void appendLinesToFile(String line, File file, boolean append) {
		ArrayList<String> lines = new ArrayList<>();
		lines.add(line);
		appendLinesToFile(lines, file, append);
	}

	private static void appendLinesToFile(ArrayList<String> lines, File file, boolean append) {
		if(!append){
			if(file.exists()){
				file.delete();
			}
		}

		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file,append), "UTF8"));
			for(String line: lines){
				bufferedWriter.append(line);
				bufferedWriter.newLine();
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
}
