package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.fdt.dailymotion.task.NewsTask;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

/**
 *
 * @author Administrator
 */
public class NewsPoster {
	private static final Logger log = Logger.getLogger(NewsPoster.class);

	public final static String UPLOAD_CONTEXT_URL_LABEL = "upload_context_url";

	private static final String LINE_FEED = "\r\n";

	private static final Random rnd = new Random();;
	
	private String[] themes = new String[]{"auto","webcam","animals","creation","lifestyle","people","music","news","school","travel","sport","tech","shortfilms","fun"};
	//private String[] themes = new String[]{"shortfilms"};

	private NewsTask task = null;
	private Proxy proxy = null;
	private Account account = null;

	private Boolean loadPreGenFile = false;

	private Integer times[];

	private static final String TIME_STAMP_FORMAT = "HH:mm:ss.SSS";
	private static final String VIDEO_RECORD_FORMAT = "yyyy/MM/dd";

	private SimpleDateFormat sdf = new SimpleDateFormat(TIME_STAMP_FORMAT);
	private SimpleDateFormat vrdf = new SimpleDateFormat(VIDEO_RECORD_FORMAT);

	public NewsPoster(NewsTask task, Proxy proxy, Account account, Boolean loadPreGenFile) {
		this.task = task;
		this.proxy = proxy;
		this.account = account;
		this.loadPreGenFile = loadPreGenFile;
	}

	public String executePostNews(Integer[] times) throws Exception {
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.times = times;
		return postNews();
	}

	private String getTimeString(){
		double milSecCnt = 0L;
		milSecCnt = (((double)times[0]/times[1])) * 1000;

		String valueStr = String.format("%.0f", milSecCnt);
		log.info("Preview time: " + sdf.format(new Date(Long.parseLong(valueStr)-0)));

		return sdf.format(new Date(Long.parseLong(valueStr)-0));
	}

