package com.fdt.scrapper.task;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BingSnippetTask extends SnippetTask
{
    public BingSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("http://www.bing.com/search?q=#KEY_WORDS#");
	this.setXpathTitle("//div[@class='sb_tlst']/h3/a");
	this.setXpathDesc("//div[@class='sa_mc']/p");
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }

    @Override
    public String getFullUrl(){
	String result = "";
	try
	{
	    result = this.scrapperUrl.replace(KEY_WORDS_KEY, URLEncoder.encode(keyWordsNative,"UTF-8")).replace(LANGUAGE_KEY, language);
	}
	catch (UnsupportedEncodingException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return result;
    }
}
