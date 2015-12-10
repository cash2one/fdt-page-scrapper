package com.fdt.imgur;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.XPatherException;
import org.json.JSONObject;

import com.fdt.imgur.task.IImgurTask;
import com.fdt.imgur.task.ImgurPromoTask;
import com.fdt.imgur.task.ImgurTask;
import com.fdt.imgur.task.ImgurTaskFactory;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;

public class ImgurThread extends Thread{

	private static final Logger log = Logger.getLogger(ImgurThread.class);

	private static final String LINE_FEED = "\r\n";

	private HashMap<String,String> cookie = new HashMap<String, String>();

	private List<ImgurTask> inputTask;
	private List<ImgurTask> successTask = new ArrayList<ImgurTask>();
	private List<ImgurTask> errorTask = new ArrayList<ImgurTask>();
	private List<ImgurTask> unprocessedTask = new ArrayList<ImgurTask>();

	private List<ImgurPromoTask> inputPromoTask;
	private List<ImgurPromoTask> successPromoTask = new ArrayList<ImgurPromoTask>();
	private List<ImgurPromoTask> errorPromoTask = new ArrayList<ImgurPromoTask>();
	private List<ImgurPromoTask> unprocessedPromoTask = new ArrayList<ImgurPromoTask>();

	private ImgurTaskFactory taskFactory;
	private ProxyFactory proxyFactory;
	private String listProcessedFilePath;
	private String errorProcessedFilePath;

	public ImgurThread(List<ImgurTask> inputTask, List<ImgurPromoTask> inputPromoTask, ImgurTaskFactory taskFactory, ProxyFactory proxyFactory, String listProcessedFilePath, String errorProcessedFilePath) 
	{
		this.inputTask = inputTask;
		this.inputPromoTask = inputPromoTask;
		this.taskFactory = taskFactory;
		this.proxyFactory = proxyFactory;
		this.listProcessedFilePath = listProcessedFilePath;
		this.errorProcessedFilePath = errorProcessedFilePath;
	}

	@Override
	public void start(){
		taskFactory.incRunThreadsCount();
		super.start();
	}

	@Override
	public void run() 
	{
		ProxyConnector proxyConnector = proxyFactory.getProxyConnector();

		synchronized (this) {
			try{

				int maxCount = Math.max(inputTask.size(), inputPromoTask.size());
				ImgurTask imgurTask;
				ImgurPromoTask imgurPromoTask;

				for(int i = 0; i < maxCount; i++){

					if(inputTask.size() > i){
						imgurTask = inputTask.get(i);
						if(processTask(imgurTask,proxyConnector)){
							successTask.add(imgurTask);
						}else{
							errorTask.add(imgurTask);
						}
					}

					if(inputPromoTask.size() > i){
						imgurPromoTask = inputPromoTask.get(i);
						if(processPromoTask(imgurPromoTask,proxyConnector)){
							successPromoTask.add(imgurPromoTask);
						}else{
							errorPromoTask.add(imgurPromoTask);
						}
					}
				} 
			}
			catch(Exception e){
				log.error("Error occured during processing: ", e);
			}
			finally	
			{
				for(ImgurTask task : inputTask){
					if( !errorTask.contains(task) && !successTask.contains(task)){
						unprocessedTask.add(task);
					}
				}

				for(ImgurTask task : errorTask){
					//Move file to error folder
					File destFile = new File(errorProcessedFilePath + "/" + task.getFile().getName());
					if(destFile.exists()){
						destFile.delete();

						try {
							FileUtils.moveFile(task.getFile(), destFile);
						} catch (IOException e) {
							log.error(String.format("Error occured during moving file (%s) to error folder", task.getFile().getName()));
						}	
					}
				}

				for(ImgurPromoTask task : inputPromoTask){
					if( !errorPromoTask.contains(task) && !successPromoTask.contains(task)){
						unprocessedPromoTask.add(task);
					}
				}

				if(proxyConnector != null){
					proxyFactory.releaseProxy(proxyConnector);
					proxyConnector = null;
				}

				taskFactory.decRunThreadsCount(unprocessedTask, unprocessedPromoTask);
			}
		}
	}

