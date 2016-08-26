package com.fdt.scrapper.task;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.fdt.scrapper.Domain;

public class AlexaTask extends Task {
	private static final ArrayList<String> SCRAPPER_XPATH = new ArrayList<String>(Arrays.asList("//POPULARITY/@TEXT"));
	private static final String SCRAPPER_URL = "http://data.alexa.com/data?cli=10&dat=s&url=http://";
	
	private static final Logger log = Logger.getLogger(AlexaTask.class);
	
	public AlexaTask(Domain domain){
		super(SCRAPPER_XPATH, true);
		StringBuilder url = new StringBuilder();
		url.append(SCRAPPER_URL).append(domain.getName());
		super.setUrlToScrap(url.toString());
	}

	@Override
	public boolean setResult(ArrayList<String> result) {
		log.debug("URL: " + this.getUrlToScrap() + "; RESULT = ["+result+"]");
		this.result = result;
		Integer value = Integer.parseInt(result.get(0));
		return value > 100000;
	}
}