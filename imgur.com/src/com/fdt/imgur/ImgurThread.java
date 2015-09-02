package com.fdt.imgur;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.json.JSONObject;

import com.fdt.imgur.task.ImgurPromoTask;
import com.fdt.imgur.task.ImgurTask;
import com.fdt.imgur.task.ImgurTaskFactory;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;

public class ImgurThread extends Thread{

	private static final Logger log = Logger.getLogger(ImgurThread.class);

	private static final String LINE_FEED = "\r\n";

	private final static String TINYURL_DOWNLOAD_URL_LABEL = "tinyurl_tinyurl_download_url";

	private HashMap<String,String> cookie = new HashMap<String, String>();

	private ImgurTask task;
	private ImgurPromoTask promoTask;
	private ImgurTaskFactory taskFactory;
	private ProxyFactory proxyFactory;
	private String listProcessedFilePath;

	private String firstFileString = "";

	private String downloadUrl = "";

	public ImgurThread(ImgurTask task, ImgurTaskFactory taskFactory, ProxyFactory proxyFactory, String listProcessedFilePath) {
		this.task = task;
		this.taskFactory = taskFactory;
		this.proxyFactory = proxyFactory;
		this.listProcessedFilePath = listProcessedFilePath;
		this.downloadUrl = Constants.getInstance().getProperty(TINYURL_DOWNLOAD_URL_LABEL);
	}

	@Override
	public void start(){
		taskFactory.incRunThreadsCount();
		super.start();
	}

	@Override
	public void run() {
		ProxyConnector proxyConnector = null;
		Proxy proxy;
		
		String imgurUrl = "";
		synchronized (this) {
			try{
				boolean errorExist = false;
				try {
					proxyConnector = proxyFactory.getProxyConnector();
					proxy = proxyConnector.getConnect(ProxyFactory.PROXY_TYPE);
					executerequestToGetCookies("http://imgur.com/upload/start_session", "imgur.com", "GET", proxyConnector, null);
					try{
						imgurUrl = getImgurUrl(downloadUrl, proxy);
					}
					catch(Exception e){
						log.error("Error occured during upload url. Program will try to upload file");
						imgurUrl = "";
					}
					
					if("".equals(imgurUrl)){
						//start loading file to local folder and upload to imgur
						if(loadImageFromWeb(task.getImageUrl(), task.getImageFormat())){
							//TODO load file to imgur
						}else{
							//TODO throw error
						}
					}
					
					String fileAsStr = getFileAsString(task.getFile());

					if( this.firstFileString == null || "".equals(firstFileString.trim()) ){
						throw new Exception("Could not find first file string");
					}

					downloadUrl = this.downloadUrl + firstFileString.replaceAll("[\\.\\]\\[\\)\\(,+!#]*", "").trim().replaceAll("\\s", "+");

					//TODO Get tinyurl


					fileAsStr = fileAsStr.replaceAll("\\[KEYWORD\\]", imgurUrl);

					appendStringToFile(fileAsStr, task.getFile());

					//Move file to processed folder
					File destFile = new File(listProcessedFilePath + "/" + task.getFile().getName());
					if(destFile.exists()){
						destFile.delete();
					}
					FileUtils.moveFile(task.getFile(), destFile);
					/*if(task.isResultEmpty()){
						proxyConnector = proxyFactory.getProxyConnector();
						log.debug("Free proxy count: " + (proxyFactory.getFreeProxyCount()-1));
						log.debug("Task (" + task.toString() +") is using proxy connection: " +proxyConnector.getProxyKey());
						Proxy proxy = proxyConnector.getConnect();
						NewsPoster ps;
						ps = new NewsPoster(task, proxy, account, taskFactory, linkList);
						String newsResult = ps.executePostNews();
						task.setResult(newsResult);
					}*/

				}
				catch (Exception e) {
					errorExist = true;
					taskFactory.reprocessingTask(task);
					log.error("Error occured during process task: " + task.toString(), e);
				}finally{
					if(proxyConnector != null){
						proxyFactory.releaseProxy(proxyConnector);
						proxyConnector = null;
					}
				}

				if(!errorExist){
					taskFactory.putTaskInSuccessQueue(task);
				}

			} finally {
				taskFactory.decRunThreadsCount(task);
			}
		}
	}

	private String getFileAsString(File file) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;
		boolean isFirstLineSaved = false;

		this.firstFileString = "";

