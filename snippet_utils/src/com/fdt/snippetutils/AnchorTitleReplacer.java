package com.fdt.snippetutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.scrapper.SnippetExtractor;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.BingSnippetTask;
import com.fdt.scrapper.task.ConfigManager;
import com.fdt.scrapper.task.Snippet;

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

	private String anchorFilePath;
	private String outputPath;

	private int maxLineCount = 7;
	private int minLineCount = 3;

	private String titlesFilePath;

	private int repeatCount;

	private boolean isDeleteUsedLine = false;

	ProxyFactory proxyFactory;
	
	private boolean replaceWithBing = false;

	private ArrayList<String> usedLines = new ArrayList<String>();
	private ArrayList<String> newLines = new ArrayList<String>();

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
			log.error(e);
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

	}

	private void execute() throws IOException{

		ArrayList<String> lines= readFile(this.anchorFilePath);
		ArrayList<String> titles = readTitlesFile(this.titlesFilePath);

		for(int i = 0; i < this.repeatCount; i++){
			lines= readFile(this.anchorFilePath);
			loop(lines, titles);
		}
	}

	private void loop(ArrayList<String> lines, ArrayList<String> titles) throws IOException{
		ArrayList<String> rndLines4Process;
		ArrayList<String> rndLinesProcessed = null;

		while(lines.size() > 0){
			rndLines4Process = getRndLines(lines);
			//TODO Process links and save to file
			rndLinesProcessed = processLines(rndLines4Process, titles);
			//Save file
			File fileToSave = new File(outputPath, String.valueOf(System.currentTimeMillis())+rndLines4Process.hashCode());
			appendLinesToFile(rndLinesProcessed, fileToSave);
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
				String patternStr = "(.*)\"\\>(" + title + ")\\<\\/a\\>(.*)";

				if(line.matches(patternStr))
				{
					//System.out.println("Mached");

					//Substring Name
					if(rnd.nextInt(3) < 2){
						Pattern pattern = Pattern.compile(patternStr);
						Matcher matcher = pattern.matcher(line);
						if (matcher.find()){
							fullTitle = matcher.group(2).trim();
							//System.out.println("Full title: " + fullTitle);
							bookName = matcher.group(3).trim();
							//System.out.println("Book name: " + bookName);
							if(fullTitle.equals(bookName)){
								log.debug("NEW TITLE FOUND: " + fullTitle);
							}
							if(replaceWithBing){
								do{
									newTitle = "";
									try {
										newTitle = getSnippet(bookName).getTitle();
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
	
	private Snippet getSnippet(String key) throws IOException, XPathExpressionException, ParseException{
		Random rnd = new Random();
		SnippetExtractor snippetExtractor = new SnippetExtractor(null, proxyFactory, null);
		//TODO Add Snippet task chooser
		BingSnippetTask bingTask = new BingSnippetTask(key);
		bingTask.setPage(1+rnd.nextInt(5));
		ArrayList<Snippet> snippets = snippetExtractor.extractSnippetsFromPageContent(bingTask);
		
		if(snippets.size() == 0){
			throw new IOException("Could not extract snippets");
		}else{
			return snippets.get(rnd.nextInt(snippets.size()));
		}
	}
}