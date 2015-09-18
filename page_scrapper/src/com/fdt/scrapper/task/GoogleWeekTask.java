package com.fdt.scrapper.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fdt.scrapper.Domain;

public class GoogleWeekTask extends Task{
	private static final String SCRAPPER_PATTERN = "(about )?(.*) result(.*)";
	private static final String SCRAPPER_URL_PART_2 = "&lr=lang_en&tbo=d&tbs=qdr:w&oe=utf-8&gws_rd=ssl";
	private static final String SCRAPPER_URL_PART_1 = "https://www.google.com/search?q=site:";
	private static final ArrayList<String> SCRAPPER_XPATH = new ArrayList<String>(Arrays.asList("div[id=resultStats]"));
	
	private static final Logger log = Logger.getLogger(GoogleWeekTask.class);

	public GoogleWeekTask(Domain domain){
		super(SCRAPPER_XPATH, false);
		StringBuilder url = new StringBuilder();
		url.append(SCRAPPER_URL_PART_1).append(domain.getName()).append(SCRAPPER_URL_PART_2);
		super.setUrlToScrap(url.toString());
	}

	@Override
	public boolean setResult(ArrayList<String> result) {
		String resultStr = result.get(0);
		log.debug("URL: " + this.getUrlToScrap() + "; RESULT = ["+resultStr+"]");
		resultStr = resultStr.toLowerCase();
		Pattern depArrHours = Pattern.compile(SCRAPPER_PATTERN);
		Matcher matcher = depArrHours.matcher(resultStr);
		if(matcher.find()){
			this.result = new ArrayList<String>(Arrays.asList(matcher.group(2).replace(",", "")));
		}else{
			this.result = new ArrayList<String>(Arrays.asList("0"));
		}
		
		return false;
	}
}
