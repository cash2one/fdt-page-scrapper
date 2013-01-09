/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fdt.scrapper.task.Constants;
import com.fdt.scrapper.task.NewsTask;
import com.fdt.scrapper.task.Snippet;

/**
 *
 * @author Administrator
 */
public class NewsPoster {
	private static final Logger log = Logger.getLogger(NewsPoster.class);
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

	public NewsPoster(NewsTask task, Proxy proxy, Account account, TaskFactory taskFactory) {
		this.task = task;
		this.proxy = proxy;
		this.account = account;
		this.taskFactory = taskFactory;
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

	private String postNews(ArrayList<Snippet> snippets){
		HttpClient httpclient = null;
		String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + Constants.getInstance().getProperty(AccountFactory.POST_NEWS_URL_LABEL);
		HttpPost httppost = new HttpPost(postUrl);

		try {
			//post news
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			httppost.setHeader("Cookie", account.getCookie());
			//httppost.setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			//httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			nameValuePairs.add(new BasicNameValuePair("groups", account.getGroupId()));
			nameValuePairs.add(new BasicNameValuePair("interests", ""));


			nameValuePairs.add(new BasicNameValuePair("subject", task.getKeyWords()));
			//Insert news content here
			String snippetsContent = getSnippetsContent(snippets);
			task.getNewsContent().put("SNIPPETS", snippetsContent);
			task.getNewsContent().put("KEY_WORDS", task.getKeyWords());
			nameValuePairs.add(new BasicNameValuePair("body", mergeTemplate(task)));
			nameValuePairs.add(new BasicNameValuePair("file", ""));
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

	private String mergeTemplate(NewsTask task){
		//subject
		StringWriter writer = new StringWriter();
		taskFactory.getBottomTemplate().merge(task.getNewsContent(), writer);
		return writer.toString();
	}

	private String getSnippetsContent(ArrayList<Snippet> snippets) {
		//calculate snippets count
		int snipCount = 0;
		int linkCount = 0;
		if(snippets.size() <= MIN_SNIPPET_COUNT){
			snipCount = snippets.size();
		}else{
			int randomValue = getRandomValue(MIN_SNIPPET_COUNT, MAX_SNIPPET_COUNT);
			if(randomValue <= snippets.size()){
				snipCount = randomValue;
			}else{
				snipCount = snippets.size();
			}
		}
		logExtarnal.debug("Keywords: task.getKeyWords(). Snippet count: " + snipCount);
		StringBuilder snippetsContent = new StringBuilder();

		//get links count
		int randomValue = getRandomValue(MIN_LINK_COUNT, MAX_LINK_COUNT);
		if(randomValue > taskFactory.getSuccessQueue().size()){
			linkCount = taskFactory.getSuccessQueue().size();
		}else{
			linkCount = randomValue;
		}
		int snippetLinked = 0;
		for(int i = 0; i < snipCount; i++){
			//add link to snipper
			if(snippetLinked < linkCount){
				//add snippet link
				int randomSuccessLink = getRandomValue(1,taskFactory.getSuccessQueue().size());
				addLinkToSnippetContent(snippets.get(i), Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + taskFactory.getSuccessQueue().get(randomSuccessLink-1).getResult());
				snippetsContent.append(snippets.get(i).toString()).append("\r\n");
				snippetLinked++;
			}else{
				snippetsContent.append(snippets.get(i).toString()).append("\r\n");
			}
		}

		return snippetsContent.toString();
	}

	private org.jsoup.nodes.Document getUrlContent(String keyWords) throws MalformedURLException, IOException {
		HttpURLConnection conn = null;
		InputStream is = null;
		try{
			String strUrl = "http://search.tut.by/?status=1&ru=1&encoding=1&page=0&how=rlv&query="+keyWords.replace(" ", "+");
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

	private void addLinkToSnippetContent(Snippet snippet, String link){
		StringBuilder newContent = new StringBuilder(snippet.getContent());
		//find random
		String[] words = snippet.getContent().split(" ");
		//all snippet will be as link
		if(words.length == 1 || words.length == 2){
			//insert link here
			newContent = new StringBuilder();
			newContent.append("<a href=\""+link+"\">");
			for(int i = 0; i < words.length; i++){
				newContent.append(words[i]).append(" ");
			}
			newContent.setLength(newContent.length()-1);
			newContent.append("</a>");
		}else if(words.length > 2){
			int randomValue = getRandomValue(MIN_WORDS_COUNT, MAX_WORDS_COUNT);
			int startStringIndex = getRandomValue(0, words.length-randomValue);
			newContent = new StringBuilder();
			for(int i = 0; i < words.length; i++){
				if(startStringIndex == i){
					newContent.append("<a href=\""+link+"\">").append(words[i]).append(" ");
					continue;
				}else if((startStringIndex + randomValue-1) == i){
					newContent.append(words[i]).append("</a>").append(" ");
					continue;
				}
				newContent.append(words[i]).append(" ");
			}
			if(newContent.length() > 0){
				newContent.setLength(newContent.length()-1);
			}
		}
		snippet.setContent(newContent.toString());
	}

	private Integer getRandomValue(Integer minValue, Integer maxValue){
		return  minValue + rnd.nextInt(maxValue - minValue+1);
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
