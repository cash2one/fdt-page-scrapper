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

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fdt.scrapper.task.Task;

/**
 *
 * @author Administrator
 */
public class PageScrapper {
	private static final Logger log = Logger.getLogger(PageScrapper.class);

	Task task;
	Proxy proxy = null;

	public PageScrapper(Task task, Proxy proxy) {
		this.task = task;
		this.proxy = proxy;
	}

	public ArrayList<String> extractResult() throws MalformedURLException, IOException, XPathExpressionException, ParserConfigurationException, SAXException {
		if(task.isXmlParce()){
			return parseXml();
		}
		else{
			return parseHtml();
		}
	}

	private org.jsoup.nodes.Document getUrlContentHttp() throws MalformedURLException, IOException {
		URL url = new URL(task.getUrlToScrap());
		HttpURLConnection conn = null;
		org.jsoup.nodes.Document page = null;
		try{
			conn = (HttpURLConnection)url.openConnection(proxy);
			HttpURLConnection.setFollowRedirects(true);
			conn.setConnectTimeout(30000);
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			//conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			//conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			conn.addRequestProperty("Accept-Language","en-US,en;q=0.9,fr;q=0.5,de;q=0.5,es;q=0.5,it;q=0.5,ru;q=0.3");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			int code = conn.getResponseCode();

			InputStream is = conn.getInputStream();
			page = Jsoup.parse(conn.getInputStream(), "UTF-8", task.getUrlToScrap());
			is.close();
		}finally{
			if(conn != null)
				conn.disconnect();
		}

		return page;
	}
	
	private org.jsoup.nodes.Document getUrlContentHttps() throws MalformedURLException, IOException {
		URL url = new URL(task.getUrlToScrap());
		HttpsURLConnection conn = null;
		org.jsoup.nodes.Document page = null;
		try{
			conn = (HttpsURLConnection)url.openConnection(proxy);
			HttpURLConnection.setFollowRedirects(true);
			conn.setConnectTimeout(30000);
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			//conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			//conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			conn.addRequestProperty("Accept-Language","en-US,en;q=0.9,fr;q=0.5,de;q=0.5,es;q=0.5,it;q=0.5,ru;q=0.3");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			int code = conn.getResponseCode();

			InputStream is = conn.getInputStream();
			page = Jsoup.parse(conn.getInputStream(), "UTF-8", task.getUrlToScrap());
			is.close();
		}finally{
			if(conn != null)
				conn.disconnect();
		}

		return page;
	}

	private ArrayList<String> parseHtml() throws MalformedURLException, IOException{
		org.jsoup.nodes.Document page;
		if(task.getUrlToScrap().startsWith("https")){
			page = getUrlContentHttps();
		}else{
			page = getUrlContentHttp();
		}

		ArrayList<String> result = new ArrayList<String>();

		for(int i = 0; i < task.getResultCount(); i++){
			Elements elements = page.select(task.getxPath(i));
			if(elements.isEmpty()){
				result.add("");
			}
			else{
				result.add(elements.text());
			}
		}

		return result;
	}

	private ArrayList<String> parseXml() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		HttpURLConnection conn = null;
		ArrayList<String> resultArray = new ArrayList<String>();

		try{

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			URL url = new URL(task.getUrlToScrap());
			conn =  (HttpURLConnection)url.openConnection(proxy);

			conn.setConnectTimeout(30000);
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			//conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
			//conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			conn.addRequestProperty("Accept-Language","en-US,en;q=0.9,fr;q=0.5,de;q=0.5,es;q=0.5,it;q=0.5,ru;q=0.3");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			InputStream is = conn.getInputStream();

			Document dom = db.parse(is);
			is.close();
			conn.disconnect();
			XPath xpathInst = XPathFactory.newInstance().newXPath();
			// XPath Query for showing all nodes value

			//System.out.println(page.toString());
			for(int i = 0; i < task.getResultCount(); i++){

				XPathExpression expr = xpathInst.compile(task.getxPath(i));

				Object result = expr.evaluate(dom, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				if(nodes.getLength() > 0){
					resultArray.add(nodes.item(0).getNodeValue());
				}else{
					resultArray.add("");
				}
			}
		}finally{
			if(conn != null)
				conn.disconnect();
		}

		return resultArray;
	}
}
