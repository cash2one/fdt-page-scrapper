package com.fdt.snippetutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;

public class PageForUrlChecker {

	private static final Logger log = Logger.getLogger(PageForUrlChecker.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";

	private final static String SITE_LIST_FILE_PATH_LABEL = "site_list_file_path";

	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String PROXY_TYPE_LABEL = "proxy_type";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private String pageListFilePath;

	private ProxyFactory proxyFactory;

	private PageCheckerSaverThread saverThread = new PageCheckerSaverThread();

	private AtomicInteger currentThreadCount = new AtomicInteger(0);
	private Integer maxThreadCount = 1;
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
	}

	private void execute() throws IOException, InterruptedException{

		ArrayList<String> pages= readFile(this.pageListFilePath);

		ExecutorService extSrv = Executors.newFixedThreadPool(maxThreadCount+1);
		extSrv.submit(saverThread);

		while(pages.size() > 0)
		{
			/*while( currentThreadCount.get() >= maxThreadCount ){
				Thread.sleep(sleepTime);
			}*/

			CheckerThread rplcrThrd = new CheckerThread(pages.remove(0), this, proxyFactory);
			saverThread.addFuture(extSrv.submit(rplcrThrd));
		}

		while( currentThreadCount.get() > 0 ){
			Thread.sleep(sleepTime);
		}

		saverThread.stopped = true;
		extSrv.shutdown();
	}

	private class CheckerThread implements Callable<CheckedResult>
	{
		private String site;
		private PageForUrlChecker checker;
		private ProxyFactory proxyFactory;

		public CheckerThread(String site, PageForUrlChecker checker, ProxyFactory proxyFactory) {
			super();
			this.site = site;
			this.checker = checker;
			this.proxyFactory = proxyFactory;
		}

		@Override
		public CheckedResult call() throws Exception {
			checker.incThrdCnt();

			ProxyConnector proxyConnector = proxyFactory.getRandomProxyConnector();
			CheckedResult result = new CheckedResult(site);

			String proxyTypeStr = ConfigManager.getInstance().getProperty("proxy_type");
			Proxy proxy = proxyConnector.getConnect(proxyTypeStr);

			try{
				log.trace("Starting checking url");
				result.setValid(getUploadUrl(site, "http://tinyurl.com/", proxy));
			}finally{
				if(proxyConnector != null){
					proxyFactory.releaseProxy(proxyConnector);
					proxyConnector = null;
				}
				checker.decThrdCnt();
			}

			log.trace("Return result of THREAD:" + result.isValid());
			return result;
		}
	}

	private boolean getUploadUrl(String site, String urlToFind, Proxy proxy) {
		HttpURLConnection conn = null;
		try{
			//post news
			URL url = new URL(site);
			HttpsURLConnection.setFollowRedirects(false);
			conn = (HttpURLConnection) url.openConnection(proxy);
			log.trace("Connection created.");
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
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
			log.warn("Error occured during checking URL: " + site, e);
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return false;
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

	private class PageCheckerSaverThread implements Runnable{

		private ArrayList<Future<CheckedResult>> incomingList = new ArrayList<Future<CheckedResult>>();
		private ArrayList<Future<CheckedResult>> finishedList = new ArrayList<Future<CheckedResult>>();
		private boolean stopped = false;
		private boolean running = false;

		protected PageCheckerSaverThread(){
			super();
		}

		@Override
		public void run() {
			running = true;
			// TODO Auto-generated method stub
			while(running || !stopped){	
				if(stopped){
					running = false;
				}

				/*synchronized (incomingList) {
					for(Future<CheckedResult> future: incomingList){
						log.trace("Add future to running queue");
						runningList.add(future);
					}
					incomingList.clear();
				}
				 */

				if(incomingList.size() > 0){
					synchronized (incomingList){						
						for(Future<CheckedResult> future: incomingList)
						{
							if(future.isDone())
							{
								finishedList.add(future);
								log.trace("Future is DONE");
								try {
									if(future.get().isValid()){
										log.debug("Saving to success list..");
										appendLinesToFile(future.get().getSite(), new File("./_valid_sites.txt"), true);
									}else{
										log.debug("Saving to success list..");
										appendLinesToFile(future.get().getSite(), new File("./_invalid_sites.txt"), true);}
								} catch (Exception e) {
									log.error("Error occured during getting checking results" , e);
								}
							}
						}

						for(Future<CheckedResult> future : finishedList){
							incomingList.remove(future);
						}
					}
				}else{
					try {
						Thread.sleep(500L);
					} catch (InterruptedException e) {
					}
				}
			}
		}

		public void addFuture(Future<CheckedResult> future){
			synchronized (incomingList) {
				log.trace("Add future to collection");
				incomingList.add(future);
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

	private void appendLinesToFile(String line, File file, boolean append) {
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
				bufferedWriter.newLine();
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
		synchronized (this) {
			currentThreadCount.incrementAndGet();
			log.debug("Current thread count: " + currentThreadCount);
			notifyAll();
		}

	}

	private void decThrdCnt(){
		synchronized (this) {
			currentThreadCount.decrementAndGet();
			log.debug("Current thread count: " + currentThreadCount);
			notifyAll();
		}
	}
}
