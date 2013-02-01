/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.jsoup.Jsoup;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.BingSnippetTask;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.GoogleSnippetTask;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.TutSnippetTask;
import com.fdt.scrapper.task.UkrnetSnippetTask;

/**
 *
 * @author Administrator
 */
public class SnippetGeneratorThread implements Runnable {
	private static final String SOURCE_LABEL = "source";

	private static final String LOAD_PROXY_FILE_FROM_INET_LABEL = "load_proxy_file_from_inet";

	private static final String MAX_ATTEMPT_COUNT_LABEL = "max_attempt_count";

	private static final Logger log = Logger.getLogger(SnippetGeneratorThread.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String LINKS_LIST_FILE_PATH_LABEL = "links_list_file_path";

	private int MIN_SNIPPET_COUNT=3;
	private int MAX_SNIPPET_COUNT=9;

	private int MIN_LINK_COUNT=3;
	private int MAX_LINK_COUNT=9;

	private int MIN_WORDS_COUNT=2;
	private int MAX_WORDS_COUNT=5;

	private int LINKS_COUNT = 100;

	private String[] args = null;

	Random rnd = new Random();

	private ProxyFactory proxyFactory = null;
	private ArrayList<String> linkList = null;

	public SnippetGeneratorThread(String pathToLinksFile, String pathToProxyListFile, String[] consoleArgs) throws MalformedURLException, IOException {
		super();
		this.args = consoleArgs;
		String loadProxyFromInetStr = ConfigManager.getInstance().getProperty(LOAD_PROXY_FILE_FROM_INET_LABEL);
		boolean loadProxyFromInet = false;
		if(loadProxyFromInetStr != null && !"".equals(loadProxyFromInetStr.trim())){
			loadProxyFromInet = Boolean.valueOf(loadProxyFromInetStr) ;
		}

		proxyFactory = ProxyFactory.getInstance();

		if(loadProxyFromInet){
			proxyFactory.loadProxyListFromInet(pathToProxyListFile);
		}else{
			proxyFactory.loadProxyList(pathToProxyListFile);
		}

		linkList = loadLinkList(pathToLinksFile);
	}

	public synchronized ArrayList<String> loadLinkList(String cfgFilePath){
		ArrayList<String> linkList = new ArrayList<String>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(new File(cfgFilePath));
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				linkList.add(line.trim());
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.error("Reading PROPERTIES file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading PROPERTIES file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
			try {
				if(fr != null)
					fr.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
		return linkList;
	}

	public synchronized String getFixedSnippets(SnippetTask snippetTask) {
		//get snippets
		String snippetContent = null;
		int attempt = 0;
		int maxAttemptCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_ATTEMPT_COUNT_LABEL));
		do{
			try{
				ArrayList<Snippet> snippets = extractSnippets(snippetTask);
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
		return snippetContent;
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
		
		//int indexShift = getRandomValue(0,snippets.size()-snipCount);
		int indexShift = 0; 
		
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

	private TagNode getPageContent(SnippetTask snippetTask, Proxy proxy) throws MalformedURLException, IOException {
		HttpURLConnection conn = null;
		InputStream is = null;
		System.out.println("Using proxy: " + proxy.toString());
		try{
			String strUrl = snippetTask.getFullUrl();
			URL url = new URL(strUrl);
			//using proxy
			conn = (HttpURLConnection)url.openConnection(proxy);
			conn.setConnectTimeout(30000);
			conn.addRequestProperty("Host","search.ukr.net");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			//conn.addRequestProperty("Accept-Encoding","gzip, deflate");
			HtmlCleaner cleaner = new HtmlCleaner();
			is = conn.getInputStream();
			
			//TODO working with gzip encoding
			/*GZIPInputStream gzip = new GZIPInputStream(conn.getInputStream());
			Reader  reader = new InputStreamReader(gzip, "UTF-8");
			    int value = -1;
			    String pageStr = "";

			    while ((value = reader.read()) != -1) {
			        char c = (char) value;
			        pageStr += c;
			    }
			    gzip.close();
			
			    System.out.println(pageStr);
			String encoding = conn.getContentEncoding();*/
			
			/*org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", strUrl);
			System.out.println(page);*/
			
			TagNode html = cleaner.clean(is,"UTF-8");
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

	private ArrayList<Snippet> extractSnippets(SnippetTask snippetTask) throws MalformedURLException, IOException, XPathExpressionException{
		ArrayList<Snippet> snippets = new ArrayList<Snippet>();

		String proxyTypeStr = ConfigManager.getInstance().getProperty("proxy_type");

		TagNode page = getPageContent(snippetTask,proxyFactory.getRandomProxyConnector().getConnect(proxyTypeStr));

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

	private void saveResultToFile(String content, String fileName){
		BufferedWriter bufferedWriter = null;
		//save success tasks
		try {
			//Construct the BufferedWriter object
			log.debug("Starting saving success results...");
			bufferedWriter = new BufferedWriter(bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File("output/"+fileName)), "UTF8")));
			bufferedWriter.write(content);
			log.debug("Success results was saved successfully.");
		} catch (FileNotFoundException ex) {
			log.error("Error occured during saving Success results",ex);
		} catch (IOException ex) {
			log.error("Error occured during saving Success results",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error occured during closing output streams during saving Success results",ex);
			}
		}
	}

	@Override
	public void run()
	{
		synchronized (this)
		{
			try {
				//getting source for snippets
				String source = ConfigManager.getInstance().getProperty(SOURCE_LABEL);
				SnippetTask task = null;
				if("google".equals(source.toLowerCase().trim())){
					task = new GoogleSnippetTask(args[0]);
				}
				if("bing".equals(source.toLowerCase().trim())){
					task = new BingSnippetTask(args[0]);
				}
				if("tut".equals(source.toLowerCase().trim())){
					task = new TutSnippetTask(args[0]);
				}
				if("ukrnet".equals(source.toLowerCase().trim())){
					task = new UkrnetSnippetTask(args[0]);
				}

				task.setLanguage(args[1]);
				String generatedContent = getFixedSnippets(task);
				//save content
				//System.out.println(generatedContent);
				if(generatedContent != null && !"".equals(generatedContent)){
					saveResultToFile(generatedContent,task.getKeyWords().replace('+', ' '));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
