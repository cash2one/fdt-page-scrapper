package com.fdt.scrapper.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fdt.scrapper.Domain;

public class GoogleWeekTask extends Task{
	public GoogleWeekTask(Domain domain){
		super("div[id=resultStats]", false);
		StringBuilder url = new StringBuilder();
		url.append("http://www.google.com/search?q=site:").append(domain.getName()).append("&hl=en&safe=off&tbo=d&tbs=qdr:w");
		super.setUrlToScrap(url.toString());
	}

	@Override
	public void setResult(String result) {
		result = result.toLowerCase();
		Pattern depArrHours = Pattern.compile("(about )?(.*) result(.*)");
		Matcher matcher = depArrHours.matcher(result);
		if(matcher.find()){
			this.result = matcher.group(2).replace(",", "");
		}else{
			this.result = "0";
		}
	}
}
