package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;

/**
 * @author VarenKoks
 */
public class TinyUrlTaskRunner {
	private static final Logger log = Logger.getLogger(TinyUrlTaskRunner.class);

	private static final String LINE_FEED = "\r\n";

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	public final static String MAIN_URL_LABEL = "main_url";

	private String proxyFilePath;
	private long proxyDelay;

	private String listInputFilePath;
	private String listProcessedFilePath;
	private String errorFilePath;
	
	private String firstFileString = "";
	
	private String downloadUrl = "";

	private Properties config = new Properties();

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";

	private final static String TINYURL_LIST_INPUT_FILE_PATH_LABEL = "tinyurl_list_input_file_path";
	private final static String TINYURL_LIST_PROCESSED_FILE_PATH_LABEL = "tinyurl_list_processed_file_path";
	private final static String TINYURL_ERROR_FILE_PATH_LABEL = "tinyurl_error_file_path";
	
	private final static String TINYURL_DOWNLOAD_URL_LABEL = "tinyurl_tinyurl_download_url";

	public TinyUrlTaskRunner(String cfgFilePath){

		Constants.getInstance().loadProperties(cfgFilePath);

		this.proxyFilePath = Constants.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
		this.proxyDelay = Integer.valueOf(Constants.getInstance().getProperty(PROXY_DELAY_LABEL));

		this.listInputFilePath = Constants.getInstance().getProperty(TINYURL_LIST_INPUT_FILE_PATH_LABEL);
		this.listProcessedFilePath = Constants.getInstance().getProperty(TINYURL_LIST_PROCESSED_FILE_PATH_LABEL);
		this.errorFilePath = Constants.getInstance().getProperty(TINYURL_ERROR_FILE_PATH_LABEL);
		
		this.downloadUrl = Constants.getInstance().getProperty(TINYURL_DOWNLOAD_URL_LABEL);
		

		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						Constants.getInstance().getProperty(PROXY_LOGIN_LABEL), Constants.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
						);
			}
		});
	}

	public static void main(String[] args) {
		try{
			TinyUrlTaskRunner taskRunner = new TinyUrlTaskRunner("config.ini");
			DOMConfigurator.configure("log4j.xml");
			taskRunner.runTinyUrlReplacer();
			System.out.print("Program execution finished");
			System.exit(0);
		}catch(Throwable e){
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}


	public void runTinyUrlReplacer(){
		ProxyFactory.DELAY_FOR_PROXY = proxyDelay; 
		ProxyFactory proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(proxyFilePath);

		File rootInputFiles = new File(listInputFilePath);
		
		String downloadUrl = "";
		
		ProxyConnector proxyConnector = null;

		for(File file : rootInputFiles.listFiles())
		{
			String fileAsStr;
			try {
				fileAsStr = getFileAsString(file);
				
				if( this.firstFileString == null || "".equals(firstFileString.trim()) ){
					throw new Exception("Could not find first file string");
				}
				
				downloadUrl = this.downloadUrl + firstFileString.replaceAll("[\\.\\]\\[\\)\\(,+!#]*", "").trim().replaceAll("\\s", "+");
				
				//TODO Get tinyurl
				proxyConnector = proxyFactory.getProxyConnector();
				String tinyUrl = getTinyUrl(downloadUrl, proxyConnector.getConnect());
				
				fileAsStr = fileAsStr.replaceAll("\\[KEYWORD\\]", tinyUrl);
				
				appendStringToFile(fileAsStr, file);
				
				//Move file to processed folder
				File destFile = new File(listProcessedFilePath + "/" + file.getName());
				if(destFile.exists()){
					destFile.delete();
				}
				FileUtils.moveFile(file, destFile);
			} catch (Exception e) {
				try {
					File destFile = new File(errorFilePath + "/" + file.getName());
					if(destFile.exists()){
						destFile.delete();
					}
					FileUtils.moveFile(file, destFile);
				} catch (IOException e1) {
					log.error(e1);
				}
				log.error("Error during execution: ", e);
			}
			finally{
				if(proxyConnector != null){
					proxyFactory.releaseProxy(proxyConnector);
				}
			}
		}
	}

	public void loadProperties(String cfgFilePath){
		synchronized (this){ 
			InputStream is = null;
			try {
				is = new FileInputStream(new File(cfgFilePath));
				config.load(is);
			} catch (FileNotFoundException e) {
				log.error("Reading PROPERTIES file: FileNotFoundException exception occured: " + e.getMessage());
			} catch (IOException e) {
				log.error("Reading PROPERTIES file: IOException exception occured: " + e.getMessage());
			} finally {
				try {
					is.close();
				} catch (Throwable e) {
					log.warn("Error while initializtion", e);
				}
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
}
