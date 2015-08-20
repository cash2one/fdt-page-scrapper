/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
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

import com.fdt.dailymotion.task.NewsTask;

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

	private NewsTask task = null;
	private Proxy proxy = null;
	private Account account = null;
	
	private Integer times[];
	
	private static final String TIME_STAMP_FORMAT = "HH:mm:ss.SSS";
	
	private SimpleDateFormat sdf = new SimpleDateFormat(TIME_STAMP_FORMAT);

	public NewsPoster(NewsTask task, Proxy proxy, Account account) {
		this.task = task;
		this.proxy = proxy;
		this.account = account;
	}

	public String executePostNews(Integer[] times) throws Exception {
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.times = times;
		return postNews();
	}
	
	private String getTimeString(){
		double milSecCnt = 0L;
		milSecCnt +=  (times[0] % times[1]) * 1000 + (times[0]/(times[1]*times[1]))*1000;
		
		String valueStr = String.format("%.0f", milSecCnt);
		
		
		return sdf.format(new Date(Long.parseLong(valueStr)-1000));
	}

	private String getUploadUrl() throws Exception{
		String postUrl = "https://api.dailymotion.com/?access_token=" + account.getCookie("sid");

		//post news
		URL url = new URL(postUrl);
		HttpsURLConnection.setFollowRedirects(false);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxy);
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
		conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write("[{\"call\":\"GET /file/upload\",\"args\":{},\"id\":0}]");
		writer.flush();
		writer.close();
		os.close();

		StringBuilder responseStr = getResponseAsString(conn);

		//log.debug(responseStr.toString());

		conn.disconnect();

		JSONObject jsonObj = new JSONObject(responseStr.substring(1, responseStr.length()-1));
		String uploadUrl = jsonObj.getJSONObject("result").getString("upload_url");

		conn.disconnect();

		log.info("Upload URL: " + uploadUrl);

		return uploadUrl;
	}

	private String getVideoId() throws Exception{
		String postUrl = "https://api.dailymotion.com/?access_token=" + account.getCookie("sid");

		//post news
		URL url = new URL(postUrl);
		HttpsURLConnection.setFollowRedirects(false);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxy);
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
		conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write("[{\"call\":\"POST /videos\",\"args\":{\"title\":\"" + task.getVideoTitle() +"\",\"published\":\"false\"},\"id\":0}]");
		writer.flush();
		writer.close();
		os.close();

		StringBuilder responseStr = getResponseAsString(conn);

		//log.debug(responseStr.toString());

		conn.disconnect();

		String videoId = null;
		JSONObject jsonObj = new JSONObject(responseStr.substring(1, responseStr.length()-1));
		try{
			videoId = jsonObj.getJSONObject("result").getString("id");
		}catch(NoSuchElementException e){
			log.error("'result' element NOT FOUND. Responce JSONObject: " + responseStr);
			throw e;
		}

		conn.disconnect();

		log.info("Video ID: " + videoId);

		return videoId;
	}

	private void postVideo(String videoUrl, String videoId) throws Exception{
		String postUrl = "https://api.dailymotion.com/?access_token=" + account.getCookie("sid");

		//post news
		URL url = new URL(postUrl);
		HttpsURLConnection.setFollowRedirects(false);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxy);
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
		conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write("[{\"call\":\"POST /video/"+videoId+"\",\"args\":{\"url\":\"" + videoUrl +"\",\"fields\":\"id,\"},\"id\":0}]");
		writer.flush();
		writer.close();
		os.close();

		StringBuilder responseStr = getResponseAsString(conn);

		//log.debug(responseStr.toString());

		conn.disconnect();
	}

	private String executeAccessToken(String videoId, String requestStr) throws Exception{
		String postUrl = "https://api.dailymotion.com/?access_token=" + account.getCookie("sid");

		//post news
		URL url = new URL(postUrl);
		HttpsURLConnection.setFollowRedirects(false);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxy);
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
		conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write(requestStr);
		writer.flush();
		writer.close();
		os.close();

		StringBuilder responseStr = getResponseAsString(conn);

		conn.disconnect();

		return responseStr.toString();
	}

	private void getVideoAccessToken(String videoId) throws Exception{
		String respStr = "";
		respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"encoding_progress,\"},\"id\":0}]");
		respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/"+videoId+"\",\"args\":{\"fields\":\"thumbnail_url,\"},\"id\":0},{\"call\":\"GET /video/x318d5d\",\"args\":{\"fields\":\"status,\"},\"id\":1}]");
		respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/" + videoId + "\",\"args\":{\"fields\":\"status,\"},\"id\":0}]");

		while(respStr.contains("processing")){
			/*if(respStr.contains("error")){
				throw new Exception("Error occured during downloading video. Responce string: " + respStr);
			}*/
			log.debug("Responce download string for video ID:" + videoId + " : " + respStr);
			Thread.sleep(5000L);
			respStr = executeAccessToken(videoId, "[{\"call\":\"GET /video/" + videoId + "\",\"args\":{\"fields\":\"status,\"},\"id\":0}]");
		}
	}

	private int executeOptionRequest(String postUrl) throws Exception{

		//post news
		URL url = new URL(postUrl);
		String host = postUrl.substring(postUrl.indexOf("http://") + 7, postUrl.indexOf(".com") + 4);
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
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

		StringBuilder responseStr = getResponseAsString(conn);
		int respCode = conn.getResponseCode();

		log.debug("OPTION request responce code: " + conn.getResponseCode());

		conn.disconnect();

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

		return uploadVideo(uploadUrl, videoId);
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

		//executeOptionRequest(postUrl);

		for (long i= 0; i <= uploadFile.length(); i += 500000) {
			log.debug(String.format("%s: Upload new segment [%d:%d]", Thread.currentThread().getId(), i-500000, i));
			executeOptionRequest(postUrl);
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(false);
			//HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
			//conn.setRequestProperty("Cookie", "ts=579019; _ga=GA1.2.1306326178.1415707891; v1st=FBD5899AD5D1E456; OAX=LjW7ClRh/OsACxGb");
			//conn.setRequestProperty("Connection", "keep-alive");
			//conn.setRequestProperty("Pragma","no-cache");
			//conn.setRequestProperty("Cache-Control", "no-cache");

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
				log.debug(responseStr.toString());
				JSONObject jsonObj = new JSONObject(responseStr.toString());
				videoPostedUrl = (String)jsonObj.getString("url");
				postVideo(videoPostedUrl, videoId);
			}

			conn.disconnect();
		}

		inputStream.close();

		executeRequestToGetCookies("http://www.dailymotion.com/upload_new/ping?t=" + (System.currentTimeMillis()), "HEAD", null);

		//edit video description
		return editVideoDescription(videoId, oldCookie, "POST");
	}

	/*private String uploadVideo(String uploadUrl, String videoId) throws Exception{

		executeRequestToGetCookies("http://www.dailymotion.com/upload", "GET", null);
		//executeRequestToGetCookies("http://www.dailymotion.com/pageitem/video/edit?request=/&t=0.6538078272511391&loop=0&from_request=/upload&_csrf_l=" + account.getCookie("_csrf/link"), "GET", null);
		//String videoId = executerequestToGetCookies("http://www.dailymotion.com/ajax/video", "POST", new VideoIdExtractor(), getAjaxVideoParamString());
		task.setVideoId(videoId);
		String videoUrl = "";

		String postUrl = task.getUploadUrl();
		postUrl = uploadUrl;

		//post news
		String boundary = "----------" + System.currentTimeMillis();

		String fileName = task.getVideoFile().getName();
		File uploadFile = task.getVideoFile();
		URL url = new URL(postUrl);
		HttpURLConnection.setFollowRedirects(false);
		//HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(60000);
		conn.setConnectTimeout(300000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.addRequestProperty("User-Agent", "Shockwave Flash"); 
		conn.setRequestProperty("Accept", "text/*");

		conn.setRequestProperty("Cookie", "sdx=" + account.getCookie("sdx") + "; " + "ts=" + account.getCookie("ts") + "; ");
		//conn.setRequestProperty("Cookie", "ts=579019; _ga=GA1.2.1306326178.1415707891; v1st=FBD5899AD5D1E456; OAX=LjW7ClRh/OsACxGb");

		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		conn.setRequestProperty("DNT","1");
		conn.setRequestProperty("Pragma","no-cache");

		OutputStream outputStream;
		outputStream = conn.getOutputStream();

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

		writer.append(LINE_FEED).append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"Filename\"").append(LINE_FEED).append(LINE_FEED);;
		writer.append(fileName).append(LINE_FEED);
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"file\"; filename=\""+fileName+"\"").append(LINE_FEED);
		writer.append("Content-Type: application/octet-stream").append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED).append(LINE_FEED);
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
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"Upload\"").append(LINE_FEED).append(LINE_FEED);
		writer.append("Submit Query").append(LINE_FEED);
		writer.append("--" + boundary + "--");//.append(LINE_FEED).append(LINE_FEED);
		writer.flush();

		writer.flush();  

		writer.close();

		outputStream.close();

		FileBody fileBody = new FileBody(new File("temp_video_audio.mov"));
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
			multipartEntity.addPart("file", fileBody);
			multipartEntity.addPart("Filename", new StringBody("temp_video_audio.mov"));
			//multipartEntity.addPart("Filename", "temp_video_audio.mov");

			try {
			    multipartEntity.writeTo(outputStream);
			} finally {
				outputStream.flush();
				outputStream.close();
			}

		StringBuilder responseStr = getResponseAsString(conn);

		//log.debug(responseStr.toString());

		conn.disconnect();

		JSONObject jsonObj = new JSONObject(responseStr.toString());
		videoUrl = (String)jsonObj.getString("url");

		//link uploaded video to user
		executeRequestToGetCookies(getVideoFormXUploadUrl(videoId, videoUrl), "GET", null);

		//Edit video description 
		return editVideoDescription();
	}*/

	private String editVideoDescription(String videoId, String oldCookie, String method) throws Exception{
		String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
				"/pageitem/uploadNewForm?request=/new&t=0.5693014024517274&xid="+videoId+"&from_request=/upload&_csrf_l=" + oldCookie;

		//post news
		URL url = new URL(postUrl);
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
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
		conn.setRequestProperty("Host", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
		conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

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
		
		log.debug("Responce string after EDIT operation: " + responseStr);

		//log.debug(responseStr.toString());

		conn.disconnect();

		Thread.sleep(10000L);
		if("POST".equals(method)){
			String videoUrl = "http://www.dailymotion.com" + executerequestToGetCookies("http://www.dailymotion.com/ajax/video", "POST", new VideoUrlExtractor(), getAjaxVideoParamStringForUrl());
			if( !videoUrl.contains("/video/") || videoUrl.contains("_%D0%B1%D0%B5%D0%B7-%D0%BD%D0%B0%D0%B7%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F")){
				throw new Exception("URL to video was not extracted. Next string was extracted: " + videoUrl);
			}
			setPreview(videoId);
			log.info("VIDEO URL: " + videoUrl);
			return videoUrl;
		}else{
			return "";
		}
	}

	private String setPreview(String videoId) throws Exception{
		String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
				"/ajax/video_preview_v3";

		//post news
		URL url = new URL(postUrl);
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
		conn.setReadTimeout(60000);
		conn.setConnectTimeout(60000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
		conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Cookie", account.getCookies());
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		conn.setRequestProperty("Host", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
		//conn.setRequestProperty("Referer", http://www.dailymotion.com/pageitem/OneStepPreview?widget_only=1&hidenextvideo=1&request=/video/x30lasl_%25D0%25BF%25D1%2580%25D0%25B5%25D1%2581%25D0%25BB%25D0%25B5%25D0%25B4%25D0%25BE%25D0%25B2%25D0%25B0%25D0%25BD%25D0%25B8%25D0%25B5-m%25D0%25BE%25D1%2582%25D0%25BE%25D1%2586%25D0%25B8%25D0%25BA%25D0%25BB%25D0%25B8%25D1%2581%25D1%2582%25D0%25B0-30-06-2015_webcam);

		OutputStream outputStream;
		outputStream = conn.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);
		writer.append(getPreviewParamString(videoId));
		writer.flush();
		writer.close();
		outputStream.close();


		int respCode = conn.getResponseCode();
		// Execute HTTP Post Request
		StringBuilder responseStr = getResponseAsString(conn);

		//log.debug(responseStr.toString());

		conn.disconnect();
		
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

		//post news
		URL url = new URL(postUrl);
		log.info("URL: " + url);
		HttpURLConnection.setFollowRedirects(false);
		//TODO Uncomment
		//HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
		conn.setRequestProperty("Host", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
		conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

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

		conn.disconnect();

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

	private String getPreviewParamString(String videoId){
		StringBuilder params = new StringBuilder();
		params.append("ajax_function").append("=").append("extract_preview").append("&");
		params.append("ajax_arg[]").append("=").append(videoId).append("&");
		params.append("ajax_arg[]").append("=").append(getTimeString()).append("&");
		params.append("_").append("=").append(String.valueOf(System.currentTimeMillis())).append("&");
		params.append("from_request").append("=").append("/upload").append("&");
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

	private String getEditVideoPostParamsUrl(String videoId) throws Exception {
		StringBuilder params = new StringBuilder();
		
		String description = task.getDescription().substring(0,3000);
		
		params.append("")
		.append("form_name=").append("dm_pageitem_uploadnewform_").append(videoId).append("&")
		.append("_csrf=").append(account.getCookie("_csrf/form")).append("&")
		.append("_fid=").append("").append("&")
		.append("video_title=").append(URLEncoder.encode(task.getVideoTitle() + " " + getRndStr(),"UTF-8")).append("&")
		.append("user_category=").append(themes[rnd.nextInt(themes.length)]).append("&")
		.append("game_select=").append("").append("&")
		.append("language=").append("en").append("&")
		.append("tags_hidden=").append(URLEncoder.encode(task.getVideoTitle(),"UTF-8")).append("&")
		.append("strongtags_hidden=").append("{\"strong_tags\":{},\"daily_tags\":[").append(URLEncoder.encode(task.getTags(10),"UTF-8")).append("]}").append("&")
		.append("tags=").append("").append("&")
		.append("description=").append( URLEncoder.encode(description,"UTF-8")).append("&")
		.append("privacy=").append("0").append("&")
		.append("allow_comments=").append("1").append("&")
		//.append("allow_in_group=").append("1").append("&")
		.append("recordedOn=").append("2015/05/14").append("&")
		//.append("coming_next=").append("").append("&")
		//.append("videoId=").append(task.getVideoid()).append("&")
		//.append("videoUpdateTitle=").append(task.getVideoid()).append("&")
		//.append("extensionVideo=").append("").append("&")
		//.append("uploadType=").append("").append("&")
		//.append("saveStatus=").append("fail").append("&")
		//.append("from_request=").append("/video/edit/x2a5w2y_%25D0%25B1%25D0%25B5%25D0%25B7-%25D0%25BD%25D0%25B0%25D0%25B7%25D0%25B2%25D0%25B0%25D0%25BD%25D0%25B8%25D1%258F")
		.append("from_publish=").append("1").append("&")
		.append("from_request=").append("/upload").append("&")
		.append("_csrf_l=").append(account.getCookie("_csrf/link"));

		log.info("Params for edit video: " + params.toString());

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
