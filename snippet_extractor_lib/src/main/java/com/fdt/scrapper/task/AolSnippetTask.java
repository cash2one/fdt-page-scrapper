package com.fdt.scrapper.task;

public class AolSnippetTask extends SnippetTask
{
	public AolSnippetTask(String keyWords){
		super(keyWords);
		this.setScrapperUrl("http://search.aol.com/aol/search?q=#KEY_WORDS#&page=#PAGE_NUM#");

		this.setXpathTitle("h3[class=hac] > a, h4[class=hac] > a, p[class=v_title] > a");
		this.setXpathDesc("p[property=f:desc], div[class=videoDesc]");
		//this.setXpathRstlCnt("div[id=resultStats]");
		this.setHost("search.aol.com");
		this.setPage(1);
	}

	@Override
	public void initExtraParams() {
		// TODO Auto-generated method stub
		extraParams.put("Upgrade-Insecure-Requests", "1");
	}
}
