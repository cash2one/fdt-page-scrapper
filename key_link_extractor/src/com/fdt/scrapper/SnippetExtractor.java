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
import java.util.ArrayList;
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
	private static final String MAX_ATTEMPT_COUNT_LABEL = "max_attempt_count";

	private static final Logger log = Logger.getLogger(SnippetExtractor.class);

	private ProxyFactory proxyFactory = null;
	
	private SnippetTask task= null;
	

	public SnippetExtractor(SnippetTask snippetTask, ProxyFactory proxyFactory) throws MalformedURLException, IOException {
		super();
		this.proxyFactory = proxyFactory;
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
		StringBuilder snippetsContent = new StringBuilder();
		for(Snippet snippet : snippets){
		    snippetsContent.append(snippet.getLink()).append("\r\n");
		}
		return snippetsContent.toString();
	}

	private TagNode loadPageContent(SnippetTask snippetTask, Proxy proxy) throws MalformedURLException, IOException {
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
			conn.addRequestProperty("Host","search.ukr.net");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.addRequestProperty("Accept-Encoding","gzip");

			HtmlCleaner cleaner = new HtmlCleaner();

			is = conn.getInputStream();

			String encoding = conn.getContentEncoding();

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

				/*org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", strUrl);
		System.out.println(page);*/
				html = cleaner.clean(new ByteArrayInputStream(pageStr.toString().getBytes()));
				//System.out.println(pageStr.toString());
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

	public ArrayList<Snippet> extractSnippetsFromPageContent(SnippetTask snippetTask) throws MalformedURLException, IOException, XPathExpressionException{
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
		Object[] links= null;

		try {
			titles = page.evaluateXPath(snippetTask.getXpathTitle());
			descs = page.evaluateXPath(snippetTask.getXpathDesc());
			if(snippetTask.getXpathLink() != null && !"".equals(snippetTask.getXpathLink().trim())){
			    links = page.evaluateXPath(snippetTask.getXpathLink());
			}
		}
		catch (XPatherException e) {
			log.error("Error occured during getting titles and their desc",e);
		}

		int minLenght = titles.length > descs.length?descs.length:titles.length;
		if(titles.length > 0){
			for(int i = 0; i < minLenght; i++){
				String h3Value = ((TagNode)titles[i]).getText().toString();
				String pValue = ((TagNode)descs[i]).getText().toString();
				String linkValue = ((TagNode)links[i]).getText().toString();
				if(h3Value != null && !"".equals(h3Value.trim()) && pValue != null && !"".equals(pValue.trim())){
					snippets.add(new Snippet(h3Value, pValue, linkValue));
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
