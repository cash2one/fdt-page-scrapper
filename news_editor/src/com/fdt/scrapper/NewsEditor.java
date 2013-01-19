/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.fdt.scrapper.task.Constants;
import com.fdt.scrapper.task.NewsTask;

/**
 *
 * @author Administrator
 */
public class NewsEditor {
	private static final Logger log = Logger.getLogger(NewsEditor.class);
	private static final Logger logExtarnal = Logger.getLogger(PosterTaskRunner.class);

	private int MIN_SNIPPET_COUNT=3;
	private int MAX_SNIPPET_COUNT=5;

	private int MIN_LINK_COUNT=3;
	private int MAX_LINK_COUNT=5;

	private int MIN_WORDS_COUNT=2;
	private int MAX_WORDS_COUNT=3;

	Random rnd = new Random();

	private NewsTask task = null;
	private Proxy proxy = null;
	private Account account = null;
	private TaskFactory taskFactory = null;

	public NewsEditor(NewsTask task, Proxy proxy, Account account, TaskFactory taskFactory) {
		this.task = task;
		this.proxy = proxy;
		this.account = account;
		this.taskFactory = taskFactory;
	}

	public String executePostNews() throws Exception {
		//get snippets
		org.jsoup.nodes.Document page = getUrlContent(task.getKeyWords());
		//Elements body = page.select("textarea[id=id_body]");
		//Elements title = page.select("input[id=id_subject]");
		//String titleValue = title.attr("value");

		//String newsContent = body.get(0).text();
		//newsContent = newsContent.replaceAll("http://directnow.me/\\?az44292", "http://directnow.me/ddpromo/\\?az44399");

		//return postNews(titleValue,newsContent);
		return "";
	}

	private String postNews(String title, String newsContent){
		HttpClient httpclient = null;
		String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + task.getKeyWords() + "edit";
		HttpPost httppost = new HttpPost(postUrl);

		try {
			//post news
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			httppost.setHeader("Cookie", account.getCookie());
			httppost.setHeader("Host","subscribe.ru");
			httppost.setHeader("Referer", postUrl);
			httppost.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			nameValuePairs.add(new BasicNameValuePair("groups", account.getGroupId()));
			httppost.setHeader("Accept-Encoding","gzip,deflate");
			httppost.setHeader("Connection","keep-alive");
			//nameValuePairs.add(new BasicNameValuePair("interests", ""));


			nameValuePairs.add(new BasicNameValuePair("subject", task.getKeyWords()));
			//Insert news content here
			nameValuePairs.add(new BasicNameValuePair("body", newsContent));
			nameValuePairs.add(new BasicNameValuePair("file", title));
			nameValuePairs.add(new BasicNameValuePair("ttype", "0"));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

			httpclient = new DefaultHttpClient();	
			HttpResponse response = httpclient.execute(httppost);
			org.jsoup.nodes.Document page = Jsoup.parse(response.getEntity().getContent(), "UTF-8", postUrl);
			Elements elements = page.select("a[href]");
			System.out.println(elements.attr("href"));
			log.info(elements.attr("href"));
			return elements.attr("href");
		} catch (ClientProtocolException e) {
			logExtarnal.error("Error occured during posting news",e);
		} catch (IOException e) {
			logExtarnal.error("Error occured during posting news",e);
		}

		return "";
	}

	private org.jsoup.nodes.Document getUrlContent(String keyWords) throws MalformedURLException, IOException {
		HttpsURLConnection conn = null;
		InputStream is = null;
		try{
			//String strUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL)+ keyWords + "edit/";
			String strUrl = "https://www.google.com/search?q=\"Date+Registered:\"+\"Last+Active:\"+\"ICQ:\"+\"AIM:\"+\"MSN:\"+\"YIM:\"+\"Email:\"+\"Website:\"+\"Login+with+username\"+\"" + keyWords +"\"&num=100&hl=en&tbo=d&adtest=on&ip=0.0.0.0&noj=1&nomo=1&nota=1&igu=1&tci=g:2112,p:30000&glp=1&uule=w+CAIQICIHQmVsYXJ1cw&ei=hXX6UNH_DdCM4gTsn4CoDA&start=10&sa=N&biw=1903&bih=3200";
			URL url = new URL(strUrl);
			//using proxy
			//conn = (HttpURLConnection)url.openConnection(proxy);
			conn = (HttpsURLConnection)url.openConnection();
			//conn.setRequestProperty("Cookie", account.getCookie());
			//don't using proxy
			//conn = (HttpURLConnection)url.openConnection();
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			try{
				is = conn.getInputStream();
			}catch(IOException e){
				throw e;
			}finally{
				if(conn.getResponseCode() != 200){
					//System.exit(-1);
					log.fatal("****************************");
				}
				String msg = "RESPONCE CODE: " + conn.getResponseCode() + "[" + strUrl + "]";
				log.fatal(msg);
				//System.out.println(msg);
			}

			org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", strUrl);
			is.close();
			is = null;
			conn.disconnect();
			conn = null;
			//System.out.println(page);
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
}