	//
	private boolean processTask(ImgurTask task, ProxyConnector proxyConnector)
	{
		String imgurUrl = "";

		try {
			task.parseFile();
			Proxy proxy = proxyConnector.getConnect(ProxyFactory.PROXY_TYPE);
			executerequestToGetCookies("http://imgur.com/upload/start_session", "imgur.com", "GET", proxyConnector, null);

			try{
				imgurUrl = getImgurUrl(task, proxy);
			}
			catch(Exception e){
				log.error("Error occured during upload url. Program will try to upload file");
				imgurUrl = "";
			}

			if("".equals(imgurUrl)){
				//start loading file to local folder and upload to imgur
				if(loadImageFromWeb(task)){
					//load file to imgur
					imgurUrl = uploadVideo(task.getImageFile());
				}else{
					throw new Exception(String.format("Image file(%s) was not loaded from web", imgurUrl));
				}
			}

			String fileAsStr = getFileAsString(task.getFile());

			log.debug(String.format("Replace url(%s) with imgurUrl(%s)", task.getImageUrl(), imgurUrl));
			fileAsStr = fileAsStr.replaceAll(task.getImageUrl(), imgurUrl);

			appendStringToFile(fileAsStr, task.getFile(), false);

			//Move file to processed folder
			File destFile = new File(listProcessedFilePath + "/" + task.getFile().getName());
			if(destFile.exists()){
				destFile.delete();
			}

			FileUtils.moveFile(task.getFile(), destFile);

			task.getFile().delete();
			if(task.getImageFile() != null){
				task.getImageFile().delete();
			}
			return true;
		}
		catch (Exception e) {
			log.error("Error occured during process task: " + task.toString(), e);
		}

		return false;
	}

	private boolean processPromoTask(ImgurPromoTask task, ProxyConnector proxyConnector)
	{
		String imgurUrl = "";


		try {
			task.extractImage();
			Proxy proxy = proxyConnector.getConnect(ProxyFactory.PROXY_TYPE);
			executerequestToGetCookies("http://imgur.com/upload/start_session", "imgur.com", "GET", proxyConnector, null);

			try{
				imgurUrl = getImgurUrl(task, proxy);
			}
			catch(Exception e){
				log.error(String.format("Error occured during upload url(%s). Program will try to upload file", task.getImageUrl()));
				imgurUrl = "";
			}

			if("".equals(imgurUrl)){
				//start loading file to local folder and upload to imgur
				if(loadImageFromWeb(task)){
					//load file to imgur
					imgurUrl = uploadVideo(task.getImageFile());
				}else{
					throw new Exception(String.format("Image file(%s) was not loaded from web", imgurUrl));
				}
			}

			log.debug(String.format("Generate for url(%s) new imgur Url(%s)", task.getImageUrl(), imgurUrl));
			appendStringToFile(imgurUrl, new File("promo_success.txt"), true);

			return true;
		}
		catch (Exception e) {
			log.error("Error occured during process promo task: " + task.toString(), e);
		}

		return false;
	}

	private String getFileAsString(File file) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;

		StringBuilder fileAsStr = new StringBuilder();

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
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

	private boolean loadImageFromWeb(IImgurTask task) throws MalformedURLException, IOException{
		File imageFile;
		BufferedImage img = ImageIO.read(new URL(task.getImageUrl()));
		//write image to file
		imageFile = new File("images/"+getRndImageFilename() + "." + task.getImageFormat());
		task.setImageFile(imageFile);
		//this.videoFileWOAudio = new File("output_video/"+getFileNameWOExt(this.inputFile.getName()) + "_wo_audio.mov");
		return ImageIO.write(img, task.getImageFormat(), task.getImageFile());
	}

