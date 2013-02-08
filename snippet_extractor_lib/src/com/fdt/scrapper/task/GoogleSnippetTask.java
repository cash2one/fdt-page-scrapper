package com.fdt.scrapper.task;

public class GoogleSnippetTask extends SnippetTask
{
    public GoogleSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("http://www.google.com/search?q=#KEY_WORDS#&lr=lang_#LANGUAGE#&ie=utf-8&oe=utf-8");
	this.setXpathSnippet("//li[@class='g']");
	this.setXpathTitle("//h3[@class='r']/a");
	this.setXpathDesc("//div[@class='s']//span[@class='st']");
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }
}