		StringBuilder fileAsStr = new StringBuilder();

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
				if(!isFirstLineSaved){
					firstFileString = line;
					isFirstLineSaved = true;
				}
				fileAsStr.append(line).append(LINE_FEED);
			}
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

		return fileAsStr.toString();
	}

	public ImgurTask getTask(){
		return task;
	}

	private boolean loadImageFromWeb(String imageUrl, String imageFormat) throws MalformedURLException, IOException{
		File imageFile;
		BufferedImage img = ImageIO.read(new URL(imageUrl));
		//write image to file
		imageFile = new File("images/"+getRndImageFilename() + "." + imageFormat);
		//this.videoFileWOAudio = new File("output_video/"+getFileNameWOExt(this.inputFile.getName()) + "_wo_audio.mov");
		return ImageIO.write(img, imageFormat, imageFile);
	}

	private String getImgurUrl(String downloadUrl, Proxy proxy) throws MalformedURLException, IOException, ParseException, XPatherException {
		HttpURLConnection conn = null;
		InputStream is = null;
		System.out.println("Using proxy: " + proxy.toString());
		try{
			String strUrl = "http://imgur.com/upload";
			URL url = new URL(strUrl);
			System.out.println(strUrl);
			//using proxy
			conn = (HttpURLConnection)url.openConnection(proxy);
			conn.setConnectTimeout(30000);
			conn.setDoInput(true);
			conn.setDoOutput(false);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getParamsForImgurLinkUpload(task.getImageUrl(), getCookie("IMGURSESSION")));
			writer.flush();
			writer.close();
			os.close();

			String responseStr = getResponseAsString(conn).toString();
			
			JSONObject jsonObj = new JSONObject(responseStr.substring(1, responseStr.length()-1));
			String uploadUrl = jsonObj.getJSONObject("data").getString("upload_url");
			
			return uploadUrl;
		}finally{
			if(conn != null){
				try{conn.disconnect();}catch(Throwable e){}
			}
			if(is != null){
				try{is.close();}catch(Throwable e){}
			}
		}
	}

	private void appendStringToFile(String str, File file) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();
		} catch (FileNotFoundException ex) {
			log.error("Error during saving string to file",ex);
		} catch (IOException ex) {
			log.error("Error during saving string to file",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error during closing output stream",ex);
			}
		}
	}

	private String getRndImageFilename(){
		Random random = new Random();
		int length = 4 + random.nextInt(3);
		String characters = "abcdefghijklmnopqrstuvwxyz";
		char[] text = new char[length];
		for (int i = 0; i < length; i++)
		{
			text[i] = characters.charAt(random.nextInt(characters.length()));
		}
		return new String(text) + String.valueOf(System.currentTimeMillis());
	}

	private void executerequestToGetCookies(String postUrl, String mainUrl, String requestMethod, ProxyConnector proxy, String postParams) throws IOException, XPathExpressionException{

		HttpURLConnection conn = null;

		try{
			//post news
			URL url = new URL(postUrl);
			log.info("URL: " + url);
			HttpURLConnection.setFollowRedirects(false);
			conn = (HttpURLConnection) url.openConnection(proxy.getConnect(ProxyFactory.PROXY_TYPE));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod(requestMethod);
			if(requestMethod.equalsIgnoreCase("POST")){
				conn.setDoOutput(true);
			}else{
				conn.setDoOutput(false);
			}
			conn.setDoInput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("X-Requested-With","XMLHttpRequest");
			//conn.setRequestProperty("Cookie", account.getCookies());
			conn.setRequestProperty("Host", mainUrl);
			conn.setRequestProperty("Referer", "http://" + mainUrl);

			if(requestMethod.equalsIgnoreCase("POST") && postParams != null){
				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(postParams);
				writer.flush();
				writer.close();
				os.close();
			}

			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();

			int code = conn.getResponseCode();

			if(cookies.get("Set-Cookie") != null){
				for(String cookieOne: cookies.get("Set-Cookie"))
				{
					String cookiesValues[] = cookieOne.split(";");
					for(String cookiesArrayItem : cookiesValues){
						String singleCookei[] = cookiesArrayItem.split("=");
						addCookie(singleCookei[0].trim(), singleCookei[1].trim());
					}
				}
			}
		}
		finally{
			if(conn != null)
				conn.disconnect();
		}
	}

	private String getCookieString(){
		StringBuilder strBuilder = new StringBuilder();

		for(String key : cookie.keySet()){
			strBuilder.append(key).append("=").append(cookie.get(key)).append("; ");
		}

		return strBuilder.toString();
	}

	public void addCookie(String key, String value)
	{
		this.cookie.put(key, value);
	}

	public String getCookie(String key){
		return cookie.get(key);
	}

	private String getParamsForImgurLinkUpload(String imageUrl, String sid){
		StringBuilder params = new StringBuilder();
		params.append("forceAnonymous=false&");
		params.append("current_upload=1&");
		params.append("total_uploads=1&");
		params.append("gallery_submit=false&");
		params.append("gallery_type=&");
		params.append("sid=").append(sid).append("&");
		params.append("new_album_id=&");
		params.append("catify=0&");
		params.append("url=").append(imageUrl).append("&");
		params.append("edit_url=0");

		return params.toString();
	}
	
	private StringBuilder getResponseAsString(HttpURLConnection conn)
			throws IOException {
		InputStream is = conn.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder responseStr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			responseStr.append(line).append(LINE_FEED);
		}
		is.close();
		return responseStr;
	}
}
