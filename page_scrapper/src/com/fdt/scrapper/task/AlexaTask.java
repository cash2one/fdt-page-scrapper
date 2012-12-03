package com.fdt.scrapper.task;

import com.fdt.scrapper.Domain;

public class AlexaTask extends Task {
	private static final String SCRAPPER_XPATH = "//POPULARITY/@TEXT";
	private static final String SCRAPPER_URL = "http://data.alexa.com/data?cli=10&dat=s&url=http://";

	public AlexaTask(Domain domain){
		super(SCRAPPER_XPATH, true);
		StringBuilder url = new StringBuilder();
		url.append(SCRAPPER_URL).append(domain.getName());
		super.setUrlToScrap(url.toString());
	}

	@Override
	public void setResult(String result) {
		if(result != null){
			this.result = result;
		}
		else{
			this.result = "-1";
		}
	}
}
