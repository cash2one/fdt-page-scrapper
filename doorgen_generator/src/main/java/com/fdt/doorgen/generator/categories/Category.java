package com.fdt.doorgen.generator.categories;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.fdt.doorgen.generator.DoorgenGeneratorRunner;
import com.fdt.doorgen.generator.categories.Tag.CategoryParentType;
import com.fdt.utils.Utils;
import com.mysql.fabric.xmlrpc.base.Array;

public class Category {

	private static final String PRETITLES_REGEXP = "onestringpage";

	private int id = -1;

	private String catName;
	private String catLatinName;

	private String catType;

	private Category parentCat = null;

	//TODO Parse params from xml file
	private HashMap<String, String> extraCatParams = new HashMap<String, String>();

	private String abbr;
	private String title;
	private String titleTmpl;
	private String metaKeywords;
	private String metaKeywordsTmpl;
	private String metaDesc;
	private String metaDescTmpl;

	//category page content
	private String textTmpl;
	private String text;
	private boolean isUpdated;

	private List<Tag> tags = new ArrayList<Tag>();
	
	private static Random rnd = new Random();

	public int getId() {
		return id;
	}

	public String getAbbr() {
		return abbr;
	}

	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}

	public String getMetaKeywords() {
		return metaKeywords;
	}

	public void setMetaKeywords(String metaKeywords) {
		this.metaKeywords = metaKeywords;
	}

	public String getMetaDesc() {
		return metaDesc;
	}

	public void setMetaDesc(String metaDesc) {
		this.metaDesc = metaDesc;
	}

	public String getCategoryName() {
		return catName;
	}

	public String getCategoryLatin() {
		return catLatinName;
	}

	public Category getParentCategory() {
		return parentCat;
	}

	public void setParentCategory(Category parentCategory) {
		this.parentCat = parentCategory;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTextTmpl() {
		return textTmpl;
	}

	public void setTmplText(String tmplText) {
		this.textTmpl = tmplText;
	}

	public String getText() {
		return text;
	}

	public String getTitleTmpl() {
		return titleTmpl;
	}

	public String getMetaKeywordsTmpl() {
		return metaKeywordsTmpl;
	}

	public String getMetaDescTmpl() {
		return metaDescTmpl;
	}

	public void setGenText(String genText) {
		this.text = genText;
	}

	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}
	
	public List<Tag> getTags() {
		return tags;
	}

	public static Category parseCategory(File catFolder, String titleTmpl, String metaDescrTmpl, String metaKeyWordsTmpl, HashMap<Integer,List<String>> preTitles, HashMap<String,String> catUrls) {
		// TODO Handle synonym case
		String catContent = Utils.loadFileAsString(new File(catFolder,"main.txt"));

		Category cat = new Category();

		String[] abbrArr = catFolder.getName().split("~",2);
		
		cat.catName = abbrArr[0];
		
		if(catUrls.containsKey(cat.catName)){
			cat.catLatinName = makeUrlKey(catUrls.get(cat.catName));
		}else{
			cat.catLatinName = makeUrlKey(cat.catName);
		}
		//TODO Fill title
		cat.titleTmpl = titleTmpl;
		cat.title = Utils.synonymizeText(Utils.fillTemplate(titleTmpl, cat.catName, preTitles, DoorgenGeneratorRunner.PRETITLES_REGEXP));
		
		//cat.title = String.valueOf(catFolder.getName());
		if(abbrArr.length > 1 && abbrArr[1] != null && !abbrArr[1].trim().isEmpty()){
			cat.abbr = abbrArr[1].trim();
		}else{
			cat.abbr = cat.catName;
		}

		//TODO Fill
		cat.metaKeywordsTmpl = metaKeyWordsTmpl;
		cat.metaKeywords = Utils.synonymizeText(Utils.fillTemplate(metaKeyWordsTmpl, cat.catName, preTitles, DoorgenGeneratorRunner.PRETITLES_REGEXP));
		//TODO Fill
		cat.metaDescTmpl = metaDescrTmpl;
		cat.metaDesc = Utils.synonymizeText(Utils.fillTemplate(metaDescrTmpl, cat.catName, preTitles, DoorgenGeneratorRunner.PRETITLES_REGEXP));
		
		cat.textTmpl = catContent;
		cat.text = Utils.synonymizeText(Utils.fillTemplate(catContent, cat.catName, preTitles, DoorgenGeneratorRunner.PRETITLES_REGEXP));
		
		cat.isUpdated = false;
		
		//load current category tags
		File tagFile = new File(catFolder,"tags.txt");
		if(tagFile.exists() && tagFile.isFile()){
			cat.tags = Tag.loadTags(tagFile, -1, CategoryParentType.CATEGORY, cat.catName, preTitles);
		}
		
		return cat;
	}

	/*private static String constructDesc(List<Item> catItems, HashMap<Integer,List<String>> preTitles){
		StringBuffer strBuf = new StringBuffer();

		for(int i = 3; i < 6 && catItems.size() > i; i++){
			strBuf.append( WordUtils.capitalizeFully(catItems.get(i).getKey().toLowerCase())).append(" ").append(preTitles.get(i).toUpperCase()).append(" | ");
		}

		if(strBuf.length() > 3){
			strBuf.setLength(strBuf.length()-3);
		}

		return strBuf.toString();
	}

	private static String constructKeywords(List<Item> catItems){
		StringBuffer strBuf = new StringBuffer();

		for(int i = 0; i < 6 && catItems.size() > i; i++){
			strBuf.append( WordUtils.capitalizeFully(catItems.get(i).getKey().toLowerCase())).append(", ");
		}

		if(strBuf.length() > 2){
			strBuf.setLength(strBuf.length()-2);
		}

		return strBuf.toString();
	}*/

	public static String makeUrlKey(String input){
		return input.replaceAll("[^0-9a-zA-z\\s\\-]", " ").trim().replaceAll("\\s+", "-").toLowerCase();
	}
}
