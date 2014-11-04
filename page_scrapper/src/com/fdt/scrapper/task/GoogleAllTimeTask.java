package com.fdt.scrapper.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fdt.scrapper.Domain;

public class GoogleAllTimeTask extends Task {
	private static final String EXTRACT_PATTERN = "(about )?(.*) result(.*)";
	private static final String SCRAPPER_URL_PART_2 = "&btnG=&gws_rd=ssl";
	private static final String SCRAPPER_URL_PART_1 = "https://www.google.com/search?hl=en&safe=off&q=site:";
	private static final String SCRAPPER_XPATH = "div[id=resultStats]";
	
	private static final Logger log = Logger.getLogger(GoogleAllTimeTask.class);

	public GoogleAllTimeTask(Domain domain){
		super(SCRAPPER_XPATH, false);
		StringBuilder url = new StringBuilder();
		url.append(SCRAPPER_URL_PART_1).append(domain.getName()).append(SCRAPPER_URL_PART_2);
		super.setUrlToScrap(url.toString());
	}

	@Override
	public void setResult(String result) {
		log.debug("URL: " + this.getUrlToScrap() + "; RESULT = ["+result+"]");
		result = result.toLowerCase();
		Pattern depArrHours = Pattern.compile(EXTRACT_PATTERN);
		Matcher matcher = depArrHours.matcher(result);
		if(matcher.find()){
			this.result = matcher.group(2).replace(",", "");
		}else{
			this.result = "0";
		}
	}
}
