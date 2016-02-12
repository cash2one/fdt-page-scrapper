/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.jimbo;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.fdt.jimbo.task.NewsTask;

/**
 *
 * @author Administrator
 */
public class NewsPoster {
	private static final Logger log = Logger.getLogger(NewsPoster.class);

	public final static String UPLOAD_CONTEXT_URL_LABEL = "upload_context_url";

	private static final String LINE_FEED = "\r\n";

	private static final Random rnd = new Random();;

	//private String[] themes = new String[]{"auto","webcam","animals","creation","lifestyle","people","music","news","school","travel","sport","tech","shortfilms","fun"};
	private String[] themes = new String[]{"shortfilms"};

	private NewsTask task = null;
	private Proxy proxy = null;
	private Account account = null;

	private Integer times[];

	private static final String TIME_STAMP_FORMAT = "HH:mm:ss.SSS";
	private static final String VIDEO_RECORD_FORMAT = "yyyy/MM/dd";

	private SimpleDateFormat sdf = new SimpleDateFormat(TIME_STAMP_FORMAT);
	private SimpleDateFormat vrdf = new SimpleDateFormat(VIDEO_RECORD_FORMAT);

	public NewsPoster(NewsTask task, Proxy proxy, Account account) {
		this.task = task;
		this.proxy = proxy;
		this.account = account;
	}

	private String postNews() throws Exception{
		//String lri = 	
		HashMap<String,String> pageDetails = createNewPost();
		executerequestToGetCookies("http://www400.jimdo.com/app/cms/poll/status/", "GET", null, null);
		pageDetails = createHtmlForm(pageDetails);
		pageDetails = postHtmlForm(pageDetails);
		pageDetails = postNews(pageDetails);
		/*if(uploadUrl == null || "".equals(uploadUrl.trim())){
			throw new Exception("Upload url was not extracted.");
		}*/

		return "";
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

	private HashMap<String,String> createNewPost() throws Exception
	{
		HashMap<String,String> pageDetails = new HashMap<String,String>();

		String postUrl = "http://www400.jimdo.com/app/flex/flex/create/";

		HttpURLConnection conn = null;

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

			conn.setRequestProperty("Host", "www400.jimdo.com");
			conn.addRequestProperty("Referer","http://www400.jimdo.com/app/siteadmin/upgrade/index/");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0"); 
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Cookie", account.getCookies());

			int respCode = conn.getResponseCode();
			
			StringBuilder responseStr = getResponseAsString(conn);
			
			//log.debug(responseStr.toString());
			
			String fileStr = "newPost";
			
			int index = responseStr.indexOf(fileStr) + fileStr.length() + 12;
			
			String flexId = responseStr.substring(index, index + 20);
			
			flexId = flexId.substring(0,flexId.indexOf(","));

			Document html = Jsoup.parse(responseStr.toString());

			String cdata = html.select("script[type=text/javascript]").get(2).childNode(0).toString().replaceAll("\r\n", " ");
			
			String matrixId = html.select("div[class=post j-blog-content] > div[id]").get(0).attr("id").substring(10);
			
			log.debug(cdata);

			String json = cdata.substring(cdata.indexOf("{"),cdata.lastIndexOf("}")+1);

			JSONObject jsonObj = new JSONObject(json);

			pageDetails.put("cstok", jsonObj.getString("cstok"));
			pageDetails.put("pageId", String.valueOf(jsonObj.getInt("pageId")));
			pageDetails.put("websiteId", jsonObj.getString("websiteId"));
			pageDetails.put("ClickAndChange", jsonObj.getJSONObject("session").getString("ClickAndChange"));
			pageDetails.put("Referer", conn.getURL().toString());
			
			Iterator iterator = jsonObj.getJSONObject("matrixes").keys();

			String key = "";
			while(iterator.hasNext()) {
				key = (String)iterator.next();
			}
			pageDetails.put("matrixId", key);

			//log.debug(responseStr.toString());

			conn.disconnect();
		}
		finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return pageDetails;
	}

