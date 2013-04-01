package com.fdt.scrapper.task;

public class TutSnippetTask extends SnippetTask
{
    public TutSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("http://search.tut.by/?rs=1&page=0&query=#KEY_WORDS#&how=rlv&ru=1&tc=0&ust=#KEY_WORDS#&sh=&cg=1&cdig=1");
	this.setXpathTitle("//li[@class='b-results__li']/h3/a[2]");
	this.setXpathDesc("//li[@class='b-results__li']/p[1]");
	this.setXpathLink("//li[@class='b-results__li']/h3/a[2]/@href");
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }
}
