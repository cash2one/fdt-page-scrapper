package com.fdt.scrapper.task;

public class GoogleSnippetTask extends SnippetTask
{
    public GoogleSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("https://www.google.com/search?q=#KEY_WORDS#&lr=lang_#LANGUAGE#&oe=utf-8&gws_rd=ssl");
	this.setXpathSnippet("//li[@class='g']");
	this.setXpathTitle("//h3[@class='r']/a");
	this.setXpathDesc("//div[@class='s']//span[@class='st']");
	this.setHost("google.com");
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }

	@Override
	protected void initExtraParams() {
		// TODO Auto-generated method stub
		
	}
}
