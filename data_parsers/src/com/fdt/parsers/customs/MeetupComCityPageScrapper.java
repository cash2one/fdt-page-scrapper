package com.fdt.parsers.customs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fdt.utils.Utils;

public class MeetupComCityPageScrapper {

	private static final Logger log = Logger.getLogger(MeetupComCityPageScrapper.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME =60000L;

	private static final File projectDir = new File("meetup.com");

	//Check for error countries
	private final File successCities = new File("success_saved_ciites.txt");

	private AtomicInteger currentThreadCount = new AtomicInteger(0);

	public MeetupComCityPageScrapper(String cfgFilePath){

	}

	public static void main(String[] args) {
		try{
			MeetupComCityPageScrapper taskRunner = new MeetupComCityPageScrapper("config.ini");
			DOMConfigurator.configure("./log4j_data_parser.xml");

			ArrayList<String> cityUrls = new ArrayList<String>();

			//Load cities urls list
			for(File country : projectDir.listFiles())
			{
				if(country.isDirectory())
				{

					boolean isRegionsExist = country.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							if(dir.isDirectory() && name.equals("regions")){
								return true;
							}
							return false;
						}
					}).length > 0;

					if(isRegionsExist){
						for(File region : new File(country,"regions").listFiles()){
							cityUrls.addAll(Utils.loadFileAsStrList(new File(region,"cities.txt")));
						}
					}else{
						//Read cities_top.txt
						cityUrls.addAll(Utils.loadFileAsStrList(new File(country,"cities.txt")));
					}
				}
			}

			File cityList = new File("cities_all_list.txt");
			cityList.delete();
			for(String line : cityUrls){
				Utils.appendStringToFile(line, cityList);
			}

			taskRunner.scrapCities(cityUrls);
			//Thread.sleep(RUNNER_QUEUE_EMPTY_WAIT_TIME);

			//System.out.print("Program execution finished");
			//System.exit(0);
		}catch(Throwable e){
			e.printStackTrace();
			log.error("Error during main stream",e);
			System.out.print("Program execution finished with errors");
		}
	}

	private void scrapCities(List<String> citiesUrls) throws IOException, InterruptedException{

		System.setProperty("http.proxyHost", "95.211.175.225");
		System.setProperty("http.proxyPort", "13152");

		List<String> successCitiesList = null;
		if(successCities.exists()){
			successCitiesList = Utils.loadFileAsStrList(successCities);
		}

		//if error countries exist
		if(successCitiesList != null && successCitiesList.size() > 0){
			citiesUrls.removeAll(successCitiesList);
		}

		ExecutorService se = Executors.newFixedThreadPool(50);

		for(String countryStr : citiesUrls){
			CountryThread ct = new CountryThread(countryStr);
			se.execute(ct);
		}

		while(currentThreadCount.get() > 0){
			synchronized (this) {
				wait(10000L);
			}
		}

		se.shutdown();

		while(!se.awaitTermination(60, TimeUnit.SECONDS)){
		}

	}

	private class CountryThread implements Runnable{

		private String countryStr;

		public CountryThread(String countryStr){
			this.countryStr = countryStr;
		}

		@Override
		public void run() 
		{
			try{
				currentThreadCount.incrementAndGet();
				processCityStr(countryStr);
			}finally{
				currentThreadCount.decrementAndGet();
			}
		}

		private void processCityStr(String countryUrl){
			boolean isSuccess = false;

			while(!isSuccess){
				try
				{
					//split country string
					String[] cntrInfo = countryUrl.split(";");
					//split country url
					//country url
					String cntrUrl= cntrInfo[0];
					File file4Save = new File(projectDir, cntrInfo[0].substring("http://www.meetup.com/cities/".length(),cntrInfo[0].length()-1) + ".html");

					log.info(String.format("Fetching %s", cntrInfo[0]));

					Document doc = Jsoup.connect(cntrUrl)
							.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0")
							.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
							.timeout(30000).get();
					
					File dir4Save = file4Save.getParentFile();
					if(!dir4Save.exists()){
						dir4Save.mkdir();
					}

					file4Save.delete();
					Utils.appendStringToFile(doc.toString(), file4Save);
					isSuccess = true;

					Utils.appendStringToFile(countryUrl, successCities);
				}catch(Throwable e){
				}
			}
		}
	}
}
