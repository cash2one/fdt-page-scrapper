/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.GoogleSnippetTask;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTask;

/**
 *
 * @author Administrator
 */
public class SnippetGenerator {
    private static final Logger log = Logger.getLogger(SnippetGenerator.class);

    private int MIN_SNIPPET_COUNT=3;
    private int MAX_SNIPPET_COUNT=5;

    private int MIN_LINK_COUNT=3;
    private int MAX_LINK_COUNT=5;

    private int MIN_WORDS_COUNT=2;
    private int MAX_WORDS_COUNT=3;

    private int LINKS_COUNT = 100;

    Random rnd = new Random();

    private ProxyFactory proxyFactory = null;
    private ArrayList<String> linkList = null;
    /**
     * args[0] - keyword
     * args[1] - language
     * args[2] - path to link file
     * args[3] - path to proxy list file
     */
    public static void main(String[] args){
	if(args.length < 4){
	    System.out.print("Not enought arguments....");
	}
	try {
	    SnippetGenerator generator = new SnippetGenerator(args[3], args[2]);
	    SnippetTask task = new GoogleSnippetTask(args[0]);
	    task.setLanguage(args[1]);
	    String generatedContent = generator.getFixedSnippets(task);
	    //TODO save content
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public SnippetGenerator(String pathToLinksFile, String pathToProxyListFile) {
	super();
	proxyFactory = ProxyFactory.getInstance();
	proxyFactory.loadProxyList(pathToProxyListFile);
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

    public String getFixedSnippets(SnippetTask snippetTask) throws Exception {
	//get snippets
	ArrayList<Snippet> snippets = extractSnippets(snippetTask);
	if(snippets == null || snippets.size() == 0){
	    throw new Exception("Snippets size is 0. Will try to use another proxy server");
	}
	String snippetContent = getSnippetsContent(snippets);
	//post news
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
	for(int i = 0; i < snipCount; i++){
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

    private Document getUrlContent(SnippetTask snippetTask, Proxy proxy) throws MalformedURLException, IOException {
	HttpURLConnection conn = null;
	InputStream is = null;
	try{
	    String strUrl = snippetTask.getFullUrl();
	    URL url = new URL(strUrl);
	    //using proxy
	    conn = (HttpURLConnection)url.openConnection(proxy);
	    Tidy tidy = new Tidy();
	    tidy.setQuiet(true);
	    tidy.setShowWarnings(false);
	    is = conn.getInputStream();
	    Document doc = tidy.parseDOM(is, null);
	    //don't using proxy
	    //conn = (HttpURLConnection)url.openConnection();
	    //conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
	    //conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
	    conn.disconnect();
	    return doc;
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
	Document page = getUrlContent(snippetTask,proxyFactory.getRandomProxyConnector().getConnect());

	XPathFactory xpf = XPathFactory.newInstance();
	XPath xpath = xpf.newXPath();

	//get root of snippets
	NodeList tableOfSnippets = (NodeList) xpath.evaluate(snippetTask.getXpathSnipper(), page, XPathConstants.NODESET);

	//search titles
	NodeList titleNodes= (NodeList) xpath.evaluate(snippetTask.getXpathSnipper(), tableOfSnippets, XPathConstants.NODESET);
	//search snippets
	NodeList snippetsNodes= (NodeList) xpath.evaluate(snippetTask.getXpathSnipper(), tableOfSnippets, XPathConstants.NODESET);

	for(int i = 0; i < titleNodes.getLength(); i++){
	    String h3Value = titleNodes.item(i).getNodeValue();
	    String pValue = snippetsNodes.item(i).getNodeValue();
	    if(h3Value != null && !"".equals(h3Value.trim()) && pValue != null && !"".equals(pValue.trim())){
		snippets.add(new Snippet(h3Value, pValue));
	    }
	}
	return snippets;
    }
}