	private HashMap<String,String> createHtmlForm(HashMap<String,String> pageDetails) throws Exception
	{
		String postUrl = "http://www400.jimdo.com/app/module/matrix/create";

		HttpURLConnection conn = null;

		try{
			//post news
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(true);
			conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestProperty("Host", "www400.jimdo.com");
			conn.addRequestProperty("Referer", pageDetails.get("Referer"));
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0"); 
			conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("DNT","1");
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestProperty("Cookie", account.getCookies());
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("data", "{\"matrixId\":\""+ pageDetails.get("matrixId") + "\",\"order\":[\"htmlCode\"]}"));
			nameValuePairs.add(new BasicNameValuePair("cstok", pageDetails.get("cstok")));
			nameValuePairs.add(new BasicNameValuePair("websiteid", pageDetails.get("websiteId")));
			nameValuePairs.add(new BasicNameValuePair("pageid", pageDetails.get("pageId")));
			
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();

			int respCode = conn.getResponseCode();
			
			StringBuilder responseStr = getResponseAsString(conn);

			JSONObject jsonObj = new JSONObject(responseStr.toString());

			pageDetails.put("moduleId", jsonObj.getJSONObject("payload").getString("id"));
			
			//log.debug(responseStr.toString());

			conn.disconnect();
		}
		finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return pageDetails;
	}
	
	private HashMap<String,String> postHtmlForm(HashMap<String,String> pageDetails) throws Exception
	{
		String postUrl = "http://www400.jimdo.com/app/module/module/update";

		HttpURLConnection conn = null;

		try{
			//post news
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(true);
			conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestProperty("Host", "www400.jimdo.com");
			conn.addRequestProperty("Referer", pageDetails.get("Referer"));
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0"); 
			//conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestProperty("Cookie", account.getCookies());
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("moduleId", pageDetails.get("moduleId")));
			nameValuePairs.add(new BasicNameValuePair("type", pageDetails.get("htmlCode")));
			nameValuePairs.add(new BasicNameValuePair("htmlCode", task.getDescription()));
			nameValuePairs.add(new BasicNameValuePair("ClickAndChange", pageDetails.get("ClickAndChange")));
			nameValuePairs.add(new BasicNameValuePair("cstok", pageDetails.get("cstok")));
			nameValuePairs.add(new BasicNameValuePair("websiteid", pageDetails.get("websiteId")));
			nameValuePairs.add(new BasicNameValuePair("pageid", pageDetails.get("pageId")));
			
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();

			int respCode = conn.getResponseCode();
			
			StringBuilder responseStr = getResponseAsString(conn);

			JSONObject jsonObj = new JSONObject(responseStr.toString());

			log.info("Status of htmlCode post:" + jsonObj.getString("status"));
			
			conn.disconnect();
		}
		finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return pageDetails;
	}
	
	private HashMap<String,String> postNews(HashMap<String,String> pageDetails) throws Exception
	{
		String postUrl = "http://www400.jimdo.com/app/flex/flex/update";

		HttpURLConnection conn = null;

		try{
			//post news
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(true);
			conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestProperty("Host", "www400.jimdo.com");
			conn.addRequestProperty("Referer", pageDetails.get("Referer"));
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0"); 
			//conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestProperty("Cookie", account.getCookies());
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("title", task.getTitle()));
			nameValuePairs.add(new BasicNameValuePair("commentAllowed", "1"));
			nameValuePairs.add(new BasicNameValuePair("category", ""));
			nameValuePairs.add(new BasicNameValuePair("flexId", pageDetails.get("flexId")));
			nameValuePairs.add(new BasicNameValuePair("published_time", "2016-02-11 05:39"));
			nameValuePairs.add(new BasicNameValuePair("timezone_offset", "-180"));
			nameValuePairs.add(new BasicNameValuePair("publish", "1"));
			nameValuePairs.add(new BasicNameValuePair("cstok", pageDetails.get("cstok")));
			nameValuePairs.add(new BasicNameValuePair("websiteid", pageDetails.get("websiteId")));
			nameValuePairs.add(new BasicNameValuePair("pageid", pageDetails.get("pageId")));
			
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();

			int respCode = conn.getResponseCode();
			
			StringBuilder responseStr = getResponseAsString(conn);

			JSONObject jsonObj = new JSONObject(responseStr.toString());

			log.info("Status of htmlCode post:" + jsonObj.getString("status"));
			
			conn.disconnect();
		}
		finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return pageDetails;
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
			conn.setRequestProperty("Referer", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write("[{\"call\":\"POST /videos\",\"args\":{\"title\":\"" + " " +"\",\"published\":\"false\"},\"id\":0}]");
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
			conn.setRequestProperty("Referer", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

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
			conn.setRequestProperty("Referer", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

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
	//		String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
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
	//			conn.setRequestProperty("Host", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
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

		String postUrl = Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
				"/pageitem/OneStepPreview?widget_only=1&hidenextvideo=1&request="+fromRequest;

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

		conn.setRequestProperty("Host", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
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
		writer.append("Content-Disposition: form-data; name=\"save\"").append(LINE_FEED).append(LINE_FEED);
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
			conn.setRequestProperty("Host", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
			conn.setRequestProperty("Referer", Config.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

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
	
	public static String getQueryWOEncode(List<NameValuePair> params) throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params)
		{
			if (first)
				first = false;
			else
				result.append("&");

			result.append(pair.getName());
			result.append("=");
			result.append(pair.getValue());
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
