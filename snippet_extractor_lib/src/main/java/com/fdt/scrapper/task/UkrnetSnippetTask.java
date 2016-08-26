package com.fdt.scrapper.task;

public class UkrnetSnippetTask extends SnippetTask
{
    public UkrnetSnippetTask(String keyWords){
	super(keyWords);
	this.setScrapperUrl("https://search.ukr.net/?q=#KEY_WORDS#&cr=countryUA&lr=&as_qdr=&filter=&sitesearch=&safe=&related=&as_rq=&start=0");
	this.setXpathTitle("//section/h3[@class='title']/a");
	this.setXpathDesc("//section/div[@class='descript']/div[@class='text']");
	this.setHost("search.ukr.net");
	this.setEncodeKeywords(true);
	//this.setXpathTitle("//div[@class='list']/ol[@class='results']/li/div[@class='title']/a");
	//this.setXpathDesc("//div[@class='list']/ol[@class='results']/li/div[@class='text']");
	/*this.setXpathSnipper("li[class=g]");
	this.setXpathTitle("h3[class=r] a");
	this.setXpathDesc("div[class=s] span[class=st]");*/
    }

	@Override
	public void initExtraParams() {
		// TODO Auto-generated method stub
		
	}
}
