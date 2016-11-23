package com.fdt.dailymotion.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fdt.dailymotion.VideoTaskRunner;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

public class AudioSpeecherCreator {

	private static final Logger log = Logger.getLogger(AudioSpeecherCreator.class);

	private static final String LINE_FEED = "\r\n";

	private List<String> sentences = new ArrayList<String>();
	
	private int audioVoice = 1;
	
	private int audioSpeed = 1;

	private File outputFolder = null;

	@Autowired
	private ProxyFactory proxyFactory;

	private AtomicInteger currentThreadCount = new AtomicInteger(0);

	private Integer maxThreadCount = 1;

	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_speech_generator.xml");
		
		try{
			ApplicationContext ctx = SpringApplication.run(AudioSpeecherCreator.class, args);

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
			
			AudioSpeecherCreator checker = ctx.getBean(AudioSpeecherCreator.class);
			DOMConfigurator.configure("log4j_speech_generator.xml");
			checker.execute();
			System.out.print("Program execution finished");
			System.exit(0);
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}

	public AudioSpeecherCreator(List<String> sentences, int audioVoice, int audioSpeed, File outputFolderPath, int maxThreadCount, ProxyFactory proxyFactory) throws IOException {
		super();
		this.sentences = sentences;
		this.outputFolder = outputFolderPath;

		this.maxThreadCount = maxThreadCount;

		this.audioVoice = audioVoice;
		this.audioSpeed = audioSpeed;
		
		this.proxyFactory = proxyFactory;
	}

	private void incThrdCnt(){
		currentThreadCount.incrementAndGet();
		log.debug("Current thread count: " + currentThreadCount);
	}

	private void decThrdCnt(){
		currentThreadCount.decrementAndGet();
		log.debug("Current thread count: " + currentThreadCount);
	}

	public void execute() throws XPathExpressionException, Exception
	{
		ProxyConnector proxyConnector = null;
		String requestToken = "";

		try{
			proxyConnector = proxyFactory.getRandomProxyConnector();
			requestToken = getRequestToken(proxyConnector.getConnect(proxyFactory.getProxyType()));
		}finally{
			if(proxyConnector != null){
				proxyFactory.releaseProxy(proxyConnector);
			}
		}

		int fileIdx = 1;
		
		do{
			while(sentences.size() > 0)
			{
				if(currentThreadCount.get() < maxThreadCount){
					//TODO Getting lang and speed values
					AudioSpeecherCreatorThread creatorThrd = new AudioSpeecherCreatorThread(sentences.remove(0), audioVoice, audioSpeed, requestToken, outputFolder, (String.format("%03d",fileIdx++)) + ".mp3", proxyFactory, this);
					creatorThrd.start();
				}else{
					Thread.sleep(1L);
				}
			}
		}while(sentences.size() > 0 || currentThreadCount.get() > 0);

		while( currentThreadCount.get() > 0){
			Thread.sleep(5000L);
		}
	}

	public class AudioSpeecherCreatorThread extends Thread
	{
		private final String sentence;
		private final int voice;
		private final int speed;
		private final String requestToken;
		private File outputFolder;
		private String fileName;
		private ProxyFactory proxyFactory;
		private AudioSpeecherCreator speechCreator; 

		public AudioSpeecherCreatorThread(String sentence, int voice, int speed, String requestToken, File outputFolder, String fileName, ProxyFactory proxyFactory, AudioSpeecherCreator speechCreator) {
			super();
			this.sentence = sentence;
			this.voice = voice;
			this.speed = speed;
			this.outputFolder = outputFolder;
			this.fileName = fileName;
			this.requestToken = requestToken;
			this.proxyFactory = proxyFactory;
			this.speechCreator = speechCreator;
		}

		public void start(){
			this.speechCreator.incThrdCnt();
			log.info("Thread started for " + fileName);
			super.start();
		}

		public void run()
		{
			ProxyConnector proxyConnector = null;
			
			int repeatCount = 0;

			try{
				boolean isErrorExist = false;

				do{
					repeatCount++;
					isErrorExist = false;
					proxyConnector = proxyFactory.getRandomProxyConnector();

					String proxyTypeStr = ConfigManager.getInstance().getProperty("proxy_type");
					Proxy proxy = null;

					try{
						log.trace("Starting checking url");
						proxy = proxyConnector.getConnect(proxyTypeStr);
						generateSpeech(sentence, voice, speed, requestToken, outputFolder, fileName, proxy);
					}
					catch(Throwable e){
						isErrorExist = true;
						log.warn("Error occured during generating speech for sentence: " + sentence, e);
					}
					finally{
						if(proxyConnector != null){
							proxyFactory.releaseProxy(proxyConnector);
							proxyConnector = null;
						}
					}
				}
				while(isErrorExist);
			}finally{
				speechCreator.decThrdCnt();
			}
		}
	}

