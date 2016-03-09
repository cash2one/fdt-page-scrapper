package com.fdt.scrapper.task;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HTTP;

public abstract class SnippetTask
{
	public final static String KEY_WORDS_KEY = "#KEY_WORDS#";
	public final static String LANGUAGE_KEY = "#LANGUAGE#";
	public final static String PAGE_NUMBER = "#PAGE_NUM#";

	protected String scrapperUrl = "";
	private String xpathSnippets = "";
	private String xpathTitle = "";
	private String xpathLink = "";
	private String xpathDesc = "";
	private String xpathRstlCnt = "";
	
	protected String keyWords = "";
	protected String keyWordsNative = "";
	protected String keyWordsOrig = "";
	protected String language = "en";
	protected String host = "";
	
	private boolean useOrigKeywords = false;
	
	protected List<Integer> bannedRespCodes = new ArrayList<Integer>();
	protected boolean encodeKeywords = false;
	protected int page = 1;
	
	protected Map<String,String> extraParams = new HashMap<String, String>();
	
	protected String source = "";

	private int attemptCount = 1;

	private String result = null;
	
	private ArrayList<Snippet> snipResult = new ArrayList<Snippet>();

	public SnippetTask(String keyWords)
	{
		super();
		this.keyWordsNative = keyWords.replaceAll("/", " ").replaceAll("\t", " ");
		this.keyWords = keyWords.replace(' ', '+').replace('\t', '+');
		this.keyWordsOrig = keyWords;
		initExtraParams();
	}

	public SnippetTask()
	{
		super();
		initExtraParams();
	}

	protected abstract void initExtraParams();
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		if(language != null && !"".equals(language.trim())){
			this.language = language;
		}else{
			this.language = "en";
		}
	}

	public String getKeyWords()
	{
		return keyWords;
	}


	public void setKeyWords(String keyWords)
	{
		this.keyWordsNative = keyWords.replaceAll("/", " ");
		this.keyWords = keyWords.replace(' ', '+');
		this.keyWordsOrig = keyWords;
		initExtraParams();
	}


	public String getScrapperUrl()
	{
		return scrapperUrl;
	}

	public void setScrapperUrl(String scrapperUrl)
	{
		this.scrapperUrl = scrapperUrl;
	}
	public String getXpathSnipper()
	{
		return xpathSnippets;
	}
	public void setXpathSnippet(String xpathSnipper)
	{
		this.xpathSnippets = xpathSnipper;
	}
	public String getXpathTitle()
	{
		return xpathTitle;
	}
	public void setXpathTitle(String xpathTitle)
	{
		this.xpathTitle = xpathTitle;
	}
	public String getXpathDesc()
	{
		return xpathDesc;
	}
	public void setXpathDesc(String xpathDesc)
	{
		this.xpathDesc = xpathDesc;
	}

	public String getXpathLink()
	{
		return xpathLink;
	}

	public void setXpathLink(String xpathLink)
	{
		this.xpathLink = xpathLink;
	}

	public String getXpathRstlCnt() {
		return xpathRstlCnt;
	}

	public void setXpathRstlCnt(String xpathRstlCnt) {
		this.xpathRstlCnt = xpathRstlCnt;
	}

	public String getFullUrl(){
		String result = "";
		if(!isEncodeKeywords()){
			result = scrapperUrl.replace(KEY_WORDS_KEY, useOrigKeywords?keyWordsOrig:keyWords).replace(LANGUAGE_KEY, language).replace(PAGE_NUMBER, String.valueOf(getCustomPage()));
		}else{
			try {
				result = scrapperUrl.replace(KEY_WORDS_KEY, URLEncoder.encode(keyWords,HTTP.UTF_8)).replace(LANGUAGE_KEY, language).replace(PAGE_NUMBER, String.valueOf(getCustomPage()));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	public String toString(){
		return getFullUrl();
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public void incAttemptCount() {
		this.attemptCount++;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public boolean isEncodeKeywords() {
		return encodeKeywords;
	}

	public void setEncodeKeywords(boolean encodeKeywords) {
		this.encodeKeywords = encodeKeywords;
	}

	public Map<String, String> getExtraParams() {
		return extraParams;
	}

	public void setExtraParams(Map<String, String> extraParams) {
		this.extraParams = extraParams;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	public int getCustomPage(){
		return this.page;
	}
	
	public boolean isUseOrigKeywords() {
		return useOrigKeywords;
	}

	public void setUseOrigKeywords(boolean useOrigKeywords) {
		this.useOrigKeywords = useOrigKeywords;
	}

	public boolean isBanPage(int respCode){
		return bannedRespCodes.contains(respCode);
	}
	
	public boolean addBannedRespCode(Integer respCode){
		return bannedRespCodes.add(respCode);
	}
	
	public Integer getRsltCnt(String strCnt) {
		return Integer.valueOf(strCnt);
	}

	public String getKeyWordsOrig() {
		return keyWordsOrig;
	}

	public ArrayList<Snippet> getSnipResult() {
		return snipResult;
	}

	public void appendSnipResult(ArrayList<Snippet> snipResult) {
		this.snipResult.addAll(snipResult);
	}
}
