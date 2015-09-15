package com.fdt.snippetutils;

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
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;

public class PageForUrlChecker {

	private static final Logger log = Logger.getLogger(PageForUrlChecker.class);

	private final String SRT_LINK_EXTRACTOR_PATTERN = "<a\\shref=\"(.*)\">(.*)</a>(.*)";
	private final Pattern EXTRACTOR_PATTERN = Pattern.compile(SRT_LINK_EXTRACTOR_PATTERN);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";

	private final static String SITE_LIST_FILE_PATH_LABEL = "site_list_file_path";

	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String PROXY_TYPE_LABEL = "proxy_type";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private final static String IS_LINK_WRAPPED_LABEL = "is_link_wrapped";

	private final static String STRING_FOR_CHECK_LABEL = "string_for_check";

	private String pageListFilePath;

	private ProxyFactory proxyFactory;

	private PageCheckerSaverThread saverThread = new PageCheckerSaverThread();

	private AtomicInteger currentThreadCount = new AtomicInteger(0);
	private Integer maxThreadCount = 1;
	private Boolean isLinkWrapped = true;

	private String stringForCheck;

	private Long sleepTime = 50L;


	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_tinyurl.xml");
		try{
			if(args.length < 1){
				System.out.println("Some arguments are absent. Please use next list of arguments: 1 config file");
				System.exit(-1);
			}else{
				System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
				ConfigManager.getInstance().loadProperties(args[0]);
				System.out.println(args[0]);
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(
								ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL),
								ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
								);
					}
				});

				PageForUrlChecker replacer = new PageForUrlChecker();
				replacer.execute();
			}

		}catch(Exception e){
			log.error("Error occured during replacer executor: ", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public PageForUrlChecker() throws IOException {
		super();
		this.pageListFilePath = ConfigManager.getInstance().getProperty(SITE_LIST_FILE_PATH_LABEL);

		ProxyFactory.DELAY_FOR_PROXY = Integer.valueOf(ConfigManager.getInstance().getProperty(PROXY_DELAY_LABEL));
		ProxyFactory.PROXY_TYPE = ConfigManager.getInstance().getProperty(PROXY_TYPE_LABEL);
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL));

		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));

		this.isLinkWrapped = Boolean.valueOf(ConfigManager.getInstance().getProperty(IS_LINK_WRAPPED_LABEL));

		this.stringForCheck = ConfigManager.getInstance().getProperty(STRING_FOR_CHECK_LABEL);
	}

	private void execute() throws IOException, InterruptedException{

		ArrayList<String> pages= readFile(this.pageListFilePath);

		while(pages.size() > 0)
		{
			if(currentThreadCount.get() < maxThreadCount){
				CheckerThread rplcrThrd = new CheckerThread(pages.remove(0), stringForCheck, this, proxyFactory, isLinkWrapped);
				rplcrThrd.start();
			}else{
				Thread.sleep(1L);
			}
		}

		while( currentThreadCount.get() > 0){
			Thread.sleep(sleepTime);
		}

		saverThread.saveResults();
	}

	public class CheckerThread extends Thread
	{
		private final String site;
		private String stringToCheck;
		private PageForUrlChecker checker;
		private ProxyFactory proxyFactory;
		private boolean isLinkWrapped;

		public CheckerThread(String site, String stringToCheck, PageForUrlChecker checker, ProxyFactory proxyFactory, boolean isLinkWrapped) {
			super();
			this.site = site;
			this.stringToCheck = stringToCheck;
			this.checker = checker;
			this.proxyFactory = proxyFactory;
			this.isLinkWrapped = isLinkWrapped;
		}

		public void start(){
			this.checker.incThrdCnt();
			super.start();
		}

		public void run()
		{
			ProxyConnector proxyConnector = null;
			CheckedResult result = new CheckedResult(site);
			int repeatCount = 0;

			try{
				boolean isErrorExist = false;

				do{
					repeatCount++;
					isErrorExist = false;
					proxyConnector = proxyFactory.getRandomProxyConnector();

					String proxyTypeStr = ConfigManager.getInstance().getProperty("proxy_type");
					Proxy proxy = null;

					String cleanLink = site;

					if(isLinkWrapped){
						cleanLink = extractLink(site);
					}

					try{
						log.trace("Starting checking url");
						proxy = proxyConnector.getConnect(proxyTypeStr);
						result.setValid(getUploadUrl(cleanLink, stringToCheck, proxy));
					}
					catch(Exception e){
						if(e instanceof FileNotFoundException || (e instanceof IOException && !(e instanceof SocketException))){
							repeatCount = 6;
						}
						isErrorExist = true;
						log.warn("Error occured during checking URL: " + site, e);
					}
					finally{
						if(proxyConnector != null){
							proxyFactory.releaseProxy(proxyConnector);
							proxyConnector = null;
						}
					}
				}
				while(isErrorExist && repeatCount <= 5);

				if(isErrorExist){
					result.setValid(false);
				}
			}finally{
				saverThread.addResult(result);
				checker.decThrdCnt();
			}
		}
	}

	private String extractLink(String wrappedLink){
		Matcher matcher = EXTRACTOR_PATTERN.matcher(wrappedLink);
		if(matcher.find() && matcher.group(1) != null && !"".equals(matcher.group(1).trim())){
			return matcher.group(1).trim();
		}else{
			return wrappedLink;
		}
	}

	private boolean getUploadUrl(String site, String urlToFind, Proxy proxy) throws Exception {
		HttpURLConnection conn = null;
		try{
			//post news
			URL url = new URL(site);
			HttpURLConnection.setFollowRedirects(true);
			conn = (HttpURLConnection) url.openConnection(proxy);
			log.trace("Connection created.");
			conn.setReadTimeout(120000);
			conn.setConnectTimeout(120000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9,fr;q=0.5,de;q=0.5,es;q=0.5,it;q=0.5,nl;q=0.5,ru;q=0.3");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Host", site);

			log.trace("Try to get responce as string...");
			StringBuffer responseStr = getResponseAsString(conn);
			log.trace("Responce extracted.");
			//log.trace(responseStr);
			boolean result = responseStr.toString().indexOf(urlToFind) > 0;

			return result;
		}catch(Exception e){
			throw e;
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}
	}

	private StringBuffer getResponseAsString(HttpURLConnection conn)
			throws IOException {
		InputStream is = conn.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuffer responseStr = new StringBuffer();
		while ((line = br.readLine()) != null) {
			responseStr.append(line);
		}
		br.close();
		is.close();
		return responseStr;
	}

	private class PageCheckerSaverThread {

		private final Integer BUFFER_SIZE = 4096;

		private ArrayList<CheckedResult> results = new ArrayList<CheckedResult>();

		protected PageCheckerSaverThread(){
			super();
		}

		public void addResult(CheckedResult result){
			synchronized (results) {
				results.add(result);
			}
			if(results.size() >= BUFFER_SIZE){
				saveResults();
			}
		}

		private void saveResults() {

			StringBuffer validLines = new StringBuffer();
			StringBuffer invalidLines = new StringBuffer();
			ArrayList<CheckedResult> buffer = null;

			synchronized (results) {
				buffer = new ArrayList<>(results);
				results.clear();
			}

			for(CheckedResult checkResult : buffer){
				if(checkResult.isValid()){
					validLines.append(checkResult.getSite()).append("\r\n");
				}else{
					invalidLines.append(checkResult.getSite()).append("\r\n");
				}
			}

			try {
				log.debug("Saving to success list..");
				appendLinesToFile(validLines.toString(), new File("./_valid_sites.txt"), true);
				log.debug("Saving to success list..");
				appendLinesToFile(invalidLines.toString(), new File("./_invalid_sites.txt"), true);
			}
			catch (Exception e) {
				log.error("Error occured during getting checking results" , e);
			}
		}
	}

	private class CheckedResult{
		private String site;
		private boolean valid;

		public CheckedResult(String site) {
			super();
			this.site = site;
		}

		public boolean isValid() {
			return valid;
		}

		public void setValid(boolean valid) {
			this.valid = valid;
		}

		public String getSite() {
			return site;
		}
	}

	private ArrayList<String> readFile(String filePath) throws IOException{

		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<String> fileTitleList = new ArrayList<String>();

		try {
			fr = new FileReader(new File(filePath));
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				if(!"".equals(line.trim())){
					fileTitleList.add(line.trim());
				}
				line = br.readLine();
			}

			//fileTitleArray = fileTitleList.toArray(new String[fileTitleList.size()]);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
			}
			try {
				if(fr != null)
					fr.close();
			} catch (Throwable e) {
			}
		}

		return fileTitleList;
	} 

	private synchronized void appendLinesToFile(String line, File file, boolean append) {
		ArrayList<String> lines = new ArrayList<>();
		lines.add(line);
		appendLinesToFile(lines, file, append);
	}

	private  void appendLinesToFile(ArrayList<String> lines, File file, boolean append) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file,append), "UTF8"));
			for(String line: lines){
				bufferedWriter.append(line);
			}
		} catch (IOException e) {
			log.warn(String.format("Error occured during saving collection string to file %s", file.getName()));
		} finally {
			//Close the BufferedWriter
			if (bufferedWriter != null) {
				try {
					bufferedWriter.flush();
					bufferedWriter.close();
				} catch (IOException e) {
					log.warn(String.format("Error occured closing file %s", file.getName()));
				}
			}
		}
	}

	private void incThrdCnt(){
		currentThreadCount.incrementAndGet();
		log.debug("Current thread count: " + currentThreadCount);
	}

	private void decThrdCnt(){
		currentThreadCount.decrementAndGet();
		log.debug("Current thread count: " + currentThreadCount);
	}
}

