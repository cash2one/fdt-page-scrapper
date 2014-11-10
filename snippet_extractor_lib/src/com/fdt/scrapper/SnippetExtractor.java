/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTask;

/**
 *
 * @author Administrator
 */
public class SnippetExtractor {
	private static final String MAX_LINK_COUNT_LABEL = "MAX_LINK_COUNT";
	private static final String MIN_LINK_COUNT_LABEL = "MIN_LINK_COUNT";

	private static final String MAX_SNIPPET_COUNT_LABEL = "MAX_SNIPPET_COUNT";
	private static final String MIN_SNIPPET_COUNT_LABEL = "MIN_SNIPPET_COUNT";

	private static final String MAX_ATTEMPT_COUNT_LABEL = "max_attempt_count";

	private static final Logger log = Logger.getLogger(SnippetExtractor.class);

	private int MIN_SNIPPET_COUNT=3;
	private int MAX_SNIPPET_COUNT=9;

	private int MIN_LINK_COUNT=3;
	private int MAX_LINK_COUNT=9;

	private int MIN_WORDS_COUNT=2;
	private int MAX_WORDS_COUNT=5;

	private int LINKS_COUNT = 100;

	Random rnd = new Random();

	private ProxyFactory proxyFactory = null;
	private ArrayList<String> linkList = null;
	
	private SnippetTask task= null;
	

