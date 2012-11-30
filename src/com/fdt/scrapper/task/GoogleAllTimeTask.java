package com.fdt.scrapper.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fdt.scrapper.Domain;

public class GoogleAllTimeTask extends Task {
	public GoogleAllTimeTask(Domain domain){
		super("div[id=resultStats]", false);
		StringBuilder url = new StringBuilder();
		url.append("http://www.google.com/search?hl=en&safe=off&q=site:").append(domain.getName()).append("&btnG=");
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
