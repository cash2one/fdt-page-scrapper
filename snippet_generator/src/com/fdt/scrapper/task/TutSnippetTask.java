package com.fdt.scrapper.task;

public class TutSnippetTask extends SnippetTask
{
    public TutSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("http://search.tut.by/?status=1&ru=1&encoding=1&page=0&how=rlv&query=#KEY_WORDS#");
	this.setXpathTitle("//li[@class='b-results__li']/h3/a[2]");
	this.setXpathDesc("//li[@class='b-results__li']/p[1]");
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }
}