	public SnippetExtractor(SnippetTask snippetTask, ProxyFactory proxyFactory, ArrayList<String> linkList) throws MalformedURLException, IOException {
		super();
		MIN_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL));
		MAX_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL));
		MIN_LINK_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_LINK_COUNT_LABEL));
		MAX_LINK_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_LINK_COUNT_LABEL));
		
		this.proxyFactory = proxyFactory;
		this.linkList = linkList;
		task = snippetTask;
	}

	public synchronized void insertLinksToSnippets(SnippetTask snippetTask) {
		//get snippets
		String snippetContent = null;
		int attempt = 0;
		int maxAttemptCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_ATTEMPT_COUNT_LABEL));
		do{
			try{
				ArrayList<Snippet> snippets = extractSnippetsFromPageContent(snippetTask);
				if(snippets == null || snippets.size() == 0){
					throw new Exception("Snippets size is 0. Will try to use another proxy server");
				}
				snippetContent = getSnippetsContent(snippets);
			}catch(Exception e){
				log.error("Error during getting snippets content",e);
				e.printStackTrace();
			}
			attempt++;
		}while((snippetContent == null || "".equals(snippetContent.trim())) && attempt < maxAttemptCount);
		
		task.setResult(snippetContent);
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
		log.debug("Keywords: task.getKeyWords(). Snippet count: " + snipCount);
		StringBuilder snippetsContent = new StringBuilder();

		//get links count
		int randomValue = getRandomValue(MIN_LINK_COUNT, MAX_LINK_COUNT);
		if(randomValue > LINKS_COUNT){
			linkCount = LINKS_COUNT;
		}else{
			linkCount = randomValue;
		}
		int snippetLinked = 0;

		int indexShift = getRandomValue(0,snippets.size()-snipCount); 

		for(int i = indexShift; i < (snipCount+indexShift); i++){
			//add link to snipper
			if(snippetLinked < linkCount){
				//add snippet link
				int randomSuccessLink = getRandomValue(1,linkList.size()-1);
				addLinkToSnippetContent(snippets.get(i), linkList.get(randomSuccessLink));
				snippetsContent.append(snippets.get(i).toString()).append("\r\n");
				snippetLinked++;
			}else{
				snippetsContent.append(snippets.get(i).toString()).append("\r\n");
			}
		}

		return snippetsContent.toString();
	}

	private TagNode loadPageContent(SnippetTask snippetTask, Proxy proxy) throws MalformedURLException, IOException, ParseException {
		HttpURLConnection conn = null;
		InputStream is = null;
		System.out.println("Using proxy: " + proxy.toString());
		try{
			String strUrl = snippetTask.getFullUrl();
			URL url = new URL(strUrl);
			System.out.println(strUrl);
			//using proxy
			conn = (HttpURLConnection)url.openConnection(proxy);
			conn.setConnectTimeout(30000);
			conn.addRequestProperty("Host",snippetTask.getHost());
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.addRequestProperty("Accept-Encoding","gzip");
			fillExtraParamsFromTask(conn, snippetTask);

			HtmlCleaner cleaner = new HtmlCleaner();

			is = conn.getInputStream();

			String encoding = conn.getContentEncoding();
			
			InputStream inputStreamPage = null;

			TagNode html = null;
			//working with gzip encoding
			if("gzip".equalsIgnoreCase(encoding)){
				GZIPInputStream gzip = new GZIPInputStream(is);
				BufferedReader bfRdr = new BufferedReader(new InputStreamReader(gzip,"UTF-8"));
				String str = "";
				StringBuilder pageStr = new StringBuilder();

				str = bfRdr.readLine();
				while (str != null) {
					pageStr.append(str);
					str = bfRdr.readLine();
				}
				bfRdr.close();
				gzip.close();

				inputStreamPage = new ByteArrayInputStream(pageStr.toString().getBytes("UTF-8"));
				html = cleaner.clean(inputStreamPage,"UTF-8");
				
				System.out.println(pageStr.toString());
			}else{
				html = cleaner.clean(is,"UTF-8");
			}
			//int code = conn.getResponseCode();
			return html;
		}finally{
			if(conn != null){
				try{conn.disconnect();}catch(Throwable e){}
			}
			if(is != null){
				try{is.close();}catch(Throwable e){}
			}
		}
	}
	
	private void fillExtraParamsFromTask(HttpURLConnection connection, SnippetTask task){
		for(String key : task.getExtraParams().keySet()){
			connection.addRequestProperty(key, task.getExtraParams().get(key));
		}
	}
	
	private HttpURLConnection executeURL(String strUrl, Proxy proxy) throws IOException{
		//String strUrl = snippetTask.getFullUrl();
		URL url = new URL(strUrl);
		System.out.println(strUrl);
		//using proxy
		HttpURLConnection conn = (HttpURLConnection)url.openConnection(proxy);
		conn.setConnectTimeout(30000);
		conn.addRequestProperty("Host","www.google.ru");
		conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
		conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
		conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.addRequestProperty("Accept-Encoding","gzip");
		
		@SuppressWarnings("unused")
		int code = conn.getResponseCode();
		
		return conn;
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

	public ArrayList<Snippet> extractSnippetsFromPageContent(SnippetTask snippetTask) throws MalformedURLException, IOException, XPathExpressionException, ParseException{
		ArrayList<Snippet> snippets = new ArrayList<Snippet>();

		String proxyTypeStr = ConfigManager.getInstance().getProperty("proxy_type");

		ProxyConnector proxyConnector = proxyFactory.getProxyConnector();
		TagNode page = null;
		try{
			page = loadPageContent(snippetTask,proxyConnector.getConnect(proxyTypeStr));
		}finally{
			if(proxyConnector != null){
				proxyFactory.releaseProxy(proxyConnector);
			}
		}

		Object[] titles = null;
		Object[] descs= null;

		try {
			titles = page.evaluateXPath(snippetTask.getXpathTitle());
			descs = page.evaluateXPath(snippetTask.getXpathDesc());
		}
		catch (XPatherException e) {
			log.error("Error occured during getting titles and their desc",e);
		}

		int minLenght = titles.length > descs.length?descs.length:titles.length;
		if(titles.length > 0){
			for(int i = 0; i < minLenght; i++){
				String h3Value = ((TagNode)titles[i]).getText().toString();
				String pValue = ((TagNode)descs[i]).getText().toString();
				if(h3Value != null && !"".equals(h3Value.trim()) && pValue != null && !"".equals(pValue.trim())){
					snippets.add(new Snippet(h3Value, pValue));
				}
			}
		}

		return snippets;
	}
	
	public SnippetTask getTask() {
		return task;
	}

	public SnippetTask extractSnippets()
	{
		synchronized (this)
		{
			try {
				insertLinksToSnippets(task);
				return task;
			}
			catch (Exception e) {
				log.error("Error occured during getting snippets",e);
				return null;
			}
		}
	}
}
