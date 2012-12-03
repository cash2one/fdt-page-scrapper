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

	public String extractResult() throws MalformedURLException, IOException, XPathExpressionException, ParserConfigurationException, SAXException {
		if(task.isXmlParce()){
			return parseXml();
		}
		else{
			return parseHtml();
		}
	}

	private org.jsoup.nodes.Document getUrlContent() throws MalformedURLException, IOException {
		URL url = new URL(task.getUrlToScrap());
		HttpURLConnection conn = (HttpURLConnection)url.openConnection(proxy);
		conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
		conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); 
		InputStream is = conn.getInputStream();
		org.jsoup.nodes.Document page = Jsoup.parse(conn.getInputStream(), "UTF-8", task.getUrlToScrap());
		is.close();
		return page;
	}

	private String parseHtml() throws MalformedURLException, IOException{
		org.jsoup.nodes.Document page = getUrlContent();
		
		Elements elements = page.select(task.getxPath());
		if(elements.isEmpty()){
			return "";
		}
		else{
			return elements.text();
		}
	}

	private String parseXml() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		//Using factory get an instance of document builder
		DocumentBuilder db = dbf.newDocumentBuilder();
		//parse using builder to get DOM representation of the XML file
		URL url = new URL(task.getUrlToScrap());
		HttpURLConnection conn =  (HttpURLConnection)url.openConnection(proxy);
		InputStream is = conn.getInputStream();

		Document dom = db.parse(is);
		is.close();
		XPath xpathInst = XPathFactory.newInstance().newXPath();
		// XPath Query for showing all nodes value
		XPathExpression expr = xpathInst.compile(task.getxPath());

		Object result = expr.evaluate(dom, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		if(nodes.getLength() > 0){
			return nodes.item(0).getNodeValue();
		}else{
			return null;
		}
	}
}
