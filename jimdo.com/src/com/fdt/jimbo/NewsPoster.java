/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.jimbo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fdt.jimbo.task.NewsTask;

/**
 *
 * @author Administrator
 */
public class NewsPoster {
	private static final Logger log = Logger.getLogger(NewsPoster.class);
	
	private final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

	public final static String UPLOAD_CONTEXT_URL_LABEL = "upload_context_url";

	private static final String LINE_FEED = "\r\n";

	private NewsTask task = null;
	private Proxy proxy = null;
	private Account account = null;

	private Integer times[];

	private static final String TIME_STAMP_FORMAT = "HH:mm:ss.SSS";

	private SimpleDateFormat sdf = new SimpleDateFormat(TIME_STAMP_FORMAT);
	
	private AccountFactory accountFactory = null;

	public NewsPoster(NewsTask task, Proxy proxy, Account account, AccountFactory accountFactory) {
		this.task = task;
		this.proxy = proxy;
		this.account = account;
		this.accountFactory = accountFactory;
	}

	private String postNews() throws Exception{
		//String lri = 	
		HashMap<String,String> pageDetails = createNewPost();
		//removeAllDrafts(pageDetails);
		executeRequestToGetCookies("http://www400.jimdo.com/app/cms/poll/status/", "GET", null, null);
		pageDetails = createHtmlForm(pageDetails);
		pageDetails = postHtmlForm(pageDetails);
		pageDetails = postNews(pageDetails);
		String postUrl = executeRequestToGetCookies("http://www400.jimdo.com/app/cms/preview/index/pageId/" + pageDetails.get("pageId"), "GET",  new IResultExtractor() {
			private String responseStr;

			public void init(String responseStr){
				this.responseStr = responseStr;
			}

			@Override
			public String getResult() {
				Document html = Jsoup.parse(responseStr.toString());

				String url = html.select("a[id=j-prev-url-anchor]").get(0).attr("href");

				return url;
			}
		});
		/*if(uploadUrl == null || "".equals(uploadUrl.trim())){
			throw new Exception("Upload url was not extracted.");
		}*/

		return postUrl;
	}
	
	public String executePostNews(Integer[] times) throws Exception {
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.times = times;
		return postNews();
	}

	private void removeAllDrafts(HashMap<String,String> pageDetails) throws IOException, ParseException{
		String jsonResp = executeRequestToGetCookies("http://www400.jimdo.com/app/siteadmin/blogposting/postinglist", "POST",  new IResultExtractor() 
		{
			private String responseStr;

			public void init(String responseStr){
				this.responseStr = responseStr;
			}

			@Override
			public String getResult() {
				return this.responseStr;
			}
		}, getPostListArgs(pageDetails));
		
		JSONObject jsonObj = new JSONObject(jsonResp);
		
		JSONArray array = jsonObj.getJSONObject("payload").getJSONArray("data");

		String key = "";
		int index = 0;
		while(!array.isNull(index)) {
			JSONObject post = array.getJSONObject(index++);
			if(!post.getBoolean("published")){
				executeRequestToGetCookies("http://www400.jimdo.com/app/flex/flex/delete/flexId/" + post.getString("removeFlexId"), "GET", null);
			}
		}
		
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
			conn.addRequestProperty("Referer","http://www400.jimdo.com/app/siteadmin/settings/");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0"); 
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Cookie", account.getCookies(new String[]{"JDI","shd","ClickAndChange","safemode","_jimBlob","_gat_jimBlob"}));

			int respCode = conn.getResponseCode();
		
			StringBuilder responseStr = getResponseAsString(conn);
			
			log.debug(responseStr.toString());
			
			log.debug(responseStr.toString());
			
			String fileStr = "newPost";
			
			int index = responseStr.indexOf(fileStr) + fileStr.length() + 12;
			
			String flexId = responseStr.substring(index, index + 20);
			
			flexId = flexId.substring(0,flexId.indexOf(","));
			pageDetails.put("flexId", flexId);

			Document html = Jsoup.parse(responseStr.toString());

			String cdata = html.select("script[type=text/javascript]").get(2).childNode(0).toString().replaceAll("\r\n", " ");
			
			String matrixId = html.select("div[class=post j-blog-content] > div[id]").get(0).attr("id").substring(10);
			
			log.debug(cdata);

			String json = cdata.substring(cdata.indexOf("{"),cdata.lastIndexOf("}")+1);

			JSONObject jsonObj = new JSONObject(json);

			pageDetails.put("cstok", jsonObj.getString("cstok"));
			pageDetails.put("pageId", String.valueOf(jsonObj.getLong("pageId")));
			pageDetails.put("websiteId", jsonObj.getString("websiteId"));
			pageDetails.put("ClickAndChange", jsonObj.getJSONObject("session").getString("ClickAndChange"));
			pageDetails.put("Referer", conn.getURL().toString());
			
			Iterator iterator = jsonObj.getJSONObject("matrixes").keys();
			JSONObject obj = jsonObj.getJSONObject("matrixes");

			String key = "";
			while(iterator.hasNext()) {
				key = (String)iterator.next();
			}
			pageDetails.put("matrixId", matrixId);

			

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
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0"); 
			conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			String cookies = account.getCookies(new String[]{"JDI","shd","ClickAndChange","safemode","_jimBlob","_gat_jimBlob"});
			cookies = account.getCookies(new String[]{"ClickAndChange"});
			conn.setRequestProperty("Cookie", cookies);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			//conn.setRequestProperty("Content-Length","174");
			
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
			
			if(respCode >= 400){
				accountFactory.rejectAccount(account);
			}
			
			StringBuilder responseStr = getResponseAsString(conn, respCode);

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
			conn.setRequestProperty("Cookie", account.getCookies(new String[]{"JDI","ClickAndChange"}));
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("moduleId", pageDetails.get("moduleId")));
			nameValuePairs.add(new BasicNameValuePair("type", "htmlCode"));
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
			conn.setRequestProperty("Cookie", account.getCookies(new String[]{"JDI","ClickAndChange"}));
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("title", task.getTitle()));
			nameValuePairs.add(new BasicNameValuePair("commentAllowed", "1"));
			nameValuePairs.add(new BasicNameValuePair("category", ""));
			nameValuePairs.add(new BasicNameValuePair("flexId", pageDetails.get("flexId")));
			nameValuePairs.add(new BasicNameValuePair("published_time", dateFormat.format(new Date(System.currentTimeMillis()))));
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

			log.info("Error count:" + jsonObj.getString("error"));
			
			conn.disconnect();
		}
		finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return pageDetails;
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
	
	private StringBuilder getResponseAsString(HttpURLConnection conn) throws IOException{
		InputStream is = conn.getInputStream();
		StringBuilder result = getResponseAsString(is);
		is.close();
		return result;
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

	private String executeRequestToGetCookies(String postUrl, String requestMethod, IResultExtractor resultExtractor) throws IOException{
		return executeRequestToGetCookies( postUrl, requestMethod, resultExtractor, null);
	}

	private String executeRequestToGetCookies(String postUrl, String requestMethod, IResultExtractor resultExtractor, String postParams) throws IOException{
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

	private String getRndStr()
	{
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
	
	private String getPostListArgs(HashMap<String,String> pageDetails) {
		StringBuilder params = new StringBuilder();
		params.append("cstok=").append(pageDetails.get("cstok"));
		params.append("websiteid=").append(pageDetails.get("websiteId"));
		params.append("pageid=").append(pageDetails.get("pageId"));

		return params.toString();
	}

}
