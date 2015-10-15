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
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
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

public class AnchorTitleReplacer {

	private static final Logger log = Logger.getLogger(AnchorTitleReplacer.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";

	private final static String MAX_LINE_COUNT_LABEL = "max_line_count";
	private final static String MIN_LINE_COUNT_LABEL = "min_line_count";

	private final static String IS_CLEAN_KEY_LABEL = "clean_key";

	private final static String ANCHOR_FILE_PATH_LABEL = "anchor_file_path";
	private final static String REPEAT_COUNT_LABEL = "repeat_count";
	private final static String OUTPUT_PATH_LABEL = "output_path";
	private final static String INPUT_TITLES_PATH_LABEL = "input_titles_path";
	private final static String TITLES_FILE_PATH_LABEL = "titles_file_path";

	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String PROXY_TYPE_LABEL = "proxy_type";

	private final static String GET_ANCHOR_FROM_WEB_LABEL = "get_anchor_from_web";

	private static final String SOURCE_LABEL = "source";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private String anchorFilePath;
	private String outputPath;
	private String inputTitlesPath;

	private int maxLineCount = 7;
	private int minLineCount = 3;

	private String patternTitlesFilePath;
	
	private boolean cleanKey = false;

	private int repeatCount;

	ProxyFactory proxyFactory;

	private boolean replaceWithBing = false;

	private String source = "BING";
	
	private HashMap<String, ArrayList<String>> fileTitles = new HashMap<String, ArrayList<String>>(); 

	private AtomicInteger currentThreadCount = new AtomicInteger(0);
	private Integer maxThreadCount = 1;
	private Long sleepTime = 50L;

	public static void main(String[] args) {
		DOMConfigurator.configure("log4j.xml");
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

				AnchorTitleReplacer replacer = new AnchorTitleReplacer();
				replacer.execute();
			}

		}catch(Exception e){
			log.error("Error occured during replacer executor: ", e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public AnchorTitleReplacer() throws IOException {
		super();
		this.anchorFilePath = ConfigManager.getInstance().getProperty(ANCHOR_FILE_PATH_LABEL);
		this.outputPath = ConfigManager.getInstance().getProperty(OUTPUT_PATH_LABEL);
		this.inputTitlesPath = ConfigManager.getInstance().getProperty(INPUT_TITLES_PATH_LABEL);
		this.patternTitlesFilePath = ConfigManager.getInstance().getProperty(TITLES_FILE_PATH_LABEL);
		this.repeatCount = Integer.parseInt(ConfigManager.getInstance().getProperty(REPEAT_COUNT_LABEL));

		this.maxLineCount = Integer.parseInt(ConfigManager.getInstance().getProperty(MAX_LINE_COUNT_LABEL));
		this.minLineCount = Integer.parseInt(ConfigManager.getInstance().getProperty(MIN_LINE_COUNT_LABEL));

		this.replaceWithBing = Boolean.parseBoolean(ConfigManager.getInstance().getProperty(GET_ANCHOR_FROM_WEB_LABEL));
		
		this.cleanKey = Boolean.parseBoolean(ConfigManager.getInstance().getProperty(IS_CLEAN_KEY_LABEL));

		ProxyFactory.DELAY_FOR_PROXY = Integer.valueOf(ConfigManager.getInstance().getProperty(PROXY_DELAY_LABEL));
		ProxyFactory.PROXY_TYPE = ConfigManager.getInstance().getProperty(PROXY_TYPE_LABEL);
		proxyFactory = ProxyFactory.getInstance();
		proxyFactory.init(ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL));

		this.source = ConfigManager.getInstance().getProperty(SOURCE_LABEL);

		if(source == null || "".equals(source)){
			source = "GOOGLE";
		}

		this.maxThreadCount = Integer.valueOf(ConfigManager.getInstance().getProperty(MAX_THREAD_COUNT_LABEL));


		if(!replaceWithBing){
			this.maxThreadCount = 1;
			this.sleepTime = 1L;
		}
	}

	private void execute() throws IOException, InterruptedException{

		ArrayList<String> lines= readFile(this.anchorFilePath);
		ArrayList<String> titles = readTitlesFile(this.patternTitlesFilePath);
		ArrayList<Pattern> titlesPattern = new ArrayList<Pattern>();
		fileTitles = getFileTitles(this.inputTitlesPath);

		HashMap<String, Pattern> linePttrnMpng = new HashMap<String, Pattern>();
		ArrayList<String> keys = new ArrayList<String>();

		String patternStr = "";
		Pattern pattern = Pattern.compile(patternStr);

		for(String title: titles){
			/*if(replaceWithBing){
				patternStr = "(.*)\">(.*)</a>(.*)";
			}else{
				patternStr = "(.*)\">(" + title + ")</a>(.*)";
			}*/
			patternStr = "(.*)\">(" + title + ")</a>(.*)";
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

		//

		try{
			for(int i = 0; i < this.repeatCount; i++){
				HashMap<String, Pattern> linePttrnMpngNew = new HashMap<String, Pattern>(linePttrnMpng);
				ArrayList<String> keysNew = new ArrayList<String>(keys);
				lines= readFile(this.anchorFilePath);
				loop(linePttrnMpngNew, keysNew, titles);
			}
		}finally{
			saveBannedProxy(proxyFactory.getBannedProxyList());
		}
	}
	
	private HashMap<String, ArrayList<String>> getFileTitles(String inputTitlesFolder) throws IOException{
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
	
		File inputTitlesPath = new File(inputTitlesFolder);
		StringBuffer fileName;
		ArrayList<String> titles;
		for(File file : inputTitlesPath.listFiles()){
			fileName = new StringBuffer(file.getName());
			titles = readFile(file.getPath());
			result.put(fileName.substring(0, fileName.length()-4).toLowerCase(), titles);
		}
		
		return result;
	}
	
	private String getRandomTitleFromFiles(String key){
		return getRandomTitleFromFiles(key, false);
	}
	
	private String getRandomTitleFromFiles(String key, boolean cleanKey){
		Random rnd = new Random();
		ArrayList<String> keyTitles = null;
		
		if(cleanKey)
			key = key.replaceAll("[^A-Za-z0-9\\s\\-]", "");
		
		if((keyTitles = fileTitles.get(key.toLowerCase().trim())) != null){
			return keyTitles.get(rnd.nextInt(keyTitles.size()));
		}
		
		return "";
	}

	private void loop(HashMap<String, Pattern> lines, ArrayList<String> keys, ArrayList<String> titles) throws IOException, InterruptedException{

		ExecutorService extSrv = Executors.newFixedThreadPool(maxThreadCount);

		while(lines.size() > 0){

			while( currentThreadCount.get() == maxThreadCount ){
				Thread.sleep(sleepTime);
			}

			HashMap<String, Pattern> rndLines = getRndLines(lines, keys);

			ReplacerThread rplcrThrd = new ReplacerThread(this, rndLines, titles);
			extSrv.submit(rplcrThrd);
		}

		extSrv.shutdown();
		while( currentThreadCount.get() > 0 ){
			Thread.sleep(sleepTime);
		}

	}

	private HashMap<String, Pattern> getRndLines(HashMap<String, Pattern> lines, ArrayList<String> keys){
		Random rnd = new Random();
		int rndLnCount = minLineCount + rnd.nextInt(maxLineCount-minLineCount + 1);

		HashMap<String, Pattern> rndLines4Process = new HashMap<String, Pattern>();

		for(int i = 0; i < rndLnCount; i++){
			if(lines.size() > 0){
				String key = keys.remove(rnd.nextInt(keys.size()));
				Pattern ptrn = lines.remove(key);
				rndLines4Process.put(key, ptrn);
			}
		}

		return rndLines4Process;
	}

	private class ReplacerThread implements Runnable{

		private HashMap<String, Pattern> lines;
		private ArrayList<String> titles;
		private AnchorTitleReplacer replacer;

		public ReplacerThread(AnchorTitleReplacer replacer, HashMap<String, Pattern> lines, ArrayList<String> titles) {
			super();
			this.replacer = replacer;
			this.lines = lines;
			this.titles = titles;
		}

		@Override
		public void run() {
			replacer.incThrdCnt();
			ArrayList<String> rndLinesProcessed = null;

			try{
				rndLinesProcessed = processLines(lines, titles);
				//Save file
				File fileToSave = new File(outputPath, String.valueOf(System.currentTimeMillis())+lines.hashCode());
				appendLinesToFile(rndLinesProcessed, fileToSave, false);
			}finally{
				replacer.decThrdCnt();
			}
		}
	}

	private ArrayList<String> processLines(HashMap<String, Pattern> lines, ArrayList<String> titles){
		ArrayList<String> output = new ArrayList<String>();
		Random rnd = new Random();

		String fullTitle;
		String bookName;
		String newTitle;
		String newLine;

		for(String line: lines.keySet()){
			//Substring Name
			if(rnd.nextInt(3) < 2){
				Matcher matcher = lines.get(line).matcher(line);
				if (matcher.find()){
					fullTitle = matcher.group(2).trim();
					//System.out.println("Full title: " + fullTitle);
					bookName = matcher.group(3).trim();
					/*//System.out.println("Book name: " + bookName);
							if(fullTitle.equals(bookName)){
								log.debug("NEW TITLE FOUND: " + fullTitle);
							}*/
					if(replaceWithBing)
					{
						//replace title from file
						newTitle = getRandomTitleFromFiles(bookName, this.cleanKey);
						/*Snippet snippet = null;
						try {
							snippet = getSnippet(fullTitle);
							newTitle = snippet.getTitle();
						} catch (Exception e) {
							log.warn(String.format("Error occured during getting snippets: %s", e.getMessage()), e);
						} */

						if("".equals(newTitle)){
							appendLinesToFile(line, new File("result_not_found.txt"), true);
							log.warn(String.format("!!! TITLE WILL NOT BE CHANGED !!! Key: '%s'; Full line: '%s'",bookName, line));
							newTitle = titles.get(rnd.nextInt(titles.size())).replace("(.*)", bookName);
						}
						//newLine = line.replace(fullTitle, newTitle);
						newLine = line.replace(fullTitle, newTitle);
					}else{
						//TODO replace from file
						newTitle = titles.get(rnd.nextInt(titles.size())).replace("(.*)", bookName);
						newLine = line.replace(fullTitle, newTitle);
					}
					newLine = newLine.replaceAll("\\\\", "");
					output.add(newLine);
					//System.out.println("New line: " + newLine);
				}else{
					output.add(line);
				}
			}
			else{
				output.add(line);
			}
		}

		return output;
	}

	private ArrayList<String> readFile(String filePath) throws IOException{

		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<String> fileTitleList = new ArrayList<String>();

		try {
			br = new BufferedReader( new InputStreamReader( new FileInputStream(filePath), "UTF8" ) );

			String line = br.readLine();
			while(line != null && !"".equals(line.trim())){
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

		return fileTitleList;
	} 

	private ArrayList<String> readTitlesFile(String filePath) throws IOException{

		ArrayList<String> titles = readFile(this.patternTitlesFilePath);

		for(int i = 0; i < titles.size(); i++){
			titles.set(i, titles.get(i).replaceAll("\\(", "\\\\("));
			titles.set(i, titles.get(i).replaceAll("\\)", "\\\\)"));
			titles.set(i, titles.get(i).replaceAll("\\[Book\\]", "(.*)"));
		}

		return titles;
	}

	private synchronized static void appendLinesToFile(String line, File file, boolean append) {
		ArrayList<String> lines = new ArrayList<>();
		lines.add(line);
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

	private Snippet getSnippet(String key) throws Exception{
		Random rnd = new Random();
		SnippetExtractor snippetExtractor = new SnippetExtractor(null, proxyFactory, null);
		//TODO Add Snippet task chooser
		SnippetTask snippetTask = getTaskBySource(source, key);
		ArrayList<Snippet> snippets = snippetExtractor.extractSnippetsFromPageContent(snippetTask);

		while(snippets.size() == 0 || snippetTask.getPage() != 1){
			snippetTask.setPage(reducePage(snippetTask.getPage()));
			snippets = snippetExtractor.extractSnippetsFromPageContent(snippetTask);
		}
		
		if(snippets.size() == 0){
			throw new IOException("Could not extract snippets. Snippet extracted size == 0");
		}else{
			return snippets.get(rnd.nextInt(snippets.size()));
		}
	}
	
	private int reducePage(int currentPage){
		if(currentPage/5 > 1){
			log.info(String.format("Recude page from %d to %d",currentPage, currentPage/5 ));
			return currentPage/5;
		}else{
			log.info(String.format("Recude page from %d to %d",currentPage, 1));
			return 1;
		}
	}

	private SnippetTask getTaskBySource(String source, String key) throws Exception{
		Random rnd = new Random();
		SnippetTask task = null;

		if("GOOGLE".equals(source.toUpperCase().trim())){
			task = new GoogleSnippetTask(key);
			task.setPage(rnd.nextInt(50));
		} else
			if("BING".equals(source.toUpperCase().trim())){
				task = new BingSnippetTask(key);
				task.setPage(1+rnd.nextInt(50));
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
}
