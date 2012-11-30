package com.fdt.scrapper.task;

import com.fdt.scrapper.Domain;

public class AlexaTask extends Task {
	public AlexaTask(Domain domain){
		super("//POPULARITY/@TEXT", true);
		StringBuilder url = new StringBuilder();
		url.append("http://data.alexa.com/data?cli=10&dat=s&url=http://").append(domain.getName());
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
