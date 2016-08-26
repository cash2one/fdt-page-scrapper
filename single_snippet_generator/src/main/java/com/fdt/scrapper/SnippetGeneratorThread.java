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
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;

/**
 *
 * @author Administrator
 */
public class SnippetGeneratorThread implements Runnable {
	private static final String LOAD_PROXY_FILE_FROM_INET_LABEL = "load_proxy_file_from_inet";
	
	private static final String SOURCE_LABEL = "source";

	private static final Logger log = Logger.getLogger(SnippetGeneratorThread.class);

	private String key = null;
	private String lang = null;

	Random rnd = new Random();

	private ProxyFactory proxyFactory = null;
	private ArrayList<String> linkList = null;

	public SnippetGeneratorThread(String key, String lang, String pathToLinksFile, String pathToProxyListFile) throws MalformedURLException, IOException {
		super();
		this.key = key;
		this.lang = lang;
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
				String utf8Line = new String(line.getBytes(),"UTF-8");
				linkList.add(utf8Line.trim());
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

	private void saveResultToFile(String content, String fileName){
		ArrayList<String> contentArray = new ArrayList<String>();
		contentArray.add(content);
		saveResultToFile(contentArray, fileName);
	}

	private void saveResultToFile(ArrayList<String> content, String fileName){
		BufferedWriter bufferedWriter = null;
		//save success tasks
		try {
			//Construct the BufferedWriter object
			log.debug("Starting saving success results...");
			bufferedWriter = new BufferedWriter(bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File("output/"+fileName)), "UTF8")));
			for(String line:content){
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
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
				String source = ConfigManager.getInstance().getProperty(SOURCE_LABEL);
				SnippetExtractor snippetExtractor = new SnippetExtractor(key, lang, proxyFactory, linkList,source);
				String generatedContent = snippetExtractor.extractSnippets().getResult();
				//save content
				//System.out.println(generatedContent);
				if(generatedContent != null && !"".equals(generatedContent)){
					saveResultToFile(generatedContent,snippetExtractor.getTask().getKeyWords().replace('+', ' '));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
