package com.fdt.scrapper.task;

public class UkrnetSnippetTask extends SnippetTask
{
    public UkrnetSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("http://search.ukr.net/yandex/search.php?search_mode=ordinal&lang=ru&engine=1&q=#KEY_WORDS#");
	this.setXpathTitle("//div[@class='list']/ol[@class='results']/li");
	this.setXpathDesc("//div[@class='list']/ol[@class='results']/li/div[@class='text']");
	//this.setXpathTitle("//div[@class='list']/ol[@class='results']/li/div[@class='title']/a");
	//this.setXpathDesc("//div[@class='list']/ol[@class='results']/li/div[@class='text']");
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }
}
