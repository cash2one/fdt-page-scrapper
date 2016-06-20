/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.SnippetTaskWrapper;
import com.fdt.utils.Utils;

/**
 *
 * @author Administrator
 */
public class SnippetExtractor {

	public static final String SITE_REGEXP_REMOVER = "((http|https|ftp|mailto)://)?([a-zA-Z0-9][a-zA-Z0-9\\-]*\\.)+(com|net|org|ru|by|info|biz|name|mobi|bz|cn|vn|tw|in|mn|cc|ws|bz|ru|su)";

	private static final Logger log = Logger.getLogger(SnippetExtractor.class);

	private static final String MAX_LINK_COUNT_LABEL = "MAX_LINK_COUNT";
	private static final String MIN_LINK_COUNT_LABEL = "MIN_LINK_COUNT";

	private static final String MAX_SNIPPET_COUNT_LABEL = "MAX_SNIPPET_COUNT";
	private static final String MIN_SNIPPET_COUNT_LABEL = "MIN_SNIPPET_COUNT";

	private static final String MAX_ATTEMPT_COUNT_LABEL = "max_attempt_count";

	private Integer MIN_SNIPPET_COUNT=3;
	private Integer MAX_SNIPPET_COUNT=9;

	private Integer MIN_LINK_COUNT=3;
	private Integer MAX_LINK_COUNT=9;

	private Integer MIN_WORDS_COUNT=2;
	private Integer MAX_WORDS_COUNT=5;

	private int LINKS_COUNT = 100;

	private int MAX_EXTRA_SNIPPETS = 3;

	private static final String LINE_FEED = "\r\n";

	private final Random rnd = new Random();

	private ProxyFactory proxyFactory = null;
	private ArrayList<String> linkList = null;

	private SnippetTaskWrapper task= null;

	private boolean addLinkFromFolder = true;

	public SnippetExtractor(ProxyFactory proxyFactory) throws MalformedURLException, IOException {	
		this();
		this.proxyFactory = proxyFactory;
		this.linkList = null;
		this.task = null;
	}

	public SnippetExtractor(SnippetTask snippetTask, ProxyFactory proxyFactory) throws MalformedURLException, IOException {	
		this();
		this.proxyFactory = proxyFactory;
		this.task = new SnippetTaskWrapper(snippetTask);
		this.linkList = null;
	}

	public SnippetExtractor(SnippetTask snippetTask, ProxyFactory proxyFactory, ArrayList<String> linkList) throws MalformedURLException, IOException {	
		this(new SnippetTaskWrapper(snippetTask), proxyFactory, linkList);
	}


	public SnippetExtractor(SnippetTaskWrapper snippetTask, ProxyFactory proxyFactory, ArrayList<String> linkList) throws MalformedURLException, IOException {
		this();

		this.proxyFactory = proxyFactory;
		this.linkList = linkList;
		this.task = snippetTask;
	}
	
	public SnippetExtractor(SnippetTaskWrapper snippetTask, ProxyFactory proxyFactory, ArrayList<String> linkList, int minSnipCnt, int maxSnipCnt, int minLinkCnt, int maxLinkCnt) throws MalformedURLException, IOException {
		this();

		this.proxyFactory = proxyFactory;
		this.linkList = linkList;
		this.task = snippetTask;
		
		MIN_LINK_COUNT = minLinkCnt;
		MAX_LINK_COUNT = maxLinkCnt;
		
		MIN_SNIPPET_COUNT = minSnipCnt;
		MAX_SNIPPET_COUNT = maxSnipCnt;
	}

