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
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fdt.dailymotion.task.NewsTask;
import com.fdt.scrapper.task.Snippet;

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

	public NewsPoster(NewsTask task, Proxy proxy, Account account) {
		this.task = task;
		this.proxy = proxy;
		this.account = account;
	}

	public String executePostNews() throws Exception {
		//TODO get snippets
		/*ArrayList<Snippet> snippets = parseHtml(task.getKeyWords());
		if(snippets == null || snippets.size() == 0){
			throw new Exception("Snippets size is 0. Will try to use another proxy server");
		}*/
		//post news
		return postNews();
	}

	private String postNews(){
		String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
				Constants.getInstance().getProperty(UPLOAD_CONTEXT_URL_LABEL) + 
				account.getCookie("_csrf/link");

		try {
			//post news
			URL url = new URL(postUrl);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Cookie", account.getCookies());
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestProperty("Host", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
			conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			// Execute HTTP Post Request
			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();

			String uploadLink = "";

			if(cookies.get("X-Json") != null)
			{
				StringBuilder strBuild = new StringBuilder(cookies.get("X-Json").toString());
				strBuild.setLength(180);
				uploadLink = strBuild.substring(49).replace("\\", "");
				task.setUploadUrl(uploadLink);
				System.out.println("Upload URL: " + uploadLink);
			}else{
				throw new IOException("Couldn't extract upload context URL");
			}

			conn.disconnect();

			log.info("Upload URL: " + uploadLink);

			uploadVideo();

			return "";
		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		}

		return "";
	}

	private String uploadVideo(){

		String cookiesTest = account.getCookies();

		executeRequestToGetCookies("http://www.dailymotion.com/upload", "GET", null);
		executeRequestToGetCookies("http://www.dailymotion.com/pageitem/video/edit?request=/&t=0.6538078272511391&loop=0&from_request=/upload&_csrf_l=" + account.getCookie("_csrf/link"), "GET", null);
		String videoId = executerequestToGetCookies("http://www.dailymotion.com/ajax/video", "POST", new VideoIdExtractor(), getAjaxVideoParamString());
		task.setVideoId(videoId);
		String videoUrl = "";

		cookiesTest = account.getCookies();

		String postUrl = task.getUploadUrl();

		try {
			//post news
			String boundary = "----------" + System.currentTimeMillis();

			String fileName = "temp_video_audio.mov";
			File uploadFile = new File(fileName);
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

			/*FileBody fileBody = new FileBody(new File("temp_video_audio.mov"));
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
			multipartEntity.addPart("file", fileBody);
			multipartEntity.addPart("Filename", new StringBody("temp_video_audio.mov"));
			//multipartEntity.addPart("Filename", "temp_video_audio.mov");

			try {
			    multipartEntity.writeTo(outputStream);
			} finally {
				outputStream.flush();
				outputStream.close();
			}*/

			int code = conn.getResponseCode();

			// Execute HTTP Post Request
			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();

			StringBuilder responseStr = getResponseAsString(conn);

			log.debug(responseStr.toString());

			conn.disconnect();

			try {
				JSONObject jsonObj = new JSONObject(responseStr.toString());
				videoUrl = (String)jsonObj.getString("url");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			executeRequestToGetCookies(getVideoFormXUploadUrl(videoId, videoUrl), "GET", null);

			//Edit video description 
			editVideoDescription();

		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		}

		return "";
	}

	private String editVideoDescription(){
		String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + 
				"/pageitem/video/edit?request=%2F&loop=0";

		try {
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
			conn.setRequestProperty("Host", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
			conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			OutputStream outputStream;
			outputStream = conn.getOutputStream();

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

			writer.append(getEditVideoPostParamsUrl());

			writer.flush();
			writer.close();
			outputStream.close();

			int code = conn.getResponseCode();

			// Execute HTTP Post Request
			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();

			StringBuilder responseStr = getResponseAsString(conn);

			log.debug(responseStr.toString());

			conn.disconnect();

			return "";
		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		}

		return "";
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

	private String executeRequestToGetCookies(String postUrl, String requestMethod, IResultExtractor resultExtractor){
		return executerequestToGetCookies( postUrl, requestMethod, resultExtractor, null);
	}

	private String executerequestToGetCookies(String postUrl, String requestMethod, IResultExtractor resultExtractor, String postParams){

		try {
			//post news
			URL url = new URL(postUrl);
			System.out.println("URL: " + url);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod(requestMethod);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Cookie", account.getCookies());
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
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

			// Execute HTTP Post Request
			int code = conn.getResponseCode();

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

			log.debug(responseStr.toString());

			if(resultExtractor != null){
				resultExtractor.init(responseStr.toString());
			}

			conn.disconnect();

		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		}

		return resultExtractor != null ? resultExtractor.getResult():"";
	}

	private org.jsoup.nodes.Document getUrlContent(String keyWords) throws MalformedURLException, IOException {
		HttpURLConnection conn = null;
		InputStream is = null;
		try{

			String strUrl = "http://search.tut.by/?rs=1&page="+"&query="+keyWords.replace(" ", "+")+"&how=rlv&ru=1&tc=0&ust="+keyWords.replace(" ", "+")+"&sh=&cg=20&cdig=1";
			URL url = new URL(strUrl);
			//using proxy
			conn = (HttpURLConnection)url.openConnection(proxy);
			//don't using proxy
			//conn = (HttpURLConnection)url.openConnection();
			//conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			//conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			is = conn.getInputStream();
			org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", strUrl);
			is.close();
			is = null;
			conn.disconnect();
			conn = null;
			return page;
		}finally{
			if(conn != null){
				try{conn.disconnect();}catch(Throwable e){}
			}
			if(is != null){
				try{is.close();}catch(Throwable e){}
			}
		}
	}

	private ArrayList<Snippet> parseHtml(String keyWords) throws MalformedURLException, IOException{
		ArrayList<Snippet> snippets = new ArrayList<Snippet>();
		org.jsoup.nodes.Document page = getUrlContent(keyWords);

		Elements elements = page.select("li[class=b-results__li]");
		if(!elements.isEmpty()){
			for(Element element : elements){
				String h3Value = element.select("h3").text();
				String pValue = element.select("p").text();
				if(h3Value != null && !"".equals(h3Value.trim()) && pValue != null && !"".equals(pValue.trim())){
					snippets.add(new Snippet(h3Value.trim(), pValue.trim()));
				}
			}
		}
		return snippets;
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
			id = responseStr.substring(14, responseStr.lastIndexOf("}"));

			return id;
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

	private String getEditVideoPostParamsUrl() {
		StringBuilder params = new StringBuilder();
		params.append("")
		.append("form_name=").append("dm_pageitem_video_edit_0").append("&")
		.append("_csrf=").append(account.getCookie("_csrf/form")).append("&")
		.append("_fid=").append("").append("&")
		.append("video_title=").append(task.getVideoTitle()).append("&")
		.append("user_category=").append(themes[rnd.nextInt(themes.length)]).append("&")
		.append("game_select=").append("").append("&")
		.append("language=").append("en").append("&")
		.append("tags_hidden=").append("").append("&")
		.append("strongtags_hidden=").append("{\"strong_tags\":{},\"daily_tags\":[]}").append("&")
		.append("tags=").append(task.getTags()).append("&")
		.append("description=").append(task.getDescription()).append("&")
		.append("allow_comments=").append("1").append("&")
		.append("allow_in_group=").append("1").append("&")
		.append("recordedOn=").append("2014/11/14").append("&")
		.append("coming_next=").append("").append("&")
		.append("videoId=").append(task.getVideoid()).append("&")
		.append("videoUpdateTitle=").append(task.getVideoid()).append("&")
		.append("extensionVideo=").append("").append("&")
		.append("uploadType=").append("").append("&")
		.append("saveStatus=").append("fail").append("&")
		//.append("from_request=").append("/video/edit/x2a5w2y_%25D0%25B1%25D0%25B5%25D0%25B7-%25D0%25BD%25D0%25B0%25D0%25B7%25D0%25B2%25D0%25B0%25D0%25BD%25D0%25B8%25D1%258F")
		.append("_csrf_l=").append(account.getCookie("_csrf/link"));

		
		return params.toString();
	}
}