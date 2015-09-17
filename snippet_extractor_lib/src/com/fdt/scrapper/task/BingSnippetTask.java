package com.fdt.scrapper.task;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BingSnippetTask extends SnippetTask
{
    public BingSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("http://www.bing.com/search?&qs=n&sc=8-4&sp=-1&sk=&q=#KEY_WORDS#&first=#PAGE_NUM#&FORM=PERE&filt=all");
	////li[@class='b_algo']//h2/a
	this.setXpathTitle("li[class=b_algo] > h2 > a, li[class=b_algo] > div[class=b_title] > h2 > a");
	////div[@class='b_caption']/p | //div[@class='b_snippet']/p | //div[@class='b_caption b_rich']/div/p
	this.setXpathDesc("li[class=b_algo] > div[class=b_caption] > p, li[class=b_algo] > div[class=b_caption] > div[class=b_snippet] > p, li[class=b_algo] > div.b_caption.b_rich > div > p");
	this.setHost("bing.com");
	this.setPage(1);
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }

    @Override
    public String getFullUrl(){
	String result = "";
	try
	{
	    result = this.scrapperUrl.replace(KEY_WORDS_KEY, URLEncoder.encode(keyWordsNative,"UTF-8")).replace(LANGUAGE_KEY, language).replace(PAGE_NUMBER, String.valueOf(1+10*(page-1)));
	}
	catch (UnsupportedEncodingException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return result;
    }

	@Override
	protected void initExtraParams() {
		// TODO Auto-generated method stub
		
	}
}
