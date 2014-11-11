/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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
import org.apache.http.message.BasicNameValuePair;
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

	private NewsTask task = null;
	private Proxy proxy = null;
	private Account account = null;

	public NewsPoster(NewsTask task, Proxy proxy, Account account) {
		this.task = task;
		this.proxy = proxy;
		this.account = account;
	}

	public String executePostNews() throws Exception {
		//get snippets
		ArrayList<Snippet> snippets = parseHtml(task.getKeyWords());
		if(snippets == null || snippets.size() == 0){
			throw new Exception("Snippets size is 0. Will try to use another proxy server");
		}
		//post news
		return postNews(snippets);
	}

	public String postNews(ArrayList<Snippet> snippets){
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
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Cookie", account.getCookies());
			conn.setRequestProperty("Host", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL));
			conn.setRequestProperty("Referer", Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + "/upload");

			// Execute HTTP Post Request
			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();
			if(cookies.get("Set-Cookie").toString().contains("notexists")){
				log.error("Account doesn't exist: \""+ account.getLogin() + "\". Please check email and password.");
			}

			for(String cookieOne: cookies.get("Set-Cookie"))
			{
				String cookiesValues[] = cookieOne.split(";");
				for(String cookiesArrayItem : cookiesValues){
					String singleCookei[] = cookiesArrayItem.split("=");
					account.addCookie(singleCookei[0].trim(), singleCookei[1].trim());
				}
			}
			
			conn.disconnect();
			
			int code = conn.getResponseCode();

			InputStream is = conn.getInputStream();

			String link = "";
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String json = reader.readLine();

				JSONObject jsonObject = new JSONObject( json );
				link = (String)jsonObject.get("id");
			}
			catch (ParseException e) {
				log.error("Error occured during posting news",e);
			}
			finally{
				if(reader != null){
					reader.close();
				}
			}

			String groupUrl = "";
			if(link != null && link.length() > 0){
				groupUrl =  ((String)link);
			}
			if(is != null){
				is.close();
			}
			conn.disconnect();

			groupUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL)+"/"+groupUrl + "/";
			System.out.println(groupUrl);
			log.info(groupUrl);

			return groupUrl;
		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		}

		return "";
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
}
