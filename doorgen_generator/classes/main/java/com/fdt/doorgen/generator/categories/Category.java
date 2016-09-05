package com.fdt.doorgen.generator.categories;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.fdt.doorgen.generator.DoorgenGeneratorRunner;
import com.fdt.utils.Utils;

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
	private String metaKeywords;
	private String metaDesc;
	
	//category page content
	private String tmplText;
	private String genText;
	private boolean isUpdated;
	
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

	public String getTmplText() {
		return tmplText;
	}

	public void setTmplText(String tmplText) {
		this.tmplText = tmplText;
	}

	public String getGenText() {
		return genText;
	}

	public void setGenText(String genText) {
		this.genText = genText;
	}

	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}

	public static Category parseCategory(File catFolder, List<Item> tagItems, String titleTmpl, String metaDescrTmpl, String metaKeyWordsTmpl, HashMap<Integer,List<String>> preTitles) {
		// TODO Handle synonym case
		String catContent = Utils.loadFileAsString(new File(catFolder,"main.txt"));
		
		Category cat = new Category();
		
		cat.catName = String.valueOf(catFolder.getName());
		cat.catLatinName = String.valueOf(makeUrlKey(catFolder.getName()));
		//TODO Fill title
		cat.title = fillTemplate(titleTmpl, cat.catName, preTitles);
		//cat.title = String.valueOf(catFolder.getName());
		cat.abbr = String.valueOf(makeUrlKey(catFolder.getName()));
		//TODO Fill
		cat.metaKeywords = fillTemplate(cat.catName, metaKeyWordsTmpl, preTitles);
		//TODO Fill
		cat.metaDesc = fillTemplate(cat.catName, metaDescrTmpl, preTitles);
		cat.tmplText = catContent;
		cat.genText = catContent;
		cat.isUpdated = false;

		return cat;
	}
	
	private static String fillTemplate(String template, String keyWord, HashMap<Integer,List<String>> preTitles){

		//TODO Fill template
		String output = template.replaceAll("\\[keyword\\]", keyWord);
		
		for(int idx : preTitles.keySet()){
			int prettlLstSz = preTitles.get(idx).size();
			output = output.replaceAll("\\["+DoorgenGeneratorRunner.PRETITLES_REGEXP + (idx > 0?String.valueOf(idx):"") +"\\]", preTitles.get(idx).get(rnd.nextInt(prettlLstSz)));
		}
		
		return output;
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
