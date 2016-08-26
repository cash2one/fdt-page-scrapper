package com.fdt.dailymotion.task;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.fdt.utils.Utils;

public class NewsTask{

	private static final Logger log = Logger.getLogger(NewsTask.class);

	private File inputFile;
	private File[] imageFiles = new File[]{};
	private File previewImageFile;
	private File videoFile;
	//private File videoFileWOAudio;

	private File templateFile;
	
	private String shortUrlList;

	private String videoTitle = "";
	private String videoDescription = "";
	
	private String videoid = "";
	private String imageUrl = "";
	private String postLink = "";

	private String key = "";
	private String author = "";
	private String snippets = "";

	private String uploadUrl = "";

	private int attempsCount = 1;
	//empty result
	private boolean isUseImageFromLinkOnly=true;
	private File[] randImgFiles;
	
	private boolean isUsePreview = false;
	
	private boolean preGenFileLoaded = false;

	
	public static String titleTmplListFilePath = null;
	public static String beforeLinkTmplFilePath = null;
	public static String descTmplListFilePath = null;
	
	private static final String LINE_FEED = "\r\n";
	
	private static final Random rnd = new Random();
	
	private static final List<String[]> BRACKETS_BIG = new ArrayList<String[]>(
		    Arrays.asList(
		    		new String[]{"[","]"},
		    		new String[]{"{","}"},
		    		new String[]{"|","|"},
		    		new String[]{"!","!"},
		    		new String[]{"{{","}}"},
		    		new String[]{"[[","]]"},
		    		new String[]{"((","))"},
		    		new String[]{"!!","!!"},
		    		new String[]{"",""}
		    )
	);
	
	private static final List<String[]> BRACKETS_SMALL = new ArrayList<String[]>(
		    Arrays.asList(
		    		new String[]{"(",")"},
		    		new String[]{"\"","\""}
		    )
	);

	public NewsTask(File inputFile, File templateFile, String shortUrlList, boolean isUseImageFromLinkOnly, File[] randImgFiles, boolean isUsePreview) throws Exception {
		super();
		this.inputFile = inputFile;
		this.templateFile = templateFile;
		this.shortUrlList = shortUrlList;
		this.isUseImageFromLinkOnly = isUseImageFromLinkOnly;
		this.randImgFiles = randImgFiles;
		this.isUsePreview = isUsePreview;
		//TODO Read and parse file
	}
	
	public NewsTask(String key, File templateFile, String shortUrlLis, File pregenFile) throws Exception {
		super();
		this.key = key;
		this.videoTitle = "";
		this.templateFile = templateFile;
		this.shortUrlList = shortUrlLis;
		
		this.inputFile = pregenFile;
		this.videoFile = pregenFile;
		this.preGenFileLoaded = true;
		//TODO Read and parse file
	}

	public void parseFile() throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;

		StringBuilder fileAsStr = new StringBuilder();

		try {
			int lineIndex = 0;
			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
				fileAsStr.append(line).append(LINE_FEED);
				if(!"".equals(line)){
					lineIndex++;
					switch(lineIndex){
					case 1:{
						key = line.trim();
						
						List<String> titleTmplts = Utils.loadFileAsStrList(titleTmplListFilePath);
						videoTitle = titleTmplts.get(rnd.nextInt(titleTmplts.size())).replaceAll( "\\[Book\\]",  Matcher.quoteReplacement(key));
						break;
					}
					case 2:{
						
						break;
					}
					case 3:{
						author = line.substring(line.indexOf("by") + 2).trim();
						break;
					}
					}
				}
			}
			
			extractPostLink(fileAsStr.toString());
			
			//Generate description
			this.videoDescription = generateDescription(Utils.loadFileAsString(new File(beforeLinkTmplFilePath)), Utils.loadFileAsStrList(descTmplListFilePath));

			log.debug("File content: " + fileAsStr.toString());
			
			if( !preGenFileLoaded )
			{
				//загружаем только файл по ссылке, из него и будет генерироваться видео
				loadImage(fileAsStr.toString());
				if(!isUseImageFromLinkOnly){
					//иначе загружаем все файлы из указанной папки + используем файл из ссылки в самом начале
					loadImages();
				}
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
	}