	public SnippetExtractor() throws MalformedURLException, IOException {
		super();
		if(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL) != null)
			MIN_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_SNIPPET_COUNT_LABEL));
		if(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL) != null)
			MAX_SNIPPET_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_SNIPPET_COUNT_LABEL));
		if(ConfigManager.getInstance().getProperty(MIN_LINK_COUNT_LABEL) != null)
			MIN_LINK_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MIN_LINK_COUNT_LABEL));
		if(ConfigManager.getInstance().getProperty(MAX_LINK_COUNT_LABEL) != null)
			MAX_LINK_COUNT = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_LINK_COUNT_LABEL));
	}

	public boolean isInsLnkFrmGenFile() {
		return addLinkFromFolder;
	}

	public void setAddLinkFromFolder(boolean addLinkFromFolder) {
		this.addLinkFromFolder = addLinkFromFolder;
	}

	public synchronized void insertLinksToSnippets(SnippetTaskWrapper snippetTask){
		insertLinksToSnippets(snippetTask, true);
	}

	public synchronized void insertLinksToSnippets(SnippetTaskWrapper snippetTask, boolean isAddLinks) {
		//get snippets
		String snippetContent = null;
		int attempt = 0;
		int maxAttemptCount = 10;
		boolean errExist = false;

		String propValue = ConfigManager.getInstance().getProperty(MAX_ATTEMPT_COUNT_LABEL);
		if(propValue != null && !"".equals(propValue.trim())){
			maxAttemptCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_ATTEMPT_COUNT_LABEL));
		}
		HashSet<Snippet> snippetResult = new HashSet<Snippet>();
		ProxyConnector proxyConnector = null;

		do{
			//set random page from 25 to 50
			snippetTask.selectRandTask().setPage(25 + rnd.nextInt(26));
			errExist = false;

			if(proxyConnector == null){
				proxyConnector = proxyFactory.getRandomProxyConnector();
				log.debug(String.format("Get proxy: %s", proxyConnector.toString()));
			}

			try{
				//TODO Get proxy connector
				while(snippetResult.size() < MIN_SNIPPET_COUNT && snippetTask.getCurrentTask().getPage() > 1)
				{
					try 
					{
						//Если не было ошибки - то уменьшаем количество страниц, 
						if(!errExist) {
							snippetTask.getCurrentTask().setPage(reducePage(snippetTask.getCurrentTask().getPage(), 3));
						}
						//иначе не уменьшаем, и пробуем с другим прокси
						else {
							errExist = false;
							//change proxy to another
							if(proxyConnector != null) {
								proxyFactory.releaseProxy(proxyConnector);
								proxyConnector = proxyFactory.getRandomProxyConnector();
								log.debug(String.format("Get another proxy: %s", proxyConnector.toString()));
							}
						}

						SnippetExtractor snippetExtractor = new SnippetExtractor(snippetTask, proxyFactory, new ArrayList<String>());
						snippetResult.addAll(snippetExtractor.extractSnippetsFromPageContent(proxyConnector));
					}
					catch (Exception e) {
						errExist = true;
						log.error("Error occured during processing key: " + snippetTask.getCurrentTask().getKeyWords(), e);
					}
				}

				attempt++;
			}
			finally
			{
				if(proxyConnector != null){
					proxyFactory.releaseProxy(proxyConnector);
					proxyConnector = null;
				}
			}
		}
		while(snippetResult.size() < MIN_SNIPPET_COUNT && attempt < maxAttemptCount);


		if(isAddLinks){
			if(addLinkFromFolder)
			{
				//добавлеям в сниппеты все линки которые были переданы классу
				snippetContent = getSnippetsContentFromFolder(new ArrayList<Snippet>(snippetResult));
			}else{
				//берём рандомногое количество линков из тех, что бы ли переданные классу
				snippetContent = getSnippetsContent(new ArrayList<Snippet>(snippetResult));
			}
		}else{
			snippetContent = getSnippetsContentWOLinks(new ArrayList<Snippet>(snippetResult));
		}

		task.getCurrentTask().setResult(snippetContent);
	}
	
	private String getSnippetsContentWOLinks(ArrayList<Snippet> snippets){
		StringBuffer snipContent = new StringBuffer();
		for(Snippet snippet : snippets){
			snipContent.append(snippet.toString()).append("\r\n");
		}
		
		return snipContent.toString();
	}

	private int reducePage(int currentPage){
		if(currentPage/5 > 1){
			log.info(String.format("Recude page from %d to %d",currentPage, currentPage/5 ));
			return currentPage/5;
		}else{
			log.info(String.format("Recude page from %d to %d",currentPage, 1));
			return 1;
		}
	}

	private int reducePage(int currentPage, int divider){
		if(currentPage/divider > 1){
			log.info(String.format("Recude page from %d to %d",currentPage, currentPage/divider ));
			return currentPage/divider;
		}else{
			log.info(String.format("Recude page from %d to %d",currentPage, 1));
			return 1;
		}
	}

	/**
	 * Добавляем рандомно линки, которые были переданые в обработчки. 
	 * Будет взято случайное количество линков (не все) и они будут добавлены в сниппеты
	 * 
	 * @param snippets
	 * @return
	 */
	private String getSnippetsContent(ArrayList<Snippet> snippets) {
		//calculate snippets count
		int snipCount = 0;
		int linkCount = 0;

		snipCount = getSnipCount(snippets);

		log.debug(String.format("Keywords: '%s' Snippet count: %d", task.getCurrentTask().getKeyWords(), snipCount));
		StringBuffer snippetsContent = new StringBuffer();

		//получаем случайное количество линков, которые будут добавлены
		linkCount = getLinkCount();
		int snippetLinked = 0;

		if((snippets.size()-snipCount) < 0){
			log.debug("test");
		}
		int indexShift = getRandomValue(0,snippets.size()-snipCount); 

		log.debug(String.format("Snippets.size() = (%d); linkList.size() = (%d), snipCount = (%d)", snippets.size(), linkList.size(), snipCount));

		for(int i = indexShift; i < (snipCount+indexShift); i++){
			//add link to snipper
			if(snippetLinked < linkCount){
				//add snippet link
				int randomSuccessLink = getRandomValue(1,linkList.size()-1);
				Utils.addLinkToSnippetContent(snippets.get(i), linkList.get(randomSuccessLink),MIN_WORDS_COUNT, MAX_WORDS_COUNT);
				snippetsContent.append(snippets.get(i).toString()).append("\r\n");
				snippetLinked++;
			}else{
				snippetsContent.append(snippets.get(i).toString()).append("\r\n");
			}
		}

		return snippetsContent.toString();
	}

	private int getLinkCount() {
		int linkCount;
		int randomValue = getRandomValue(MIN_LINK_COUNT, MAX_LINK_COUNT);
		if(randomValue > LINKS_COUNT){
			linkCount = LINKS_COUNT;
		}else{
			linkCount = randomValue;
		}
		return linkCount;
	}

	/**
	 * Calculate snippet count for extract
	 * 
	 * @param snippets
	 * @return
	 */
	private int getSnipCount(ArrayList<Snippet> snippets) {
		int snipCount;
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
		return snipCount;
	}

	/**
	 * добавлеям в сниппеты все (по возможности) линки которые были переданы классу
	 * Линки должны были быть взяты из рандомного файла
	 * 
	 * @param snippets
	 * @return
	 */

	private String getSnippetsContentFromFolder(ArrayList<Snippet> snippets) 
	{
		int snipCount = 0;
		int linkCount = linkList.size();
		//calculate snippets count
		/*if(getRandomValue(MIN_LINK_COUNT, MAX_LINK_COUNT) == 0){
			linkCount = 0;
		}*/

		if( MIN_LINK_COUNT == 0 && MAX_LINK_COUNT == 0){
			linkCount = linkList.size();
		}else{
			linkCount = getRandomValue(MIN_LINK_COUNT, MAX_LINK_COUNT);
		}
		
		//reduce link count
		while(linkList.size() > linkCount){
			linkList.remove(rnd.nextInt(linkList.size()));
		}

		if(snippets.size() < linkCount){
			log.warn(String.format("Link size (%s) is greater than snippets size (%s)", linkCount, snippets.size()));
			//reducing 
			return null;
		}else{
			//if(linkCount > 0){
			/*snipCount = linkCount + rnd.nextInt(MAX_EXTRA_SNIPPETS+1);*/
			snipCount = getRandomValue(MIN_SNIPPET_COUNT, MAX_SNIPPET_COUNT);

			if(snippets.size() < snipCount){
				snipCount = snippets.size();
			}
			/*}else{
				snipCount = getSnipCount(snippets);
			}*/
		}

		log.debug(String.format("Snippets.size() = (%d); linkList.size() = (%d), linkCount = %d, snipCount = (%d)", snippets.size(), linkList.size(), linkCount, snipCount));

		StringBuilder snippetsContent = new StringBuilder();

		//Get random snippets to insert
		while(snippets.size() > snipCount){
			snippets.remove(rnd.nextInt(snippets.size()));
		}

		Integer rndValue = -1;
		List<Integer> rndIdxWOLinks = new ArrayList<Integer>();

		for(int i = 0; i < (snipCount-linkCount); i++){
			rndValue = rnd.nextInt(snipCount);
			if(!rndIdxWOLinks.contains(rndValue)){
				rndIdxWOLinks.add(rndValue);
			}else{
				i--;
			}
		}

		int lnkIdx = 0;
		for(int i = 0; i < snipCount; i++){
			if( !rndIdxWOLinks.contains(i) ){
				//add link to snipper
				addLinkFromFolderToSnippetContent(snippets.get(i), linkList.get(lnkIdx++).replaceAll("<br />", ""));
			}
			snippetsContent.append(snippets.get(i).toString()).append("\r\n");
		}

		return snippetsContent.toString();
	}

	private Document loadPageContent(SnippetTask snippetTask, ProxyConnector proxyConnector) throws MalformedURLException, IOException, ParseException, XPathExpressionException {
		HttpURLConnection conn = null;
		InputStream is = null;

		String proxyTypeStr = ConfigManager.getInstance().getProperty("proxy_type");
		Proxy proxy = proxyConnector.getConnect(proxyTypeStr);

		log.debug("Using proxy: " + proxy.toString());
		try{
			String strUrl = snippetTask.getFullUrl();
			URL url = new URL(strUrl);
			log.debug(strUrl);
			//using proxy
			conn = (HttpURLConnection)url.openConnection(proxy);
			conn.setConnectTimeout(30000);
			conn.addRequestProperty("Host",snippetTask.getHost());
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0"); 
			conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			//conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			//conn.addRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.addRequestProperty("Accept-Language","en-US,en;q=0.9,fr;q=0.5,de;q=0.5,es;q=0.5,it;q=0.5");
			conn.addRequestProperty("Accept-Encoding","gzip");
			fillExtraParamsFromTask(conn, snippetTask);
			conn.setDoInput(true);
			conn.setDoOutput(false);

			int respCode = conn.getResponseCode();

			if(respCode != 200){
				log.error(String.format("Responce code not equals 200 for proxy %s", proxy.toString()));
			}

			if(snippetTask.isBanPage(respCode)){
				//TODO Save proxy to banned list
				proxyFactory.addToBannedList(proxyConnector);
			}

			is = conn.getInputStream();

			String encoding = conn.getContentEncoding();

			Document html = null;
			String htmlStr;
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

				htmlStr = pageStr.toString();
			}else{
				htmlStr = getResponseAsString(conn.getInputStream()).toString();
			}
			//int code = conn.getResponseCode();

			if(log.isTraceEnabled()){
				appendLineToFile(htmlStr, new File("./responce.html"));
			}
			html = Jsoup.parse(htmlStr);
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

	private StringBuilder getResponseAsString(InputStream is)
			throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder responseStr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			responseStr.append(line).append(LINE_FEED);
		}
		is.close();
		return responseStr;
	}

	private void fillExtraParamsFromTask(HttpURLConnection connection, SnippetTask task){
		for(String key : task.getExtraParams().keySet()){
			connection.addRequestProperty(key, task.getExtraParams().get(key));
		}
	}

	private HttpURLConnection executeURL(String strUrl, Proxy proxy) throws IOException{
		//String strUrl = snippetTask.getFullUrl();
		URL url = new URL(strUrl);
		log.debug(strUrl);
		//using proxy
		HttpURLConnection conn = (HttpURLConnection)url.openConnection(proxy);
		conn.setConnectTimeout(30000);
		conn.addRequestProperty("Host","www.google.ru");
		conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
		conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
		conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		conn.addRequestProperty("Accept-Language","en-US,en;q=0.9,fr;q=0.5,de;q=0.5,es;q=0.5,it;q=0.5,ru;q=0.3");
		conn.addRequestProperty("Accept-Encoding","gzip");

		@SuppressWarnings("unused")
		int code = conn.getResponseCode();

		return conn;
	}

	private void addLinkFromFolderToSnippetContent(Snippet snippet, String link){
		StringBuffer newContent = new StringBuffer(snippet.getContent());
		//find random
		List<Integer> dotsIdx = getDotsIdxs(newContent.toString());

		if(dotsIdx.size() > 0)
		{
			int idx = dotsIdx.get(rnd.nextInt(dotsIdx.size()));

			if(newContent.charAt(idx) == ','){
				idx += 1;
			}

			newContent.insert(idx, " " + link);

		}else{
			newContent.append(" ").append(link);
		}

		snippet.setContent(newContent.toString());
	}

	private List<Integer> getDotsIdxs(String srt){
		List<Integer> dotsIdx = new ArrayList<Integer>();
		int idx = -1;

		if( (idx = srt.indexOf(".")) != -1){
			dotsIdx.add(idx);
		}

		if( (idx = srt.lastIndexOf(".")) != -1){
			dotsIdx.add(idx);
		}

		if( (idx = srt.indexOf(",")) != -1){
			dotsIdx.add(idx);
		}

		if( (idx = srt.lastIndexOf(",")) != -1){
			dotsIdx.add(idx);
		}

		return dotsIdx;
	}

	public Integer getRandomValue(Integer minValue, Integer maxValue){
		rnd.nextInt();
		return  minValue + rnd.nextInt(maxValue - minValue+1);
	}

	public ArrayList<Snippet> extractSnippetsFromPageContent() throws MalformedURLException, IOException, XPathExpressionException, ParseException{
		ProxyConnector proxyConnector = proxyFactory.getRandomProxyConnector();
		try{
			return extractSnippetsFromPageContent(task.getCurrentTask(), proxyConnector);
		}finally{
			if(proxyConnector != null){
				proxyFactory.releaseProxy(proxyConnector);
				proxyConnector = null;
			}
		}
	}

	public ArrayList<Snippet> extractSnippetsFromPageContent(ProxyConnector proxyConnector) throws MalformedURLException, IOException, XPathExpressionException, ParseException{
		return extractSnippetsFromPageContent(task.getCurrentTask(), proxyConnector);
	}

	public ArrayList<Snippet> extractSnippetsFromPageContent(SnippetTask snippetTask) throws MalformedURLException, IOException, XPathExpressionException, ParseException{
		ProxyConnector proxyConnector = proxyFactory.getRandomProxyConnector();
		try{
			return extractSnippetsFromPageContent(snippetTask, proxyConnector);
		}finally{
			if(proxyConnector != null){
				proxyFactory.releaseProxy(proxyConnector);
				proxyConnector = null;
			}
		}
	}

	public ArrayList<Snippet> extractSnippetsFromPageContent(SnippetTask snippetTask, ProxyConnector proxyConnector) throws MalformedURLException, IOException, XPathExpressionException, ParseException{

		ArrayList<Snippet> snippets = new ArrayList<Snippet>();

		log.debug(String.format("Using %s for getting snippets for key '%s'", snippetTask.getHost(), snippetTask.getKeyWords()));

		Document page = loadPageContent(snippetTask,proxyConnector);

		Elements titles = null;
		Elements descs= null;
		String extraElement = null;

		titles = page.select(snippetTask.getXpathTitle());
		descs = page.select(snippetTask.getXpathDesc());

		if(titles.size() != descs.size()){
			log.error("XPATH IS FAIL!");
		}

		int minLenght = titles.size() > descs.size()?descs.size():titles.size();
		if(titles.size() > 0){
			for(int i = 0; i < minLenght; i++){
				String h3Value = titles.get(i).text().replaceAll("(\\.){2,}", ".").
						replaceAll("…", ".").
						replaceAll(" \\.", ".").
						replaceAll(SITE_REGEXP_REMOVER, "").trim();
				String pValue = descs.get(i).text().replaceAll("(\\.){2,}", ".").
						replaceAll("…", ".").replaceAll(" \\.", ".").
						replaceAll(SITE_REGEXP_REMOVER, "").trim();
				if(h3Value != null && !"".equals(h3Value.trim()) && pValue != null && !"".equals(pValue.trim()))
				{
					snippets.add( new Snippet(h3Value,pValue));
				}else{
					log.warn("Empty TITLE or SNIPPET are empty.");
				}
			}
		}

		titles = null;
		descs = null;

		snippetTask.appendSnipResult(snippets);
		return snippets;
	}

	public String extractResultCount() throws MalformedURLException, IOException, XPathExpressionException, ParseException{

		SnippetTask curTask =  this.task.getCurrentTask();

		log.debug(String.format("Using %s for getting result count for key '%s'", curTask.getHost(), curTask.getKeyWords()));

		ProxyConnector proxyConnector = proxyFactory.getRandomProxyConnector();
		Document page = null;
		try{
			page = loadPageContent(this.task.getCurrentTask(),proxyConnector);
		}finally{
			if(proxyConnector != null){
				proxyFactory.releaseProxy(proxyConnector);
				proxyConnector = null;
			}
		}

		Elements cntResult = null;

		cntResult = page.select(curTask.getXpathRstlCnt());

		if(cntResult.size() == 0){
			log.error("XPATH IS FAIL!");
		}

		return cntResult.text();
	}

	private boolean isProxyBanned(SnippetTask snippetTask, int respCode){
		return snippetTask.isBanPage(respCode);
	}

	public SnippetTask getTask() {
		return task.getCurrentTask();
	}

	public SnippetTaskWrapper extractSnippetsWithInsertedLinks(){
		return extractSnippetsWithInsertedLinks(true);
	}
	
	public SnippetTaskWrapper extractSnippetsWithInsertedLinks(boolean isAddLinks)
	{
		synchronized (this)
		{
			try {
				insertLinksToSnippets(task, isAddLinks);
				return task;
			}
			catch (Exception e) {
				log.error("Error occured during getting snippets",e);
				return null;
			}
		}
	}

	private static void appendLineToFile(String str, File file) throws IOException {
		if(file.exists()){
			file.delete();
		}

		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file,false), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();

		} finally {
			//Close the BufferedWriter
			if (bufferedWriter != null) {
				bufferedWriter.flush();
				bufferedWriter.close();
			}
		}
	}
}
