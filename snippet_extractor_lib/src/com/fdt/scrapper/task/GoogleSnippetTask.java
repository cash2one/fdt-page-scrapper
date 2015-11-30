package com.fdt.scrapper.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GoogleSnippetTask extends SnippetTask
{
	public GoogleSnippetTask(String keyWords){
		super(keyWords);
		this.setScrapperUrl("https://www.google.com/search?q=#KEY_WORDS#&lr=lang_#LANGUAGE#&oe=utf-8&gws_rd=ssl&start=#PAGE_NUM#");
		this.setXpathSnippet("li[class=g]");
		////div[@class='srg']/div/div/h3[@class='r']/a"
		this.setXpathTitle("div[class=srg] > div > div > h3[class=r] > a");
		////div[@class='srg']/div/div/div[@class='s']/div/span[@class='st']
		this.setXpathDesc("div[class=srg] > div > div > div[class=s] > div > span[class=st]");
		////div[@id='resultStats']
		this.setXpathRstlCnt("div[id=resultStats]");
		this.setHost("google.com");
		this.setPage(1);
		
		//this.addBannedRespCode(403);
		//this.addBannedRespCode(503);
		/*this.setXpathSnipper("li[class=g]");
		this.setXpathTitle("h3[class=r] a");
		this.setXpathDesc("div[class=s] span[class=st]");*/
	}

/*	@Override
	public String getFullUrl(){
		String result = "";
		if(!isEncodeKeywords()){
			result = scrapperUrl.replace(KEY_WORDS_KEY, keyWords).replace(LANGUAGE_KEY, language).replace(PAGE_NUMBER, String.valueOf(10*(page-1)));
		}else{
			try {
				result = scrapperUrl.replace(KEY_WORDS_KEY, URLEncoder.encode(keyWords,HTTP.UTF_8)).replace(LANGUAGE_KEY, language).replace(PAGE_NUMBER, String.valueOf(10*(page-1)));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}*/
	
	@Override
	public int getCustomPage(){
		return (getPage()-1) * 10;
	}

	@Override
	protected void initExtraParams() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Integer getRsltCnt(String result) {
		String EXTRACT_PATTERN = "(about )?(.*) result(.*)";
		Pattern depArrHours = Pattern.compile(EXTRACT_PATTERN);
		Matcher matcher = depArrHours.matcher(result.toLowerCase());
		if(matcher.find()){
			String test = matcher.group(1);
			test = matcher.group(2);
			test = matcher.group(3);
			return Integer.valueOf(matcher.group(2).replace(",", "").replace(" ", "").replace(".", ""));
		}else{
			return -1;
		}
	}
}
