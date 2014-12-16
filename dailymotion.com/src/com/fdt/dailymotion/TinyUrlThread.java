package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.fdt.dailymotion.task.TinyUrlTask;
import com.fdt.dailymotion.task.TinyUrlTaskFactory;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;

public class TinyUrlThread extends Thread{

	private static final Logger log = Logger.getLogger(TinyUrlThread.class);
	
	private static final String LINE_FEED = "\r\n";

	private TinyUrlTask task;
	private TinyUrlTaskFactory taskFactory;
	private ProxyFactory proxyFactory;
	private String listProcessedFilePath;
	
	private String firstFileString = "";
	
	private String downloadUrl = "";

	public TinyUrlThread(TinyUrlTask task, TinyUrlTaskFactory taskFactory, ProxyFactory proxyFactory, String listProcessedFilePath) {
		this.task = task;
		this.taskFactory = taskFactory;
		this.proxyFactory = proxyFactory;
		this.listProcessedFilePath = listProcessedFilePath;
	}

	@Override
	public void start(){
		taskFactory.incRunThreadsCount();
		super.start();
	}

	@Override
	public void run() {
		ProxyConnector proxyConnector = null;
		synchronized (this) {
			try{
				boolean errorExist = false;
				try {
					String fileAsStr = getFileAsString(task.getFile());
					
					if( this.firstFileString == null || "".equals(firstFileString.trim()) ){
						throw new Exception("Could not find first file string");
					}
					
					downloadUrl = this.downloadUrl + firstFileString.replaceAll("[\\.\\]\\[\\)\\(,+!#]*", "").trim().replaceAll("\\s", "+");
					
					//TODO Get tinyurl
					proxyConnector = proxyFactory.getProxyConnector();
					String tinyUrl = getTinyUrl(downloadUrl, proxyConnector.getConnect());
					
					fileAsStr = fileAsStr.replaceAll("\\[KEYWORD\\]", tinyUrl);
					
					appendStringToFile(fileAsStr, task.getFile());
					
					//Move file to processed folder
					File destFile = new File(listProcessedFilePath + "/" + task.getFile().getName());
					if(destFile.exists()){
						destFile.delete();
					}
					FileUtils.moveFile(task.getFile(), destFile);
					/*if(task.isResultEmpty()){
						proxyConnector = proxyFactory.getProxyConnector();
						log.debug("Free proxy count: " + (proxyFactory.getFreeProxyCount()-1));
						log.debug("Task (" + task.toString() +") is using proxy connection: " +proxyConnector.getProxyKey());
						Proxy proxy = proxyConnector.getConnect();
						NewsPoster ps;
						ps = new NewsPoster(task, proxy, account, taskFactory, linkList);
						String newsResult = ps.executePostNews();
						task.setResult(newsResult);
					}*/

				}
				catch (Exception e) {
					errorExist = true;
					taskFactory.reprocessingTask(task);
					log.error("Error occured during process task: " + task.toString(), e);
				}finally{
					if(proxyConnector != null){
						proxyFactory.releaseProxy(proxyConnector);
						proxyConnector = null;
					}
				}
				
				if(!errorExist){
					taskFactory.putTaskInSuccessQueue(task);
				}
				
			} finally {
				taskFactory.decRunThreadsCount(task);
			}
		}
	}
	
	private String getFileAsString(File file) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;
		boolean isFirstLineSaved = false;
		
		this.firstFileString = "";
		
		StringBuilder fileAsStr = new StringBuilder();
		
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
				if(!isFirstLineSaved){
					firstFileString = line;
					isFirstLineSaved = true;
				}
				fileAsStr.append(line).append(LINE_FEED);
			}
		}
		finally {
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
		
		return fileAsStr.toString();
	}

	public TinyUrlTask getTask(){
		return task;
	}
	
	private String getTinyUrl(String downloadUrl, Proxy proxy) throws MalformedURLException, IOException, ParseException, XPatherException {
		HttpURLConnection conn = null;
		InputStream is = null;
		System.out.println("Using proxy: " + proxy.toString());
		try{
			String strUrl = "http://tinyurl.com/create.php?source=indexpage&url="+URLEncoder.encode(downloadUrl,"UTF-8")+"&alias=";
			URL url = new URL(strUrl);
			System.out.println(strUrl);
			//using proxy
			conn = (HttpURLConnection)url.openConnection(proxy);
			conn.setConnectTimeout(30000);
			conn.setDoInput(true);
			conn.setDoOutput(false);

			HtmlCleaner cleaner = new HtmlCleaner();

			is = conn.getInputStream();

			String encoding = conn.getContentEncoding();

			InputStream inputStreamPage = null;

			TagNode html = null;
		
			html = cleaner.clean(is,"UTF-8");
			
			String tinyUrl = ((String)html.evaluateXPath("//blockquote/small/a/@href")[0]);
			//int code = conn.getResponseCode();
			return tinyUrl;
		}finally{
			if(conn != null){
				try{conn.disconnect();}catch(Throwable e){}
			}
			if(is != null){
				try{is.close();}catch(Throwable e){}
			}
		}
	}
	
	private void appendStringToFile(String str, File file) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();
		} catch (FileNotFoundException ex) {
			log.error("Error during saving string to file",ex);
		} catch (IOException ex) {
			log.error("Error during saving string to file",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error during closing output stream",ex);
			}
		}
	}
}
