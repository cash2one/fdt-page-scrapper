package com.fdt.scrapper.task;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fdt.scrapper.Domain;

public class PrTicTask extends Task {
	
	private static final ArrayList<String> SCRAPPER_XPATH = new ArrayList<String>(Arrays.asList("body"));

	private static final String SCRAPPER_URL = "http://api.pr-cy.ru/analysis.json?domain=";

	private static final Logger log = Logger.getLogger(PrTicTask.class);

	public PrTicTask(Domain domain){
		super(SCRAPPER_XPATH, false);
		StringBuilder url = new StringBuilder();
		url.append(SCRAPPER_URL).append(domain.getName());
		super.setUrlToScrap(url.toString());
	}

	@Override
	public boolean setResult(ArrayList<String> result) {
		ArrayList<String> cleanedResult = new ArrayList<String>();

		log.debug("URL: " + this.getUrlToScrap() + "; RESULT = ["+result.toArray(new String[result.size()])+"]");

		//pasre json response
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(result.get(0));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jsonObj = (JSONObject) obj;

		JSONObject stats = (JSONObject)jsonObj.get("stats");

		if(stats != null){
			cleanedResult.add(stats.get("pageRank") != null?stats.get("pageRank").toString():"");
			cleanedResult.add(stats.get("yandexCitation") != null?stats.get("yandexCitation").toString():"");
		}else{
			cleanedResult.add("");
			cleanedResult.add("");
		}

		this.result = cleanedResult;
		
		return false;
	}
}