	private String getUploadUrl() throws Exception{
		String postUrl = "https://api.dailymotion.com/?access_token=" + account.getCookie("sid");
		HttpsURLConnection conn = null;
		String uploadUrl = ""; 
		try{
			//post news
			URL url = new URL(postUrl);
			HttpsURLConnection.setFollowRedirects(false);
			conn = (HttpsURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Host", "api.dailymotion.com");
			conn.setRequestProperty("Referer", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write("[{\"call\":\"GET /file/upload\",\"args\":{},\"id\":0}]");
			writer.flush();
			writer.close();
			os.close();

			if(conn.getResponseCode() == 401){
				account.getAccountFactory().rejectAccount(account);
				throw new Exception(String.format("Account %s lost the login data. Account will rejected",account.getLogin()));
			}

			StringBuilder responseStr = getResponseAsString(conn);

			//log.debug(responseStr.toString());

			conn.disconnect();

			JSONObject jsonObj = new JSONObject(responseStr.substring(1, responseStr.length()-1));
			uploadUrl = jsonObj.getJSONObject("result").getString("upload_url");

			log.info("Upload URL: " + uploadUrl);
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return uploadUrl;
	}

	private String getVideoId() throws Exception{
		String postUrl = "https://api.dailymotion.com/?access_token=" + account.getCookie("sid");
		HttpsURLConnection conn = null;
		String videoId = null;

		try{
			//post news
			URL url = new URL(postUrl);
			HttpsURLConnection.setFollowRedirects(false);
			conn = (HttpsURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Host", "api.dailymotion.com");
			conn.setRequestProperty("Referer", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write("[{\"call\":\"POST /videos\",\"args\":{\"title\":\"" + task.getVideoTitle() +"\",\"published\":\"false\"},\"id\":0}]");
			writer.flush();
			writer.close();
			os.close();

			StringBuilder responseStr = getResponseAsString(conn);

			//log.debug(responseStr.toString());

			conn.disconnect();

			JSONObject jsonObj = new JSONObject(responseStr.substring(1, responseStr.length()-1));
			try{
				videoId = jsonObj.getJSONObject("result").getString("id");
			}catch(NoSuchElementException e){
				//Add account to reject list
				account.getAccountFactory().rejectAccount(account);
				log.error("'result' element NOT FOUND. Responce JSONObject: " + responseStr);
				throw e;
			}
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		log.info("Video ID: " + videoId);

		return videoId;
	}

	private void postVideo(String videoUrl, String videoId) throws Exception{
		String postUrl = "https://api.dailymotion.com/?access_token=" + account.getCookie("sid");
		HttpsURLConnection conn = null;
		try{
			//post news
			URL url = new URL(postUrl);
			HttpsURLConnection.setFollowRedirects(false);
			conn = (HttpsURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Host", "api.dailymotion.com");
			conn.setRequestProperty("Referer", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write("[{\"call\":\"POST /video/"+videoId+"\",\"args\":{\"url\":\"" + videoUrl +"\",\"fields\":\"id,\"},\"id\":0}]");
			writer.flush();
			writer.close();
			os.close();

			StringBuilder responseStr = getResponseAsString(conn);

			JSONObject jsonObj = new JSONObject(responseStr.substring(1, responseStr.length()-1));
			JSONObject errorEntity = null;

			try {
				errorEntity = jsonObj.getJSONObject("error");
			} catch (NoSuchElementException e) {
				//nothing do
			}

			if(errorEntity != null){
				throw new Exception("Error occured during posting video " + videoId + ": " + errorEntity.getString("message"));
			}

			log.debug("Responce string for POST requets for access_token: " + responseStr.toString());

		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}
	}

	private String executeAccessToken(String videoId, String requestStr) throws Exception{
		String postUrl = "https://api.dailymotion.com/?access_token=" + account.getCookie("sid");

		HttpsURLConnection conn = null;
		StringBuilder responseStr = new StringBuilder();
		try{
			//post news
			URL url = new URL(postUrl);
			HttpsURLConnection.setFollowRedirects(false);
			conn = (HttpsURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Host", "api.dailymotion.com");
			conn.setRequestProperty("Referer", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(requestStr);
			writer.flush();
			writer.close();
			os.close();

			responseStr = getResponseAsString(conn);

		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return responseStr.toString();
	}

	private void getVideoAccessToken(String videoId) throws Exception{
		String respStr = "";
		respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"encoding_progress,\"},\"id\":0}]");
		respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"thumbnail_url,\"},\"id\":0},{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"status,\"},\"id\":1}]");
		respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/" + videoId + "\",\"args\":{\"fields\":\"status,\"},\"id\":0}]");

		String[] status = new String[]{"processing","0"};
		int progress = 0;

		while(status[0].equalsIgnoreCase("processing")){
			//check for rejection
			if(!account.getAccountFactory().isAccountRejected(account) && progress <= 100){
				log.info("Responce download string for video ID:" + videoId + " : " + respStr);
				Thread.sleep(5000L);
				respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/" + videoId + "\",\"args\":{\"fields\":\"status,encoding_progress\"},\"id\":0}]");
				status = getUploadStatus(respStr);
				progress = Integer.valueOf(status[1]);
			}else{
				log.error("Responce string for exceeded account:" + respStr);
				throw new Exception("Account execeed upload limit: " + account.getLogin());
			}
		}

		log.info(String.format("Response download status: %s; Progress: %s", status[0],status[1]));
	}

	private String[] getUploadStatus(String respStr) throws ParseException{

		JSONObject jsonObj = new JSONObject(respStr.substring(1, respStr.length()-1));
		String status[] = new String[]{"",""};
		try{
			status[0] = jsonObj.getJSONObject("result").getString("status");
			status[1] = jsonObj.getJSONObject("result").getString("encoding_progress");

		}catch(NoSuchElementException e){
			//Add account to reject list
			account.getAccountFactory().rejectAccount(account);
			log.error("JSON element NOT FOUND. Responce JSONObject: " + respStr);
			throw e;
		}

		return status;
	}

	private int executeOptionRequest(String postUrl) throws Exception{

		HttpURLConnection conn = null;
		int respCode = -1;
		try{
			//post news
			URL url = new URL(postUrl);
			String host = postUrl.substring(postUrl.indexOf("http://") + 7, postUrl.indexOf(".com") + 4);
			HttpURLConnection.setFollowRedirects(false);
			conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("OPTIONS");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			conn.setRequestProperty("Host", host);
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Origin", "http://www.dailymotion.com");
			conn.setRequestProperty("Access-Control-Request-Method", "POST");
			conn.setRequestProperty("Access-Control-Request-Headers", "content-disposition,content-range,content-type,session-id");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Pragma", "no-cache");
			conn.setRequestProperty("Cache-Control" ,"no-cache");

			respCode = conn.getResponseCode();

			log.debug("OPTION request responce code: " + conn.getResponseCode());

		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return respCode;
	}

	private String postNews() throws Exception{
		//String lri = 	
		String uploadUrl = getUploadUrl();
		if(uploadUrl == null || "".equals(uploadUrl.trim())){
			throw new Exception("Upload url was not extracted.");
		}

		String videoId = getVideoId();
		if(videoId == null || "".equals(videoId.trim())){
			throw new Exception("VideoId was not extracted.");
		}

		String videoUrl = uploadVideo(uploadUrl, videoId);
		return videoUrl.substring(0, videoUrl.indexOf(videoId) + videoId.length());
	}

	private String uploadVideo(String uploadUrl, String videoId) throws Exception{
		String videoPostedUrl = "not_posted";

		executeRequestToGetCookies("http://www.dailymotion.com/upload", "GET", null);
		executeRequestToGetCookies("http://www.dailymotion.com/upload_new/ping?t=" + (System.currentTimeMillis()), "HEAD", null);
		String oldCookie = account.getCookie("_csrf/link");

		editVideoDescription(videoId, oldCookie, "GET");

		//executeRequestToGetCookies("http://www.dailymotion.com/pageitem/video/edit?request=/&t=0.6538078272511391&loop=0&from_request=/upload&_csrf_l=" + account.getCookie("_csrf/link"), "GET", null);
		//String videoId = executerequestToGetCookies("http://www.dailymotion.com/ajax/video", "POST", new VideoIdExtractor(), getAjaxVideoParamString());
		task.setVideoId(videoId);
		String videoUrl = "";

		String postUrl = uploadUrl.replace("upload?uuid", "rupload?uuid");

		//getSessionId
		String sessionId = uploadUrl.substring(uploadUrl.indexOf("uuid=")+5, uploadUrl.indexOf("&seal="));

		String fileName = task.getVideoFile().getName();
		File uploadFile = task.getVideoFile();

		FileInputStream inputStream = new FileInputStream(uploadFile);
		HttpURLConnection conn = null;
		//executeOptionRequest(postUrl);

		try{
			for (long i= 0; i <= uploadFile.length(); i += 500000) {
				log.debug(String.format("%s: Upload new segment [%d:%d]", Thread.currentThread().getId(), i-500000, i));
				executeOptionRequest(postUrl);
				URL url = new URL(postUrl);
				HttpURLConnection.setFollowRedirects(false);
				conn = (HttpURLConnection) url.openConnection(proxy);
				conn.setReadTimeout(60000);
				conn.setConnectTimeout(300000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				conn.addRequestProperty("Host", uploadUrl); 
				conn.setRequestProperty("Accept", "text/plain, */*; q=0.01");
				conn.addRequestProperty("Content-Type", "video/mp4");
				conn.addRequestProperty("Session-Id", sessionId); 
				conn.addRequestProperty("Content-Disposition","attachment; filename=\"" + fileName + "\""); 
				conn.addRequestProperty("Referer", "http://www.dailymotion.com/upload"); 

				conn.setRequestProperty("Cookie", "sdx=" + account.getCookie("sdx") + "; " + "ts=" + account.getCookie("ts") + "; ");

				OutputStream outputStream = null;

				byte[] buffer = new byte[500000];
				int bytesRead = -1;
				if ((bytesRead = inputStream.read(buffer)) != -1) {
					conn.addRequestProperty("Content-Range", "bytes " + (i) + "-" + (i+bytesRead-1)+ "/" + uploadFile.length());
					outputStream = conn.getOutputStream();
					outputStream.write(buffer, 0, bytesRead);
				}
				if(outputStream != null){
					outputStream.flush();
					outputStream.close();
				}

				int respCode = conn.getResponseCode();

				StringBuilder responseStr = getResponseAsString(conn);
				if(respCode == 200){
					log.info(responseStr.toString());
					JSONObject jsonObj = new JSONObject(responseStr.toString());
					videoPostedUrl = (String)jsonObj.getString("url");
					postVideo(videoPostedUrl, videoId);
				}
			}
		}finally{
			if(conn != null){
				conn.disconnect();
			}

			if(inputStream != null){
				try{
					inputStream.close();
				}catch(Exception e){}
			}
		}

		executeRequestToGetCookies("http://www.dailymotion.com/upload_new/ping?t=" + (System.currentTimeMillis()), "HEAD", null);

		//edit video description
		return editVideoDescription(videoId, oldCookie, "POST");
	}

	private String editVideoDescription(String videoId, String oldCookie, String method) throws Exception{
		String postUrl = ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
				"/pageitem/uploadNewForm?request=/new&t=0.5693014024517274&xid="+videoId+"&from_request=/upload&_csrf_l=" + oldCookie;

		HttpURLConnection conn = null;
		try{
			//post news
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(false);
			conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod(method);
			conn.setDoInput(true);
			if("POST".equals(method)){
				conn.setDoOutput(true);
				getVideoAccessToken(videoId);
			}else{
				conn.setDoOutput(false);
			}

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Cookie", account.getCookies());
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Host", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
			conn.setRequestProperty("Referer", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			if("POST".equals(method)){
				OutputStream outputStream;
				outputStream = conn.getOutputStream();

				PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

				writer.append(getEditVideoPostParamsUrl(videoId));

				writer.flush();
				writer.close();
				outputStream.close();
			}else{
				int respCode = conn.getResponseCode();
				Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();

				if(cookies.get("Set-Cookie") != null){
					for(String cookieOne: cookies.get("Set-Cookie"))
					{
						String cookiesValues[] = cookieOne.split(";");
						for(String cookiesArrayItem : cookiesValues){
							String singleCookei[] = cookiesArrayItem.split("=");
							account.addCookie(singleCookei[0].trim(), singleCookei[1].trim());
						}
					}
				}
			}

			int respCode = conn.getResponseCode();

			// Execute HTTP Post Request
			StringBuilder responseStr = getResponseAsString(conn);

			log.trace("Responce string after EDIT operation: " + responseStr);
		}
		finally{
			if(conn != null){
				conn.disconnect();
			}
		}


		Thread.sleep(15000L);
		if("POST".equals(method)){
			String subUrl = executerequestToGetCookies("http://www.dailymotion.com/ajax/video", "POST", new VideoUrlExtractor(), getAjaxVideoParamStringForUrl());
			String videoUrl = "http://www.dailymotion.com" + subUrl;
			if( !videoUrl.contains("/video/") || videoUrl.contains("_%D0%B1%D0%B5%D0%B7-%D0%BD%D0%B0%D0%B7%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F")){
				throw new Exception("URL to video was not extracted. Next string was extracted: " + videoUrl);
			}
			//TODO Fix issue with preview 
			//String respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"thumbnail_url,\"},\"id\":0},{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"status,\"},\"id\":1}]");
			//JSONObject jsonObj = new JSONObject(respStr.substring(1, respStr.length()-1));
			//String thumbnailUrlOld = jsonObj.getJSONObject("result").getString("thumbnail_url");
			//String thumbnailUrlNew = thumbnailUrlOld;
			//int oldFileSizeLen = getFileSize(thumbnailUrlOld);
			//int newFileSizeLen = getFileSize(thumbnailUrlOld);
			//log.info(String.format("Old preview of video (%s): %s",videoId, thumbnailUrlOld));

			if(!loadPreGenFile){
				/*while(thumbnailUrlOld.equals(thumbnailUrlNew) || oldFileSizeLen == newFileSizeLen ){
					setPreview(videoId, subUrl);
					respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"thumbnail_url,\"},\"id\":0},{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"status,\"},\"id\":1}]");
					jsonObj = new JSONObject(respStr.substring(1, respStr.length()-1));
					thumbnailUrlNew = jsonObj.getJSONObject("result").getString("thumbnail_url");
					newFileSizeLen = getFileSize(thumbnailUrlNew);
					log.info(String.format("New preview of video (%s): %s",videoId, thumbnailUrlNew));
					Thread.sleep(10000L);
				}*/
				if(task.isUsePreview()){
					try{
						setPreview(videoId, subUrl, task.getPreviewImageFile());
					}catch(Exception e){
						log.warn("Error occured during posting PREVIEW for video: " + videoId, e);
					}
				}
				/*respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"thumbnail_url,\"},\"id\":0},{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"status,\"},\"id\":1}]");
				jsonObj = new JSONObject(respStr.substring(1, respStr.length()-1));
				thumbnailUrlNew = jsonObj.getJSONObject("result").getString("thumbnail_url");
				newFileSizeLen = getFileSize(thumbnailUrlNew);
				log.info(String.format("New preview of video (%s): %s",videoId, thumbnailUrlNew));*/
			}else{
				try{
					setPreview(videoId, subUrl, new File("6f0ad89ddb33.jpg"));
				}catch(Exception e){
					log.warn("Error occured during posting PREVIEW for video: " + videoId, e);
				}
			}
			log.info("VIDEO URL: " + videoUrl);
			return videoUrl;
		}else{
			return "";
		}
	}

	private int getFileSize(String fileUrl) throws MalformedURLException {
		HttpURLConnection conn = null;
		URL url = new URL(fileUrl);
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			return -1;
		} finally {
			conn.disconnect();
		}
	}

	//	private String setPreview(String videoId, String fromRequest) throws Exception{
	//		String postUrl = ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
	//				"/ajax/video_preview_v3";
	//
	//		HttpURLConnection conn = null;
	//		StringBuilder responseStr = new StringBuilder();
	//		//post news
	//		try{
	//			URL url = new URL(postUrl);
	//			HttpURLConnection.setFollowRedirects(false);
	//			conn = (HttpURLConnection) url.openConnection(proxy);
	//			conn.setReadTimeout(60000);
	//			conn.setConnectTimeout(60000);
	//			conn.setRequestMethod("POST");
	//			conn.setDoInput(true);
	//			conn.setDoOutput(true);
	//
	//			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
	//			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	//			conn.setRequestProperty("Accept", "*");
	//			conn.setRequestProperty("Cookie", account.getCookies());
	//			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
	//			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
	//			conn.setRequestProperty("Host", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
	//			//conn.setRequestProperty("Referer", http://www.dailymotion.com/pageitem/OneStepPreview?widget_only=1&hidenextvideo=1&request=/video/x30lasl_%25D0%25BF%25D1%2580%25D0%25B5%25D1%2581%25D0%25BB%25D0%25B5%25D0%25B4%25D0%25BE%25D0%25B2%25D0%25B0%25D0%25BD%25D0%25B8%25D0%25B5-m%25D0%25BE%25D1%2582%25D0%25BE%25D1%2586%25D0%25B8%25D0%25BA%25D0%25BB%25D0%25B8%25D1%2581%25D1%2582%25D0%25B0-30-06-2015_webcam);
	//
	//			OutputStream outputStream;
	//			outputStream = conn.getOutputStream();
	//			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);
	//			writer.append(getPreviewParamString(videoId, fromRequest));
	//			writer.flush();
	//			writer.close();
	//			outputStream.close();
	//
	//			int respCode = conn.getResponseCode();
	//			// Execute HTTP Post Request
	//			responseStr = getResponseAsString(conn);
	//
	//			log.info("Set preview responce string: " + responseStr.toString());
	//		}finally{
	//			if(conn != null){
	//				conn.disconnect();
	//			}
	//		}
	//
	//		return responseStr.toString();
	//	}

	private String setPreview(String videoId, String fromRequest, File previewFile) throws Exception{

		String postUrl = ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
				"/pageitem/EditUploadPreview?widget_only=1&hidenextvideo=1&request="+fromRequest;

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

		conn.setRequestProperty("Host", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
		conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0"); 
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Cookie", account.getCookies());
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		OutputStream outputStream;
		outputStream = conn.getOutputStream();

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

		writer.append(LINE_FEED).append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"form_name\"").append(LINE_FEED).append(LINE_FEED);;
		writer.append("dm_pageitem_video_uploadpreview").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"_csrf\"").append(LINE_FEED).append(LINE_FEED);
		writer.append(account.getCookie("_csrf/form")).append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"_fid\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"MAX_FILE_SIZE\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("6291456").append(LINE_FEED);

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"video_preview\"; filename=\"" + previewFile.getName() + "\"").append(LINE_FEED);
		writer.append("Content-Type: image/jpeg").append(LINE_FEED).append(LINE_FEED);;

		writer.flush();

		FileInputStream inputStream = new FileInputStream(previewFile);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		inputStream.close();

		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"redirect_url\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("Сохранить").append(LINE_FEED);

		writer.append("--" + boundary + "--").append(LINE_FEED).append(LINE_FEED);;
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

		int respCode = conn.getResponseCode();
		StringBuilder responseStr = getResponseAsString(conn);

		//log.debug(responseStr.toString());

		conn.disconnect();

		//link uploaded video to user

		//Edit video description 
		return responseStr.toString();
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

	private String executeRequestToGetCookies(String postUrl, String requestMethod, IResultExtractor resultExtractor) throws IOException{
		return executerequestToGetCookies( postUrl, requestMethod, resultExtractor, null);
	}

	private String executerequestToGetCookies(String postUrl, String requestMethod, IResultExtractor resultExtractor, String postParams) throws IOException{
		HttpURLConnection conn = null;
		//post news
		try{
			URL url = new URL(postUrl);
			log.info("URL: " + url);
			HttpURLConnection.setFollowRedirects(false);
			//TODO Uncomment
			//HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
			conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod(requestMethod);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Cookie", account.getCookies());
			//conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestProperty("Host", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
			conn.setRequestProperty("Referer", ConfigManager.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			if(requestMethod.equalsIgnoreCase("POST") && postParams != null){
				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(postParams);
				writer.flush();
				writer.close();
				os.close();
			}

			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();

			if(cookies.get("Set-Cookie") != null){
				for(String cookieOne: cookies.get("Set-Cookie"))
				{
					String cookiesValues[] = cookieOne.split(";");
					for(String cookiesArrayItem : cookiesValues){
						String singleCookei[] = cookiesArrayItem.split("=");
						account.addCookie(singleCookei[0].trim(), singleCookei[1].trim());
					}
				}
			}

			StringBuilder responseStr = getResponseAsString(conn);

			//log.debug(responseStr.toString());

			if(resultExtractor != null){
				resultExtractor.init(responseStr.toString());
			}
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return resultExtractor != null ? resultExtractor.getResult():"";
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

	private String getAjaxVideoParamString(){
		StringBuilder params = new StringBuilder();
		params.append("ajax_function").append("=").append("create").append("&");
		params.append("ajax_arg[]").append("=").append("http://www.dailymotion.com/upload").append("&");
		params.append("ajax_arg[]").append("=").append("").append("&");
		params.append("_").append("=").append(String.valueOf(System.currentTimeMillis())).append("&");
		params.append("from_request").append("=").append("/upload").append("&");
		/*params.append("_csrf_l").append("=").append(account.getCookie("_csrf/link")).append("&");
		params.append("_csrf_l").append("=").append(account.getCookie("_csrf/link"));*/
		params.append("_csrf/link").append("=").append(account.getCookie("_csrf/link")).append("&");
		params.append("_csrf/form").append("=").append(account.getCookie("_csrf/form"));

		return params.toString();
	}

	private String getAjaxVideoParamStringForUrl(){
		StringBuilder params = new StringBuilder();
		params.append("ajax_function").append("=").append("get_url").append("&");
		params.append("ajax_arg[]").append("=").append(task.getVideoid()).append("&");
		params.append("_").append("=").append(String.valueOf(System.currentTimeMillis())).append("&");
		params.append("from_request").append("=").append("/upload").append("&");
		params.append("_csrf_l").append("=").append(account.getCookie("_csrf/link"));

		return params.toString();
	}

	private String getPreviewParamString(String videoId, String fromRequest){
		StringBuilder params = new StringBuilder();
		params.append("ajax_function").append("=").append("extract_preview").append("&");
		params.append("ajax_arg[]").append("=").append(videoId).append("&");
		params.append("ajax_arg[]").append("=").append(getTimeString()).append("&");
		params.append("_").append("=").append(String.valueOf(System.currentTimeMillis())).append("&");
		params.append("from_request").append("=").append("/pageitem/OneStepPreview?widget_only=1&hidenextvideo=1&request=" + fromRequest).append("&");
		params.append("_csrf_l").append("=").append(account.getCookie("_csrf/link"));

		return params.toString();
	}

	private class VideoIdExtractor implements IResultExtractor{

		private String responseStr; 

		public void init(String responseStr){
			this.responseStr = responseStr;
		}

		@Override
		public String getResult() {
			String id = "-1";
			log.debug("Response string: " + responseStr);
			id = responseStr.substring(14, responseStr.lastIndexOf("}"));

			return id;
		}

	}

	private class VideoUrlExtractor implements IResultExtractor{

		private String responseStr; 

		public void init(String responseStr){
			this.responseStr = responseStr;
		}

		@Override
		public String getResult() {
			String url = "-1";
			url = responseStr.substring(2).replaceAll("\\r\\n", "");;

			return url;
		}

	}

	private String getVideoFormXUploadUrl(String videoId, String videoUrl) {
		StringBuilder params = new StringBuilder();
		params.append("ajax/getVideoFromXUpload?xupload_response=1&video_id=")
		.append(videoId)
		.append("&formUpload=upload_0&xform=")
		.append(videoUrl.replaceAll(":", "%3A").replaceAll("#", "%23").replaceAll("=", "%3D"))
		.append("&from_request=%2Fupload&_csrf_l=")
		.append(account.getCookie("_csrf/link"));

		return "http://www.dailymotion.com/" + params.toString();
	}

	/*private String getEditVideoPostParamsUrl(String videoId) throws Exception {
		StringBuilder params = new StringBuilder();

		String description = task.getDescription();
		if(description.length() > 3000){
			description = URLEncoder.encode(description.substring(0,2900),"UTF-8");
		}else{
			description = URLEncoder.encode(description,"UTF-8");
		}

		String title = task.getVideoTitle() + " " + getRndStr();
		title = Utils.getFirstSmblUpper(title);
		if(title.length() > 255){
			title = URLEncoder.encode(title.substring(0,250),"UTF-8");
		}else{
			title = URLEncoder.encode(title,"UTF-8");
		}
		
		params.append("")
		.append("form_name=").append("dm_pageitem_uploadnewform_").append(videoId).append("&")
		.append("_csrf=").append(account.getCookie("_csrf/form")).append("&")
		.append("_fid=").append("").append("&")
		.append("video_title=").append(URLEncoder.encode(title,"UTF-8")).append("&")
		.append("user_category=").append(themes[rnd.nextInt(themes.length)]).append("&")
		.append("game_select=").append("").append("&")
		.append("game_select=").append("").append("&")
		.append("artist=").append("").append("&")
		.append("title=").append("").append("&")
		.append("album=").append("").append("&")
		.append("upc=").append("").append("&")
		.append("isrc=").append("").append("&")
		.append("iswc=").append("").append("&")
		.append("label=").append("").append("&")
		.append("artist_id=").append("").append("&")
		.append("track_id=").append("").append("&")
		.append("album_id=").append("").append("&")
		.append("itunes_id=").append("").append("&")
		.append("genre=").append("").append("&")
		.append("language=").append("en").append("&")
		.append("tags_hidden=").append(URLEncoder.encode(task.getVideoTitle(),"UTF-8")).append("&")
		.append("strongtags_hidden=").append("{\"strong_tags\":{},\"daily_tags\":[").append(URLEncoder.encode(task.getTags(10, 250),"UTF-8")).append("]}").append("&")
		.append("tags=").append("").append("&")
		.append("description=").append( URLEncoder.encode(description,"UTF-8")).append("&")
		.append("privacy=").append("0").append("&")
		.append("allow_comments=").append("1").append("&")
		//.append("allow_in_group=").append("1").append("&")
		.append("recordedOn=").append(vrdf.format(new Date(System.currentTimeMillis()-60*60*24*1000))).append("&")
		//.append("coming_next=").append("").append("&")
		//.append("videoId=").append(task.getVideoid()).append("&")
		//.append("videoUpdateTitle=").append(task.getVideoid()).append("&")
		//.append("extensionVideo=").append("").append("&")
		//.append("uploadType=").append("").append("&")
		//.append("saveStatus=").append("fail").append("&")
		//.append("from_request=").append("/video/edit/x2a5w2y_%25D0%25B1%25D0%25B5%25D0%25B7-%25D0%25BD%25D0%25B0%25D0%25B7%25D0%25B2%25D0%25B0%25D0%25BD%25D0%25B8%25D1%258F")
		.append("tvod_price=").append("0.49").append("&")
		.append("window_duration=").append("48").append("&")
		.append("paywall_start=").append("10").append("&")
		.append("from_publish=").append("1").append("&")
		.append("from_request=").append("/upload").append("&")
		.append("_csrf_l=").append(account.getCookie("_csrf/link"));

		log.trace("Params for edit video: " + params.toString());

		return params.toString();
	}*/
	
	//for casino
	private String getEditVideoPostParamsUrl(String videoId) throws Exception {
		StringBuilder params = new StringBuilder();

		/*String description = task.getDescription();
		if(URLEncoder.encode(description,"UTF-8").length() > 3000){
			description = URLEncoder.encode(description,"UTF-8").substring(0,3000);
		}

		String title = task.getVideoTitle() + " " + getRndStr();
		if(URLEncoder.encode(title,"UTF-8").length() > 255){
			title = URLEncoder.encode(title,"UTF-8").substring(0,255);
		}*/
		
		String description = task.getVideoDescription();
		if(description.length() > 3000){
			description = URLEncoder.encode(description.substring(0,2900),"UTF-8");
		}else{
			description = URLEncoder.encode(description,"UTF-8");
		}

		String title = task.getVideoTitle() + " " + getRndStr();
		title = Utils.getFirstSmblUpper(title);
		if(title.length() > 255){
			title = URLEncoder.encode(title.substring(0,250),"UTF-8");
		}else{
			title = URLEncoder.encode(title,"UTF-8");
		}

		log.trace(String.format("EDIT NEWS: \r\nID [%s]; \r\nTitle[%s]; \r\nDescription [%s]",videoId, description, title));
		
		params.append("")
		.append("form_name=").append("dm_pageitem_uploadnewform_").append(videoId).append("&")
		.append("_csrf=").append(account.getCookie("_csrf/form")).append("&")
		.append("_fid=").append("").append("&")
		//TODO Create new title
		.append("video_title=").append( title ).append("&")
		.append("user_category=").append(themes[rnd.nextInt(themes.length)]).append("&")
		.append("game_select=").append("").append("&")
		.append("game_select=").append("").append("&")
		.append("artist=").append("").append("&")
		.append("title=").append("").append("&")
		.append("album=").append("").append("&")
		.append("upc=").append("").append("&")
		.append("isrc=").append("").append("&")
		.append("iswc=").append("").append("&")
		.append("label=").append("").append("&")
		.append("artist_id=").append("").append("&")
		.append("track_id=").append("").append("&")
		.append("album_id=").append("").append("&")
		.append("itunes_id=").append("").append("&")
		.append("genre=").append("").append("&")
		.append("language=").append("ru").append("&")
		.append("tags_hidden=").append(URLEncoder.encode(task.getVideoTitle(),"UTF-8")).append("&")
		.append("strongtags_hidden=").append("{\"strong_tags\":{},\"daily_tags\":[").append(URLEncoder.encode(task.getTags(10, 250),"UTF-8")).append("]}").append("&")
		.append("tags=").append("").append("&")
		//TODO Create new description
		.append("description=").append( description ).append("&")
		.append("privacy=").append("0").append("&")
		.append("allow_comments=").append("1").append("&")
		//.append("allow_in_group=").append("1").append("&")
		.append("recordedOn=").append(vrdf.format(new Date(System.currentTimeMillis()-60*60*24*1000))).append("&")
		//.append("coming_next=").append("").append("&")
		//.append("videoId=").append(task.getVideoid()).append("&")
		//.append("videoUpdateTitle=").append(task.getVideoid()).append("&")
		//.append("extensionVideo=").append("").append("&")
		//.append("uploadType=").append("").append("&")
		//.append("saveStatus=").append("fail").append("&")
		//.append("from_request=").append("/video/edit/x2a5w2y_%25D0%25B1%25D0%25B5%25D0%25B7-%25D0%25BD%25D0%25B0%25D0%25B7%25D0%25B2%25D0%25B0%25D0%25BD%25D0%25B8%25D1%258F")
		.append("tvod_price=").append("0.49").append("&")
		.append("window_duration=").append("48").append("&")
		.append("paywall_start=").append("10").append("&")
		.append("from_publish=").append("1").append("&")
		.append("from_request=").append("/upload").append("&")
		.append("_csrf_l=").append(account.getCookie("_csrf/link"));

		log.trace("Params for edit video: " + params.toString());

		return params.toString();
	}

	private String getRndStr(){
		Random random = new Random();
		int length = 4 + random.nextInt(3);
		String characters = "abcdefghijklmnopqrstuvwxyz";
		char[] text = new char[length];
		for (int i = 0; i < length; i++)
		{
			text[i] = characters.charAt(random.nextInt(characters.length()));
		}
		return new String(text);
	}

}