	private String getImgurUrl(IImgurTask task, Proxy proxy) throws MalformedURLException, IOException, ParseException, XPatherException {
		HttpURLConnection conn = null;
		InputStream is = null;
		log.trace("Using proxy: " + proxy.toString());
		try{
			String strUrl = "http://imgur.com/upload";
			URL url = new URL(strUrl);
			conn = (HttpURLConnection)url.openConnection(proxy);
			conn.setConnectTimeout(30000);
			
			conn.setRequestProperty("Host", strUrl);
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("X-Requested-With","XMLHttpRequest");
			conn.setRequestProperty("Referer", "http://imgur.com/");
			conn.setRequestProperty("Cookie","SESSIONDATA=%7B%22sessionCount%22%3A1%2C%22sessionTime%22%3A" + System.currentTimeMillis() + "%7D; IMGURSESSION=" + getCookie("IMGURSESSION") + "; _nc=1");
			//conn.setRequestProperty("Cookie","SESSIONDATA=%7B%22sessionCount%22%3A1%2C%22sessionTime%22%3A" + System.currentTimeMillis() + "%7D; IMGURUIDJAFO=01bdb1a38191af6a7aad84b28b4b2f900150d20550361da67e83d27bc3920f00; IMGURSESSION=o9e9j35ljq3koojj6qh1427ai3; _nc=1");
			
			conn.setDoInput(true);
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getParamsForImgurLinkUpload(task.getImageUrl(), getCookie("IMGURSESSION")));
			writer.flush();
			writer.close();
			os.close();

			int respCode = conn.getResponseCode();
			
			if(respCode != 200){
				respCode = 0;
			}
			
			String responseStr = getResponseAsString(conn).toString();

			JSONObject jsonObj = new JSONObject(responseStr);
			String uploadUrl = "http://i.imgur.com/" + jsonObj.getJSONObject("data").getString("hash") + "." + task.getImageFormat();

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

	private void appendStringToFile(String str, File file, boolean append) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file,append), "UTF8"));
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

			conn.setRequestProperty("Host", mainUrl);
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");
			conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("X-Requested-With","XMLHttpRequest");
			//conn.setRequestProperty("Referer", "http://" + mainUrl);

			if(requestMethod.equalsIgnoreCase("POST") && postParams != null){
				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(postParams);
				writer.flush();
				writer.close();
				os.close();
			}

			//conn.getInputStream();
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
		params.append("location=").append("inside").append("&");
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

	private String uploadVideo(File uploadFile) throws Exception{

		String postUrl = "http://imgur.com/upload";

		//post news
		String boundary = "----------" + System.currentTimeMillis();

		URL url = new URL(postUrl);
		HttpURLConnection.setFollowRedirects(false);
		//HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(60000);
		conn.setConnectTimeout(300000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestProperty("Host", "imgur.com");
		conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0"); 
		conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
		conn.setRequestProperty("Cookie", getCookieString());
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		conn.setRequestProperty("Pragma","no-cache");

		OutputStream outputStream;
		outputStream = conn.getOutputStream();

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

		writer.append(LINE_FEED).append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"current_upload\"").append(LINE_FEED).append(LINE_FEED);;
		writer.append("1").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"total_uploads\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("1").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"terms\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("0").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"gallery_type\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"gallery_submit\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("0").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"create_album\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("0").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"album_title\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("Optional Album Title").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"layout\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("b").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"sid\"").append(LINE_FEED).append(LINE_FEED);
		writer.append(getCookie("IMGURSESSION")).append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"Filedata\"; filename=\"" + uploadFile.getName() + "\"").append(LINE_FEED);
		writer.append("Content-Type: image/" + FilenameUtils.getBaseName(uploadFile.getName())).append(LINE_FEED).append(LINE_FEED);

		writer.flush();

		FileInputStream inputStream = new FileInputStream(uploadFile);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		inputStream.close();

		writer.append(LINE_FEED);
		writer.append("--" + boundary + "--").append(LINE_FEED);
		/*writer.append("Content-Disposition: form-data; name=\"Upload\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("Submit Query").append(LINE_FEED);
		writer.append("--" + boundary + "--");//.append(LINE_FEED).append(LINE_FEED);
		writer.flush();*/

		writer.flush();  
		writer.close();

		/*FileBody fileBody = new FileBody(uploadFile);
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
		multipartEntity.addPart("file", fileBody);
		multipartEntity.addPart("Filename", new StringBody(uploadFile.getName()));*/
		//multipartEntity.addPart("Filename", "temp_video_audio.mov");

		try {
			//multipartEntity.writeTo(outputStream);
		} finally {
			outputStream.flush();
			outputStream.close();
		}

		StringBuilder responseStr = getResponseAsString(conn);

		//log.debug(responseStr.toString());

		conn.disconnect();

		JSONObject jsonObj = new JSONObject(responseStr.toString());
		log.debug(responseStr.toString());
		String imageUrl = "http://i.imgur.com/" + (String)jsonObj.getJSONObject("data").getString("hash") + "." + FilenameUtils.getExtension(uploadFile.getName());

		//link uploaded video to user

		//Edit video description 
		return imageUrl;
	}
}
