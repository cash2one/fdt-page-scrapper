package com.fdt.scrapper.task;

public class GoogleSnippetTask extends SnippetTask
{
	public GoogleSnippetTask(String keyWords){
		super(keyWords);
		this.setScrapperUrl("https://www.google.com/search?q=#KEY_WORDS#&lr=lang_#LANGUAGE#&oe=utf-8&gws_rd=ssl&start=#PAGE_NUM#");
		this.setXpathSnippet("//li[@class='g']");
		////h3[@class='r']/a"
		this.setXpathTitle("h3[class=r] > a");
		////div[@class='s']//span[@class='st']
		this.setXpathDesc("div[class=s] > span[class=st]");
		this.setHost("google.com");
		this.setPage(1);
		
		this.addBannedRespCode(403);
		this.addBannedRespCode(503);
		/*this.setXpathSnipper("li[class=g]");
		this.setXpathTitle("h3[class=r] a");
		this.setXpathDesc("div[class=s] span[class=st]");*/
	}

	@Override
	protected void initExtraParams() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPage(int page) {
		switch (page){
			case 0:
			case 1:{
				page = 0;
				break;
			}
			default:{
				page = page*10;
			}
		}
	}
}
