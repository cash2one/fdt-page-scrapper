package com.fdt.scrapper.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fdt.scrapper.Domain;

public class GoogleWeekTask extends Task{
	private static final String SCRAPPER_PATTERN = "(about )?(.*) result(.*)";
	private static final String SCRAPPER_URL_PART_2 = "&hl=en&safe=off&tbo=d&tbs=qdr:w";
	private static final String SCRAPPER_URL_PART_1 = "http://www.google.com/search?q=site:";
	private static final String SCRAPPER_XPATH = "div[id=resultStats]";

	public GoogleWeekTask(Domain domain){
		super(SCRAPPER_XPATH, false);
		StringBuilder url = new StringBuilder();
		url.append(SCRAPPER_URL_PART_1).append(domain.getName()).append(SCRAPPER_URL_PART_2);
		super.setUrlToScrap(url.toString());
	}

	@Override
	public void setResult(String result) {
		result = result.toLowerCase();
		Pattern depArrHours = Pattern.compile(SCRAPPER_PATTERN);
		Matcher matcher = depArrHours.matcher(result);
		if(matcher.find()){
			this.result = matcher.group(2).replace(",", "");
		}else{
			this.result = "0";
		}
	}
}
