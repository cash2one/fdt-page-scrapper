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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.fdt.scrapper.task.Constants;
import com.fdt.scrapper.task.NewsTask;
import com.fdt.scrapper.task.Snippet;

/**
 *
 * @author Administrator
 */
public class NewsPoster {
    private static final Logger log = Logger.getLogger(NewsPoster.class);

    private int MIN_SNIPPET_COUNT=3;
    private int MAX_SNIPPET_COUNT=10;

    Random rnd = new Random();

    private NewsTask task = null;
    private Proxy proxy = null;
    private Account account = null;

    public NewsPoster(NewsTask task, Proxy proxy, Account account) {
	this.task = task;
	this.proxy = proxy;
	this.account = account;
    }

    public String executePostNews() throws MalformedURLException, IOException, XPathExpressionException, ParserConfigurationException, SAXException {
	//get snippets
	ArrayList<Snippet> snippets = parseHtml(task.getKeyWords());
	//TODO calculate snippets count
	int snipCount = 0;
	if(snippets.size() <= MIN_SNIPPET_COUNT){
	    snipCount = snippets.size();
	}else{
	    int randomValue = MIN_SNIPPET_COUNT + rnd.nextInt(MAX_SNIPPET_COUNT - MIN_SNIPPET_COUNT+1);
	    if(randomValue <= snippets.size()){
		snipCount = randomValue;
	    }else{
		snipCount = snippets.size();
	    }
	}
	log.debug("Keywords: task.getKeyWords(). Snippet count: " + snipCount);
	//TODO post news
	return postNews(snippets);
    }

    private String postNews(ArrayList<Snippet> snippets){
	HttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost = new HttpPost(Constants.MAIN_ULR + Constants.POST_NEWS_ULR);

	try {
	    //post news
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    httppost.setHeader("Cookie", account.getCookie());
	    nameValuePairs.clear();
	    nameValuePairs.add(new BasicNameValuePair("groups", account.getGroupId()));
	    nameValuePairs.add(new BasicNameValuePair("interests", ""));
	    nameValuePairs.add(new BasicNameValuePair("subject", task.getKeyWords()));
	    //TODO Insert news content here
	    nameValuePairs.add(new BasicNameValuePair("body", "<p>He 3</p>"));
	    nameValuePairs.add(new BasicNameValuePair("file", ""));
	    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	    httpclient = new DefaultHttpClient();
	    HttpResponse response = httpclient.execute(httppost);
	    org.jsoup.nodes.Document page = Jsoup.parse(response.getEntity().getContent(), "UTF-8", Constants.MAIN_ULR + Constants.POST_NEWS_ULR);
	    Elements elements = page.select("a[href]");
	    System.out.println(elements.attr("href"));
	    return elements.attr("href");
	} catch (ClientProtocolException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return "";
    }

    private org.jsoup.nodes.Document getUrlContent(String keyWords) throws MalformedURLException, IOException {
	String strUrl = "http://search.tut.by/?str="+keyWords;
	URL url = new URL(strUrl);
	//TODO using proxy
	//HttpURLConnection conn = (HttpURLConnection)url.openConnection(proxy);
	//TODO using proxy
	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
	conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
	InputStream is = conn.getInputStream();
	org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", strUrl);
	is.close();
	conn.disconnect();
	return page;
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
		    snippets.add(new Snippet(h3Value, pValue));
		}
	    }
	}
	return snippets;
    }
}
