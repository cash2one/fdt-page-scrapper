package com.fdt.snippetutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

public class AnchorTitleReplacer {

	private static final Logger log = Logger.getLogger(AnchorTitleReplacer.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";

	private final static String MAX_LINE_COUNT_LABEL = "max_line_count";
	private final static String MIN_LINE_COUNT_LABEL = "min_line_count";

	private final static String IS_DELETE_USED_LINE_LABEL = "delete_used_line";

	private final static String ANCHOR_FILE_PATH_LABEL = "anchor_file_path";
	private final static String REPEAT_COUNT_LABEL = "repeat_count";
	private final static String OUTPUT_PATH_LABEL = "output_path";
	private final static String TITLES_FILE_PATH_LABEL = "titles_file_path";

	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String PROXY_DELAY_LABEL = "proxy_delay";
	private final static String PROXY_TYPE_LABEL = "proxy_type";

	private final static String GET_ANCHOR_FROM_WEB_LABEL = "get_anchor_from_web";

	private static final String SOURCE_LABEL = "source";

	private final static String MAX_THREAD_COUNT_LABEL = "max_thread_count";

	private String anchorFilePath;
	private String outputPath;

	private int maxLineCount = 7;
	private int minLineCount = 3;

	private String titlesFilePath;

	private int repeatCount;

	private boolean isDeleteUsedLine = false;

	ProxyFactory proxyFactory;

	private boolean replaceWithBing = false;

	private String source = "BING";

	private ArrayList<String> usedLines = new ArrayList<String>();
	private ArrayList<String> newLines = new ArrayList<String>();

	private Integer currentThreadCount = 0;
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
		this.titlesFilePath = ConfigManager.getInstance().getProperty(TITLES_FILE_PATH_LABEL);
		this.isDeleteUsedLine = Boolean.parseBoolean(ConfigManager.getInstance().getProperty(IS_DELETE_USED_LINE_LABEL));
		this.repeatCount = Integer.parseInt(ConfigManager.getInstance().getProperty(REPEAT_COUNT_LABEL));

		this.maxLineCount = Integer.parseInt(ConfigManager.getInstance().getProperty(MAX_LINE_COUNT_LABEL));
		this.minLineCount = Integer.parseInt(ConfigManager.getInstance().getProperty(MIN_LINE_COUNT_LABEL));

		this.replaceWithBing = Boolean.parseBoolean(ConfigManager.getInstance().getProperty(GET_ANCHOR_FROM_WEB_LABEL));

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
		ArrayList<String> titles = readTitlesFile(this.titlesFilePath);

		try{
			for(int i = 0; i < this.repeatCount; i++){
				lines= readFile(this.anchorFilePath);
				loop(lines, titles);
			}
		}finally{
			saveBannedProxy(proxyFactory.getBannedProxyList());
		}
	}

	private void loop(ArrayList<String> lines, ArrayList<String> titles) throws IOException, InterruptedException{

		while(lines.size() > 0){

			while( currentThreadCount == maxThreadCount ){
				Thread.sleep(sleepTime);
			}

			ReplacerThread rplcrThrd = new ReplacerThread(this, lines, titles);
			rplcrThrd.start();
		}

		while( currentThreadCount > 0 ){
			Thread.sleep(sleepTime);
		}

	}


	private class ReplacerThread extends Thread {

		private ArrayList<String> lines;
		private ArrayList<String> titles;
		private AnchorTitleReplacer replacer;

		public ReplacerThread(AnchorTitleReplacer replacer, ArrayList<String> lines, ArrayList<String> titles) {
			super();
			this.replacer = replacer;
			this.lines = lines;
			this.titles = titles;
		}

		@Override
		public void run() {
			ArrayList<String> rndLines4Process;
			ArrayList<String> rndLinesProcessed = null;

			try{
				rndLines4Process = getRndLines(lines);
				//TODO Process links and save to file
				rndLinesProcessed = processLines(rndLines4Process, titles);
				//Save file
				File fileToSave = new File(outputPath, String.valueOf(System.currentTimeMillis())+rndLines4Process.hashCode());
				appendLinesToFile(rndLinesProcessed, fileToSave);
			} catch (IOException e) {
				log.error("Error occured saving lines to file: ", e);
			}finally{
				replacer.decThrdCnt();
			}
		}

		@Override
		public void start(){
			replacer.incThrdCnt();
			super.start();
		}
	}