	private void loadImage(String fileContent) throws Exception{
		Pattern imgPattern =Pattern.compile("((http://)?(www.)?([\\.\\,\\-\\_\\+\\(\\)@/a-zA-Z0-9]+(jpg|png)))");
		Matcher matcher = imgPattern.matcher(fileContent);
		if(matcher.find())
		{
			log.debug("Image found: " + matcher.group(1));
			System.out.println("Image found: " + matcher.group(1));
			imageUrl = matcher.group(1);
			String imageFormat = matcher.group(5);

			BufferedImage img = ImageIO.read(new URL(imageUrl));
			//write image to file
			this.imageFiles = new File[]{new File("images/"+getFileNameWOExt(this.inputFile.getName()) + "." + imageFormat)};
			this.videoFile = new File("output_video/"+getFileNameWOExt(this.inputFile.getName()) + ".mov");
			//this.videoFileWOAudio = new File("output_video/"+getFileNameWOExt(this.inputFile.getName()) + "_wo_audio.mov");
			ImageIO.write(img, imageFormat, this.imageFiles[0]);
		}else{
			throw new Exception("Image URL NOT found");
		}
	}
	
	private void loadImages() throws Exception{
		Random rnd = new Random();
		if(randImgFiles.length >= 5){
			if(randImgFiles.length <= 7){
				this.imageFiles = randImgFiles;
			}else{
				//get random 5-7 images;
				int position = 0;
				int count = 5 + rnd.nextInt(3);
				Set<File> newRandFileArray = new HashSet<File>(count);
				
				while(position < count){
					newRandFileArray.add(randImgFiles[rnd.nextInt(randImgFiles.length)]);
					if(newRandFileArray.size()-1 == position){
						position++;
					}
				}
				
				File[] loadedImgs = newRandFileArray.toArray(new File[newRandFileArray.size()]);
				File[] rndImgArray = new File[loadedImgs.length + this.imageFiles.length];
				
				System.arraycopy(this.imageFiles, 0, rndImgArray, 0, this.imageFiles.length);
				System.arraycopy(loadedImgs, 0, rndImgArray, this.imageFiles.length, loadedImgs.length);
				
				this.imageFiles = rndImgArray;
			}
			this.videoFile = new File("output_video/"+getFileNameWOExt(this.inputFile.getName()) + ".mov");
		}else{
			throw new Exception("Please add at least 5 random images for video generating!!!");
		}
	}

	public File getVideoFile() {
		return videoFile;
	}

	/*public File getVideoFileWOAudio() {
		return videoFileWOAudio;
	}*/

	public void setVideoFile(File videoFile) {
		this.videoFile = videoFile;
	}

	private void extractPostLink(String fileContent) throws Exception{
		//Pattern imgPattern =Pattern.compile("href=\"((http(s)?://)?(www.)?([\\.a-z0-9/\\-]+))\"");
		Pattern imgPattern =Pattern.compile("(http://(" + shortUrlList + "){1,1}/([a-zA-Z0-9/]+))");
		Matcher matcher = imgPattern.matcher(fileContent);
		if(matcher.find()){
			log.debug("Link found: " + matcher.group(1));
			System.out.println("Link found: " + matcher.group(1));
			//TODO Parse correct post link
			this.postLink = matcher.group(1);
		}else{
			throw new Exception("Post link NOT found");
		}
	}

	private String getFileNameWOExt(String fullName){
		return fullName.substring(0,fullName.lastIndexOf('.'));
	}

	public int getAttempsCount() {
		return attempsCount;
	}

	public void incAttempsCount() {
		this.attempsCount++;
	}

	public boolean isResultEmpty(){
		if(snippets == null || "".equals(snippets.trim())){
			return true;
		}
		return false;
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}

	public File getInputFile() {
		return inputFile;
	}

	public String getVideoid() {
		return videoid;
	}

	public void setVideoId(String videoid) {
		this.videoid = videoid;
	}

	public String getSnippets() {
		return snippets;
	}

	public void setSnippets(String snippets) {
		//Delete external url
		snippets = snippets.replaceAll("((https|http)?:\\/\\/)?(www\\.)?([\\w\\.\\-]+)(\\.[a-zA-Z]{2,6})(\\/[\\w\\.\\-]*)*\\/?", "");

		this.snippets = snippets;
	}

