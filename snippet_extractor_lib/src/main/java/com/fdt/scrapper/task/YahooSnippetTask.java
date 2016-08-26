package com.fdt.scrapper.task;

public class YahooSnippetTask extends SnippetTask
{
	public YahooSnippetTask(String keyWords){
		super(keyWords);
		this.setScrapperUrl("https://search.yahoo.com/search?p=#KEY_WORDS#&b=#PAGE_NUM#&pz=10");
		
		this.setXpathTitle("div[id=web] > ol[class*=searchCenterMiddle] > li > div[class^=dd] > div[class^=compTitle] > h3[class=title] > a");
		this.setXpathDesc("div[id=web] > ol[class*=searchCenterMiddle] > li > div[class^=dd] > div[class=compText], div[id=web] > ol > li > div > div[class=compText aAbs]");
		//this.setXpathRstlCnt("div[id=resultStats]");
		this.setHost("search.yahoo.com");
		this.setPage(1);
	}

	@Override
	public void initExtraParams() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public int getCustomPage(){
		return (getPage()-1) * 10 + 1;
	}
}