	private ArrayList<String> getRndLines(ArrayList<String> lines){
		Random rnd = new Random();
		int rndLnCount = minLineCount + rnd.nextInt(maxLineCount-minLineCount + 1);

		ArrayList<String> rndLines4Process = new ArrayList<String>();

		for(int i = 0; i < rndLnCount; i++){
			if(lines.size() > 0){
				rndLines4Process.add(lines.remove(rnd.nextInt(lines.size())));
			}
		}

		return rndLines4Process;
	}

	private ArrayList<String> processLines(ArrayList<String> input, ArrayList<String> titles){
		ArrayList<String> output = new ArrayList<String>();
		Random rnd = new Random();

		String fullTitle;
		String bookName;
		String newTitle;
		String newLine;

		for(String line:input){
			int matchStepCount = 0;
			for(String title:titles)
			{
				matchStepCount++;
				String patternStr = "";
				if(replaceWithBing){
					patternStr = "(.*)\">(.*)</a>(.*)";
				}else{
					patternStr = "(.*)\">(" + title + ")</a>(.*)";
				}

				if(line.matches(patternStr))
				{
					//Substring Name
					if(rnd.nextInt(3) < 2){
						Pattern pattern = Pattern.compile(patternStr);
						Matcher matcher = pattern.matcher(line);
						if (matcher.find()){
							fullTitle = matcher.group(2).trim();
							//System.out.println("Full title: " + fullTitle);
							bookName = matcher.group(3).trim();
							/*//System.out.println("Book name: " + bookName);
							if(fullTitle.equals(bookName)){
								log.debug("NEW TITLE FOUND: " + fullTitle);
							}*/
							if(replaceWithBing){
								do{
									newTitle = "";
									try {
										newTitle = getSnippet(fullTitle).getTitle();
									} catch (Exception e) {
										log.error(String.format("Error occured during getting snippets: %s", e.getMessage()), e);
									} 
								}while("".equals(newTitle));
								//newLine = line.replace(fullTitle, newTitle);
								newLine = line.replace(fullTitle, newTitle);
							}else{
								newTitle = titles.get(rnd.nextInt(titles.size())).replace("(.*)", bookName);
								newLine = line.replace(fullTitle, newTitle);
							}
							output.add(newLine);
							//System.out.println("New line: " + newLine);
						}else{
							output.add(line);
						}
					}
					else{
						output.add(line);
					}
					break;
				}else{
					if(matchStepCount == titles.size()){
						log.error("NOT Processed line: " + line);
					}
				}
			}
		}

		return output;
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

		ArrayList<String> titles = readFile(this.titlesFilePath);

		for(int i = 0; i < titles.size(); i++){
			titles.set(i, titles.get(i).replaceAll("\\(", "\\\\("));
			titles.set(i, titles.get(i).replaceAll("\\)", "\\\\)"));
			titles.set(i, titles.get(i).replaceAll("\\[Book\\]", "(.*)"));
		}

		return titles;
	}

	private static void appendLinesToFile(ArrayList<String> lines, File file) throws IOException {
		if(file.exists()){
			file.delete();
		}

		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8"));
			for(String line: lines){
				bufferedWriter.append(line);
				bufferedWriter.newLine();
			}
		} finally {
			//Close the BufferedWriter
			if (bufferedWriter != null) {
				bufferedWriter.flush();
				bufferedWriter.close();
			}
		}
	}

	private Snippet getSnippet(String key) throws Exception{
		Random rnd = new Random();
		SnippetExtractor snippetExtractor = new SnippetExtractor(null, proxyFactory, null);
		//TODO Add Snippet task chooser
		SnippetTask snippetTask = getTaskBySource(source, key);
		ArrayList<Snippet> snippets = snippetExtractor.extractSnippetsFromPageContent(snippetTask);

		if(snippets.size() == 0){
			throw new IOException("Could not extract snippets");
		}else{
			return snippets.get(rnd.nextInt(snippets.size()));
		}
	}

	private SnippetTask getTaskBySource(String source, String key) throws Exception{
		Random rnd = new Random();
		SnippetTask task = null;

		if("GOOGLE".equals(source.toUpperCase().trim())){
			task = new GoogleSnippetTask(key);
			task.setPage(rnd.nextInt(9));
		} else
			if("BING".equals(source.toUpperCase().trim())){
				task = new BingSnippetTask(key);
				task.setPage(1+rnd.nextInt(5));
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
			currentThreadCount++;
			log.debug("Current thread count: " + currentThreadCount);
			notifyAll();
		}

	}

	private void decThrdCnt(){
		synchronized (this) {
			currentThreadCount--;
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