	/*public String getVideoTitle() {
		return videoTitle;
	}*/
	
	public String getVideoTitle() 
	{
		return videoTitle;
	}

	public String getVideoDescription() {
		return videoDescription;
	}

	public String getTags() {
		String tags[] = videoTitle.split(" ");
		StringBuilder tagsList = new StringBuilder();
		for(String tag : tags){
			tagsList.append("\"").append(tag).append("\",");
		}
		if(tagsList.length() > 0){
			tagsList.setLength(tagsList.length()-1);
		}
		return tagsList.toString();
	}
	
	public String getTags(int maxTagCount, int maxLenSize) throws UnsupportedEncodingException {
		String tags[] = videoTitle.split(" ");
		StringBuilder tagsList = new StringBuilder();
		
		int curTag = 0;
		int testTagsLenght = 0;
		
		for(String tag : tags){
			testTagsLenght = URLEncoder.encode(tagsList.toString(), "UTF-8").length() + URLEncoder.encode("\"" + tag + "\",", "UTF-8").length();
			
			if(curTag < maxTagCount && testTagsLenght < maxLenSize){
				curTag++;
				tagsList.append("\"").append(tag).append("\",");
			}else{
				break;
			}
		}
		
		if(tagsList.length() > 0){
			tagsList.setLength(tagsList.length()-1);
		}
		return tagsList.toString();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	/*public String getDescription() throws Exception{
		String description = null;
		description = Utils.loadFileAsString(this.templateFile);
		description = description.replaceAll("\\[KEYWORD\\]", Matcher.quoteReplacement(key));
		description = description.replaceAll("\\[LINK\\]", Matcher.quoteReplacement(postLink));
		description = description.replaceAll("\\[SNIPPETS\\]", Matcher.quoteReplacement(snippets));

		return description;
	}*/
	
	
	
	public String generateDescription(String beforeLinkTmpl, List<String> descTmpl) throws Exception{
		StringBuffer beforeLnk = new StringBuffer();
		beforeLnk.append(Utils.synonymizeText(beforeLinkTmpl)).append(LINE_FEED).append(LINE_FEED);
		
		StringBuffer newDesc = new StringBuffer();
		newDesc.append(Utils.synonymizeText(descTmpl.get(rnd.nextInt(descTmpl.size()))));
		
		String[] words = newDesc.toString().split("\\s+");
		newDesc = new StringBuffer();
		for(String word : words){
			if(!word.equalsIgnoreCase("[KEYWORD]") && !word.equalsIgnoreCase("[AUTHOR]")){
				newDesc.append(quoteringStr(word,BRACKETS_BIG)).append(" ");
			}else{
				newDesc.append(quoteringStr(word,BRACKETS_SMALL)).append(" ");
			}
		}
		
		beforeLnk.append(newDesc);
		
		String description = beforeLnk.toString();
		description = description.replaceAll("\\[LINK\\]", Matcher.quoteReplacement(postLink));
		description = description.replaceAll("\\[KEYWORD\\]", Matcher.quoteReplacement(key));
		description = description.replaceAll("\\[AUTHOR\\]", Matcher.quoteReplacement(author));
		description = description.replaceAll("\\[SNIPPETS\\]", Matcher.quoteReplacement(snippets));

		return description;
	}
	
	private String quoteringStr(String inputStr, List<String[]> BRACKETS){
		StringBuffer result = new StringBuffer();
		
		int brIdx = rnd.nextInt(BRACKETS.size());
		
		result.append(BRACKETS.get(brIdx)[0]).append(inputStr).append(BRACKETS.get(brIdx)[1]);
		
		return result.toString();
	}

	public File[] getImageFiles() {
		return imageFiles;
	}

	private String getFileAsString(File file) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;

		StringBuilder fileAsStr = new StringBuilder();

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while( (line = br.readLine()) != null){
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

	public File getPreviewImageFile() {
		return previewImageFile;
	}

	public void setPreviewImageFile(File previewImageFile) {
		this.previewImageFile = previewImageFile;
	}

	public boolean isUsePreview() {
		return isUsePreview;
	}

	public boolean isGetImageFromLink() {
		return isUseImageFromLinkOnly;
	}
}
