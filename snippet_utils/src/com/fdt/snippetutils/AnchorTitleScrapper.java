package com.fdt.snippetutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.BingSnippetTask;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.GoogleSnippetTask;
import com.fdt.scrapper.task.Snippet;
import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.TutSnippetTask;
import com.fdt.scrapper.task.UkrnetSnippetTask;

public class AnchorTitleScrapper {

	private static final Logger log = Logger.getLogger(AnchorTitleScrapper.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";

	private final static String KEYS_FILE_PATH_LABEL = "keys_file_path";
	private final static String PROCESSED_KEYS_FILE_PATH_LABEL = "processed_keys_file_path";
	private final static String OUTPUT_PATH_LABEL = "output_path";
	private final static String TITLES_FILE_PATH_LABEL = "titles_file_path";

	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String PROXY_TYPE_LABEL = "proxy_type";

	private static final String SOURCE_LABEL = "source";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private final static String MAX_TITLES_COUNT_LABEL = "max_titles_count";

	private final static String PREFIX_FIRST_LABEL = "prefix_first";
	private final static String PREFIX_SECOND_LABEL = "prefix_second";

	private String keysFilePath;
	private String processedKeysFilePath;

	private Integer maxTitlesCount = 500;
	private String outputPath;

	private String titlesFilePath;

	ProxyFactory proxyFactory;

	private String source = "BING";

	private String prefixFirst = "";
	private String prefixSecond = "";

	private AtomicInteger currentThreadCount = new AtomicInteger(0);
	private Integer maxThreadCount = 1;
	private Long sleepTime = 50L;

	private ArrayList<String> processedKeys = new ArrayList<String>();

	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_scrapper.xml");
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

				AnchorTitleScrapper replacer = new AnchorTitleScrapper();
				replacer.execute();
			}

		}catch(Exception e){
			log.error("Error occured during replacer executor: ", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public AnchorTitleScrapper() throws IOException {
		super();
		this.keysFilePath = ConfigManager.getInstance().getProperty(KEYS_FILE_PATH_LABEL);
		this.processedKeysFilePath = ConfigManager.getInstance().getProperty(PROCESSED_KEYS_FILE_PATH_LABEL);
		this.outputPath = ConfigManager.getInstance().getProperty(OUTPUT_PATH_LABEL);
		this.titlesFilePath = ConfigManager.getInstance().getProperty(TITLES_FILE_PATH_LABEL);

		this.prefixFirst = ConfigManager.getInstance().getProperty(PREFIX_FIRST_LABEL);
		this.prefixSecond = ConfigManager.getInstance().getProperty(PREFIX_SECOND_LABEL);

		ProxyFactory.DELAY_FOR_PROXY = Integer.valueOf(ConfigManager.getInstance().getProperty(PROXY_DELAY_LABEL));
		ProxyFactory.PROXY_TYPE = ConfigManager.getInstance().getProperty(PROXY_TYPE_LABEL);
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL));

		this.source = ConfigManager.getInstance().getProperty(SOURCE_LABEL);

		if(source == null || "".equals(source)){
			source = "GOOGLE";
		}

		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));

		this.maxTitlesCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_TITLES_COUNT_LABEL));
	}

	private void execute() throws IOException, InterruptedException{

		ArrayList<String> lines= readFile(this.keysFilePath);
		ArrayList<String> processedLines= readFile(this.processedKeysFilePath);
		ArrayList<String> titles = readTitlesFile(this.titlesFilePath);
		ArrayList<Pattern> titlesPattern = new ArrayList<Pattern>();

		//clean keys from processed
		for(String processedKey : processedLines){
			if(lines.contains(processedKey)){
				lines.remove(processedKey);
			}
		}

		HashMap<String, Pattern> linePttrnMpng = new HashMap<String, Pattern>();
		ArrayList<String> keys = new ArrayList<String>();

		String patternStr = "";
		Pattern pattern = Pattern.compile(patternStr);

		for(String title: titles){
			//patternStr = "(.*)\">(" + title + ")</a>(.*)";
			patternStr = title;
			pattern = Pattern.compile(patternStr);
			titlesPattern.add(pattern);
		}

		for(String line : lines){
			for(Pattern ptrn : titlesPattern){
				if(line.matches(ptrn.toString())){
					linePttrnMpng.put(line, ptrn);
					keys.add(line);
					break;
				}
			}
			if(!keys.contains(line)){
				log.error("NOT Processed line: " + line);
			}
		}

		try{
			processKeys(linePttrnMpng, keys);
		}finally{
			saveBannedProxy(proxyFactory.getBannedProxyList());
		}
	}

	private void processKeys(HashMap<String, Pattern> lines, ArrayList<String> keys) throws IOException, InterruptedException{

		ExecutorService extSrv = Executors.newFixedThreadPool(maxThreadCount);

		for(String key : lines.keySet()){

			while( currentThreadCount.get() >= maxThreadCount ){
				Thread.sleep(sleepTime);
			}

			ScrapperThread rplcrThrdFirst = new ScrapperThread(this, key, lines.get(key));
			extSrv.submit(rplcrThrdFirst);
		}

		while( currentThreadCount.get() > 0 ){
			Thread.sleep(sleepTime);
		}
		extSrv.shutdown();
	}

	private class ScrapperThread implements Runnable{

		private AnchorTitleScrapper replacer;
		private String key;
		private Pattern pattern;

		public ScrapperThread(AnchorTitleScrapper replacer, String key,Pattern pattern) {
			super();
			this.replacer = replacer;
			this.key = key;
			this.pattern = pattern;
		}

		@Override
		public void run() {
			replacer.incThrdCnt();
			HashSet<String> titles = null;

			try{
				titles = processKey(prefixFirst + " " + key, pattern);
				if(!prefixSecond.equals(prefixFirst)){
					titles.addAll(processKey(prefixSecond + " " + key, pattern));
				}
				//Save file
				File fileToSave = new File(outputPath, key + ".txt");
				appendLinesToFile(titles, fileToSave, key, false);
			}catch(Throwable e){
				log.error("Error occured during processing key: " +key, e);
			}finally{
				replacer.decThrdCnt();
			}
		}
	}

	private HashSet<String> processKey(String key, Pattern pattern) throws Exception{
		HashSet<String> titles = new HashSet<String>();

		ArrayList<Snippet> extractedSnippets = new ArrayList<Snippet>();
		ArrayList<String> currentTitles = new ArrayList<String>();

		boolean isNoMorePages = false;
		int page = 1;

		String bookName;

		Matcher matcher = pattern.matcher(key);
		if (matcher.find()){
			//fullTitle = matcher.group(1).trim();
			bookName = matcher.group(1).trim();
			SnippetTask snippetTask = getTaskBySource(source,bookName);

			while(titles.size() < maxTitlesCount && !isNoMorePages){
				try {
					snippetTask.setPage(page);
					extractedSnippets = getSnippet(snippetTask);
					currentTitles = getTitlesList(extractedSnippets);
					isNoMorePages = titles.containsAll(currentTitles);
					if(!isNoMorePages){
						titles.addAll(currentTitles);
					}else{
						log.info(String.format("Page count for key '%s' is: %s", key, page));
					}
					page++;
				} catch (Exception e) {
					log.warn(String.format("Error occured during getting snippets: %s", e.getMessage()), e);
				} 
			}
		}

		return titles;
	}

	private ArrayList<String> getTitlesList(ArrayList<Snippet> snippets){
		ArrayList<String> titles = new ArrayList<String>();

		for(Snippet snippet : snippets){
			titles.add(snippet.getTitle());
		}

		return titles;
	}

	private ArrayList<String> readFile(String filePath) throws IOException{

		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<String> fileTitleList = new ArrayList<String>();

		if(new File(filePath).exists()){
			try {
				br = new BufferedReader( new InputStreamReader( new FileInputStream(filePath), "UTF8" ) );

				String line = br.readLine();
				while(line != null){
					fileTitleList.add(line.trim());
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
		}

		return fileTitleList;
	} 

	private ArrayList<String> readTitlesFile(String filePath) throws IOException{

		ArrayList<String> titles = readFile(this.titlesFilePath);

		for(int i = 0; i < titles.size(); i++){
			titles.set(i, titles.get(i).replaceAll("\\(", "\\\\("));
			titles.set(i, titles.get(i).replaceAll("\\)", "\\\\)"));
			titles.set(i, titles.get(i).replaceAll("\\[Book\\]", "(.*)"));
		}

		return titles;
	}

	private void appendLinesToFile(HashSet<String> hasSet, File file, String key, boolean append) {
		synchronized (processedKeys) {
			processedKeys.add(key);
			if(processedKeys.size() >= 10){
				appendLinesToFile(processedKeys, new File(processedKeysFilePath), true);
				processedKeys.clear();
			}
		}

		ArrayList<String> lines = new ArrayList<>();
		String filteredStr;
		String filteredFromSeparationStr;
		for(String str : hasSet){
			filteredStr = str.replaceAll("[^a-zA-Z0-9\\!\\.\\,\\-\\)\\(\\\\/'\"\\+:;\\[\\]\\#$%\\^&\\*\\?\\s]+","").trim();
			filteredFromSeparationStr = filteredStr.replaceAll("[\\!\\.\\,\\-\\)\\(\\\\/'\"\\+:;\\[\\]\\#$%\\^&\\*\\?\\s]+"," ").trim();
			if(filteredFromSeparationStr.length() > 0)
				lines.add(filteredFromSeparationStr);
			else
				filteredStr = str;
		}
		appendLinesToFile(lines, file, append);

	}

	private static void appendLinesToFile(ArrayList<String> lines, File file, boolean append) {
		if(!append){
			if(file.exists()){
				file.delete();
			}
		}

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

	private ArrayList<Snippet> getSnippet(SnippetTask snippetTask) throws Exception{
		int attempCount = 0;
		SnippetExtractor snippetExtractor = new SnippetExtractor( proxyFactory);
		//TODO Add Snippet task chooser
		ArrayList<Snippet> snippets = snippetExtractor.extractSnippetsFromPageContent(snippetTask);

		while(snippets.size() == 0 && attempCount < 5){
			attempCount++;
			snippets = snippetExtractor.extractSnippetsFromPageContent(snippetTask);
		}

		if(snippets.size() == 0){
			throw new IOException("Could not extract snippets. Snippet extracted size == 0");
		}else{
			return snippets;
		}
	}

	private SnippetTask getTaskBySource(String source, String key) throws Exception{
		SnippetTask task = null;

		if("GOOGLE".equals(source.toUpperCase().trim())){
			task = new GoogleSnippetTask(key);
		} else
			if("BING".equals(source.toUpperCase().trim())){
				task = new BingSnippetTask(key);
			} else
				if("TUT".equals(source.toUpperCase().trim())){
					task = new TutSnippetTask(key);
				} else
					if("UKRNET".equals(source.toUpperCase().trim())){
						task = new UkrnetSnippetTask(key);
					}else{
						throw new Exception("Can't find assosiated task for source: " + source);
					}

		return task;
	}

	private void incThrdCnt(){
		currentThreadCount.incrementAndGet();
		log.debug("Current thread count: " + currentThreadCount);
	}

	private void decThrdCnt(){
		currentThreadCount.decrementAndGet();
		log.debug("Current thread count: " + currentThreadCount);
	}

	private void saveBannedProxy(List<ProxyConnector> prConnectorList){
		BufferedWriter bufferedWriter = null;

		try {
			log.debug("Starting saving unused account...");
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter("proxy_banned.txt",false));
			for(ProxyConnector prConnector : prConnectorList){
				bufferedWriter.write(prConnector.toString());
				bufferedWriter.newLine();
			}
			log.debug("Banned proxies was saved successfully.");

		} catch (FileNotFoundException ex) {
			log.error("Error occured during saving banned proxy",ex);
		} catch (IOException ex) {
			log.error("Error occured during saving banned proxy",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error occured during closing output streams during saving banned proxy",ex);
			}
		}
	}

	//	public boolean getCookies(){
	//		//getting cookie for each account
	//		HttpURLConnection conn = null;
	//		ProxyConnector proxy = null;
	//		
	//		try {
	//			proxy = proxyFactory.getRandomProxyConnector();
	//
	//			executerequestToGetCookies(Constants.getInstance().getProperty(MAIN_URL_LABEL) + "/ru", "GET", proxy, null, account);
	//			executerequestToGetCookies( Constants.getInstance().getProperty(MAIN_URL_LABEL) + "/pageitem/authenticationContainer?request=/login?&from_request=%2Fru&_csrf_l=" + account.getCookie("_csrf/link"), "GET", proxy, null, account);
	//
	//			String postUrl = Constants.getInstance().getProperty(MAIN_URL_LABEL) + Constants.getInstance().getProperty(LOGIN_URL_LABEL);
	//			URL url = new URL(postUrl);
	//			HttpURLConnection.setFollowRedirects(false);
	//			conn = (HttpURLConnection) url.openConnection(proxy.getConnect(ProxyFactory.PROXY_TYPE));
	//			conn.setReadTimeout(60000);
	//			conn.setConnectTimeout(60000);
	//			conn.setRequestMethod("POST");
	//			conn.setDoInput(true);
	//			conn.setDoOutput(true);
	//
	//			conn.addRequestProperty("Referer","http://www.dailymotion.com/ru");
	//			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:18.0) Gecko/20100101 Firefox/18.0"); 
	//			//conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	//			conn.setRequestProperty("Accept", "*/*");
	//			conn.setRequestProperty("X-Requested-With",	"XMLHttpRequest");
	//			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	//
	//			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	//			nameValuePairs.add(new BasicNameValuePair("form_name", "dm_pageitem_authenticationform"));
	//			nameValuePairs.add(new BasicNameValuePair("username", account.getEmail()));
	//			nameValuePairs.add(new BasicNameValuePair("password", account.getPass()));
	//			nameValuePairs.add(new BasicNameValuePair("_csrf", account.getCookie("_csrf/form")));
	//			nameValuePairs.add(new BasicNameValuePair("_fid", ""));
	//			nameValuePairs.add(new BasicNameValuePair("authChoice", "login"));
	//			nameValuePairs.add(new BasicNameValuePair("from_request", "/RedBull"));
	//
	//			OutputStream os = conn.getOutputStream();
	//			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	//			writer.write(getQuery(nameValuePairs));
	//			writer.flush();
	//			writer.close();
	//			os.close();
	//
	//
	//			// Execute HTTP Post Request
	//			Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();
	//			if(cookies.get("Set-Cookie") == null || (cookies.get("Set-Cookie") != null && cookies.get("Set-Cookie").toString().contains("notexists"))){
	//				log.error("Can't getting cookies for account.Account doesn't exist: \""+ account.getLogin() + "\" or banned, or error occured during login. Please check email and password.");
	//				return false;
	//			}
	//
	//			for(String cookieOne: cookies.get("Set-Cookie"))
	//			{
	//				String cookiesValues[] = cookieOne.split(";");
	//				for(String cookiesArrayItem : cookiesValues){
	//					String singleCookei[] = cookiesArrayItem.split("=");
	//					account.addCookie(singleCookei[0].trim(), singleCookei[1].trim());
	//				}
	//			}
	//			
	//			return true;
	//		} catch (Exception e) {
	//			log.error("Error during login/getting cookies for account",e);
	//			return false;
	//		}finally{
	//			if(conn!=null){
	//				conn.disconnect();
	//			}
	//			if(proxy != null){
	//				proxyFactory.releaseProxy(proxy);
	//			}
	//		}
	//	}
}
