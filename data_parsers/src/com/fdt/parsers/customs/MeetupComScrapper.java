package com.fdt.parsers.customs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fdt.utils.Utils;

public class MeetupComScrapper {

	private static final Logger log = Logger.getLogger(MeetupComScrapper.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME =60000L;

	public MeetupComScrapper(String cfgFilePath){

	}

	public static void main(String[] args) {
		try{
			MeetupComScrapper taskRunner = new MeetupComScrapper("config.ini");
			DOMConfigurator.configure("./log4j_data_parser.xml");

			//TODO Load countries urls
			List<String> countriesUrls = Utils.loadFileAsStrList("meetup.com/countries.txt");

			taskRunner.getCities(countriesUrls);
			Thread.sleep(RUNNER_QUEUE_EMPTY_WAIT_TIME);

			//System.out.print("Program execution finished");
			//System.exit(0);
		}catch(Throwable e){
			e.printStackTrace();
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}

	private void getCities(List<String> countriesUrls) throws IOException{

		File workingDir = new File("meetup.com");
		
		//System.setProperty("http.proxyHost", "95.211.175.225");
		//System.setProperty("http.proxyPort", "13152");

		for(String countryElem : countriesUrls){
			
			boolean cityFlg = false;
			
			String[] cntrInfo = countryElem.split(";");
			String[] cntrInfoDtl = cntrInfo[0].split("/");
			String cntrAbbr = cntrInfoDtl[4];
			String cntrUrl= cntrInfo[0];
			
			File cntrDir = new File(workingDir, cntrAbbr);
			cntrDir.mkdir();
			
			log.info(String.format("Fetching %s...", cntrInfo[0]));

			Document doc = Jsoup.connect(cntrUrl)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
					.timeout(30000).get();
			
			String cityFlag = doc.select("div[class=bounds] > h2").get(0).text().trim();
			
			cityFlg = cityFlag.contains("cities");
			
			List<String> regionsList = null;
			
			if(!cityFlg)
			{
				String regionLabel = cityFlag.split("\\s+")[0];
				new File(cntrDir,"info/info.txt").delete();
				new File(cntrDir,"info").mkdir();
				Utils.appendStringToFile(regionLabel, new File(cntrDir,"info/info.txt"));
				
				//create region folder
				new File(cntrDir,"regions").delete();
				new File(cntrDir,"regions").mkdir();
				
				//Create top regions file
				regionsList = getRegionsList(doc);
				for(String region : regionsList){
					Utils.appendStringToFile(region, new File(cntrDir, "info/regions_top.txt"));
				}
				
				doc = Jsoup.connect(cntrUrl + "?all=1")
						.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0")
						.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
						.timeout(30000).get();
				
				
				regionsList = getRegionsList(doc);
				for(String region : regionsList){
					Utils.appendStringToFile(region, new File(cntrDir, "info/regions.txt"));
					new File(new File(cntrDir,"regions"),region.split(";")[2]).mkdir();
					
					//Get all cities list of country or regions
					saveCititesList(new File(new File(cntrDir,"regions"), region.split(";")[2]), region.split(";")[0]);
				}
				
			}else{
				saveCititesList(cntrDir, cntrUrl);
			}
			
			
		}
	}
	
	/**
	 * Return list of states urls
	 * @return
	 */
	private List<String> getRegionsList(Document doc)
	{
		List<String> stateList = new ArrayList<String>();
		
		Elements stateElement = doc.select("li[class=gridList-item] > a ");
		
		for(Element regionElement : stateElement){
			String regionAbbr = regionElement.attr("href");
			String[] urlElems = regionAbbr.split("/");
			regionAbbr = urlElems[urlElems.length-1];
			stateList.add(String.format("%s;%s;%s", regionElement.attr("href"), regionElement.text().trim(), regionAbbr));
		}
		
		return stateList;
	}
	
	/**
	 * Return list of cities urls
	 * @return
	 * @throws IOException 
	 */
	private void saveCititesList(File citiesFolder, String citiesPageUrl) throws IOException{
		
		Document doc = Jsoup.connect(citiesPageUrl)
				.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0")
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
				.timeout(30000).get();
		
		Elements cities = doc.select(" li[class=gridList-item] > a ");
		
		for(Element cityElement : cities){
			String regionAbbr = cityElement.attr("href");
			String[] urlElems = regionAbbr.split("/");
			regionAbbr = urlElems[urlElems.length-1];
			Utils.appendStringToFile(String.format("%s;%s;%s", cityElement.attr("href"), cityElement.text().trim(), regionAbbr), new File(citiesFolder,"cities_top.txt"));
		}
		
		doc = Jsoup.connect(citiesPageUrl + "?all=1")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0")
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
				.timeout(30000).get();
		
		cities = doc.select(" li[class=gridList-item] > a ");
		
		for(Element cityElement : cities){
			String regionAbbr = cityElement.attr("href");
			String[] urlElems = regionAbbr.split("/");
			regionAbbr = urlElems[urlElems.length-1];
			Utils.appendStringToFile(String.format("%s;%s;%s", cityElement.attr("href"), cityElement.text().trim(), regionAbbr), new File(citiesFolder,"cities.txt"));
		}
	}
}
