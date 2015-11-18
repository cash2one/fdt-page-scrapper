package com.fdt.scrapper.task;

public class TutSnippetTask extends SnippetTask
{
    public TutSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("http://search.tut.by/?status=1&encoding=1&page=#PAGE_NUM#&how=rlv&query=#KEY_WORDS#");
	this.setXpathTitle("//li[@class='b-results__li']/h3/a[2]");
	this.setXpathDesc("//li[@class='b-results__li']/p[1]");
	this.setHost("search.tut.by");
	this.setEncodeKeywords(true);
	this.setPage(1);
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }

	@Override
	public void initExtraParams() {
		extraParams.put("Cookie", "__uguid=0;");
	}
}