	private boolean generateSpeech(String sentence, Integer lang, Integer speed, String requestToken, File outputFolder, String fileName, Proxy proxy) throws Exception {
		HttpURLConnection conn = null;
		String requestUrl = "http://api.naturalreaders.com/v2/tts?t=" + URLEncoder.encode(sentence,"UTF8") + "&r="+lang.toString()+"&s="+ speed.toString() +"&requesttoken=" + requestToken;
		try{
			//post news
			URL url = new URL(requestUrl);
			HttpURLConnection.setFollowRedirects(true);
			conn = (HttpURLConnection) url.openConnection(proxy);
			log.trace("Connection created.");
			conn.setReadTimeout(120000);
			conn.setConnectTimeout(120000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestProperty("Host", "api.naturalreaders.com");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
			conn.setRequestProperty("Accept", "audio/webm,audio/ogg,audio/wav,audio/*;q=0.9,application/ogg;q=0.7,video/*;q=0.6,*/*;q=0.5");
			conn.setRequestProperty("Referer","https://www.naturalreaders.com/index.html");

			/*ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
			nameValuePairs.add(new BasicNameValuePair("t", "\"" + sentence + "\""));
			nameValuePairs.add(new BasicNameValuePair("r", lang.toString()));
			nameValuePairs.add(new BasicNameValuePair("s", speed.toString()));
			nameValuePairs.add(new BasicNameValuePair("requesttoken", requestToken));

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();*/

			//int code = conn.getResponseCode();

			//TODO Get file index form params
			saveResponseAsFile(conn, new File(outputFolder, fileName));
			String fileResult = Utils.loadFileAsString( new File(outputFolder, fileName));
			
			if(fileResult.contains("\"rst\":\"ERROR_BUZY\"")){
				throw new Exception("{\"rst\":\"ERROR_BUZY\"} for file:" + fileName);
			}
			
			return true;
			/*//TODO Save output as mp3
			// Execute HTTP Post Request
			Map<String,List<String>> respHeader = conn.getHeaderFields();
			List<String> location = respHeader.get("Location");
			String locationStr = location.toString();


			if(location != null && location.toString().contains("https://e.mail.ru/messages/inbox/?back=1")){
				return true;
			}else{
				return false;
			}*/
		}catch(Exception e){
			throw e;
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}
	}

	private String getRequestToken(Proxy proxy) throws Exception
	{
		String postUrl = "http://api.naturalreaders.com/v2/auth/requesttoken?&appid=pelc790w2bx&appsecret=2ma3jkhafcyscswg8wgk00w0kwsog4s";

		HttpURLConnection conn = null;

		String requetsToken = "";

		try{
			//post news
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(true);
			conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			conn.setRequestProperty("Host", "api.naturalreaders.com");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0"); 
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

			int respCode = conn.getResponseCode();

			StringBuilder responseStr = getResponseAsString(conn, respCode);

			JSONObject jsonObj = new JSONObject(responseStr.toString());

			requetsToken = jsonObj.getString("requesttoken");

			conn.disconnect();
		}
		finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return requetsToken;
	}

	private StringBuffer getResponseAsString(HttpURLConnection conn)
			throws IOException {
		InputStream is = conn.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuffer responseStr = new StringBuffer();
		while ((line = br.readLine()) != null) {
			responseStr.append(line);
		}
		br.close();
		is.close();
		return responseStr;
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

	private StringBuilder getResponseAsString(HttpURLConnection conn, int respCode) throws IOException{
		InputStream is = null;
		if(respCode >= 400){
			is = conn.getErrorStream();
		}else{
			is = conn.getInputStream();
		}

		StringBuilder result = getResponseAsString(is);
		is.close();
		return result;
	}

	private void saveResponseAsFile(HttpURLConnection conn, File outputFile) throws IOException{
		InputStream is = conn.getInputStream();
		// write the inputStream to a FileOutputStream
		OutputStream os = new FileOutputStream(outputFile);

		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = is.read(bytes)) != -1) {
			os.write(bytes, 0, read);
		}
		is.close();
		os.close();
	}

	private StringBuilder getResponseAsString(InputStream is)
			throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder responseStr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			responseStr.append(line).append(LINE_FEED);
		}

		return responseStr;
	}
}
