package com.fdt.scrapper.task;

public abstract class SnippetTask
{
	public final static String KEY_WORDS_KEY = "#KEY_WORDS#";
	public final static String LANGUAGE_KEY = "#LANGUAGE#";

	private String scrapperUrl = "";
	private String xpathSnippets = "";
	private String xpathTitle = "";
	private String xpathDesc = "";
	private String keyWords = "";
	private String language = "en";

	private int attemptCount = 1;

	private String result = null;

	public SnippetTask(String keyWords)
	{
		super();
		this.keyWords = keyWords.replace(' ', '+');
	}

	public SnippetTask()
	{
	}
	
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
		this.keyWords = keyWords;
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

	public String getFullUrl(){
		return scrapperUrl.replace(KEY_WORDS_KEY, keyWords).replace(LANGUAGE_KEY, language);
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
}
