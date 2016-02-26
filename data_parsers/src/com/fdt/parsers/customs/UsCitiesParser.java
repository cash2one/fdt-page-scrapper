package com.fdt.parsers.customs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fdt.scrapper.task.ConfigManager;

public class UsCitiesParser 
{

	private static final Logger log = Logger.getLogger(UsCitiesParser.class);
	
	private static final String ROOT_DIR = "root_dir";
	private static final String OUTPUT_CITY_FILE_PATH = "output_city_file_path";
	private static final String OUTPUT_NEIGHBOR_CITY_FILE_PATH = "output_neighbor_city_file_path";
	
	private static final String LINE_FEED = "\r\n";
	
	private File rootDir;
	private File outputCityFile;
	private File outputNeighborCityFile;
	
	private List<City> allCities = new ArrayList<City>();
	
	public static void main(String[] args) throws Exception{
		if(args.length < 1){
			System.out.print("Not enought arguments....");
		}else{
			System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
			ConfigManager.getInstance().loadProperties(args[0]);
			System.out.println(args[0]);
			
			DOMConfigurator.configure("log4j_data_parser.xml");

			UsCitiesParser taskRunner = null;
			try {
				taskRunner = new UsCitiesParser(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			taskRunner.executeWrapper();
		}
	}
	
	public UsCitiesParser(String cfgFilePath) throws Exception{

		ConfigManager.getInstance().loadProperties(cfgFilePath);
		rootDir = new File(ConfigManager.getInstance().getProperty(ROOT_DIR));
		outputCityFile = new File(ConfigManager.getInstance().getProperty(OUTPUT_CITY_FILE_PATH));
		outputNeighborCityFile = new File(ConfigManager.getInstance().getProperty(OUTPUT_NEIGHBOR_CITY_FILE_PATH));
		
		outputCityFile.delete();
		outputNeighborCityFile.delete();
	}
	
	private void executeWrapper() throws Exception
	{
		for(File cityDir : rootDir.listFiles()){
			if(cityDir.isDirectory()){
				
				//Processing city dir
				for(File cityFile : cityDir.listFiles()){
					processingFile(cityFile);
				}
			}
		}
		
		//TODO Processing all cities here
		
		for(City city : allCities){
			appendLineToFile(city.toString(), outputCityFile);
		}
		
		for(City mainCity : allCities){
			for(City neighbor : mainCity.getNeigborCity()){
				String insertSrt =  String.format(" INSERT neighbor_city (city_id, neighbor_city_id, upd_dt)  " +
						" SELECT  " +
						" (SELECT c.city_id FROM city c, region r WHERE c.region_id = r.region_id AND c.city_name = '%s' AND r.abbr = '%s'), " +
						" (SELECT c.city_id FROM city c, region r WHERE c.region_id = r.region_id AND c.city_name = '%s' AND r.abbr = '%s'), " +
						" now(); ",mainCity.getCityName(), mainCity.getRegionLocal(), neighbor.getCityName(), neighbor.getRegionLocal());
				appendLineToFile(insertSrt, outputNeighborCityFile);
			}
		}
	}
	
	private void processingFile(File cityDirFile) throws FileNotFoundException, IOException{
		Document html = null;
		String htmlStr = getResponseAsString(new FileInputStream(cityDirFile)).toString();
		html = Jsoup.parse(htmlStr);
		
		City city = new City();
		
		String placename = html.select("meta[name=geo.placename]").get(0).attr("content").trim();
		String position = html.select("meta[name=geo.position]").get(0).attr("content").trim();
		String regionFull = html.select("meta[name=geo.region]").get(0).attr("content").trim();
		String regionLocal = regionFull.substring(regionFull.indexOf("-") + 1).trim();
		String ICBM = html.select("meta[name=ICBM]").get(0).attr("content").trim();
		String cityName = toFirstUpperLetter(placename.substring(0, placename.indexOf(","))).trim();
		
		city.setGeoPlacename(placename);
		city.setGeoPosition(position);
		city.setRegionLocal(regionLocal);
		city.setGeoRegion(regionFull);
		city.setICBM(ICBM);
		city.setCityName(cityName);
		
		//TODO Get neighbor list
		Elements neighborCities =  html.select("div[class=post] > ul > li > a");
		for(Element neiCity : neighborCities){
			String neiCityStr = neiCity.text().trim();
			String cityNeiName = toFirstUpperLetter(neiCityStr.substring(0, neiCityStr.indexOf(","))).trim();
			String regionNeiName = neiCityStr.substring(neiCityStr.indexOf(",")+1).trim();
			City neiCityItem = new City();
			neiCityItem.setCityName(cityNeiName);
			neiCityItem.setRegionLocal(regionNeiName);
			
			city.addNeighborCity(neiCityItem);
		}
		
	
		allCities.add(city);
	}
	
	private StringBuilder getResponseAsString(InputStream is)
			throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder responseStr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			responseStr.append(line).append(LINE_FEED);
		}
		is.close();
		return responseStr;
	}
	
	private class City
	{
		private String cityName;
		private String regionLocal;
		private String geoPlacename;
		private String geoPosition;
		private String ICBM;
		private String geoRegion;
		private String mapString;
		
		private List<City> neigborCity = new ArrayList<City>();
		
		public City() {
			super();
		}
		public String getCityName() {
			return cityName;
		}
		public void setCityName(String cityName) {
			this.cityName = cityName;
		}
		public String getGeoPlacename() {
			return geoPlacename;
		}
		public void setGeoPlacename(String geoPlacename) {
			this.geoPlacename = geoPlacename;
		}
		public String getGeoPosition() {
			return geoPosition;
		}
		public void setGeoPosition(String geoPosition) {
			this.geoPosition = geoPosition;
		}
		public String getICBM() {
			return ICBM;
		}
		public void setICBM(String iCBM) {
			ICBM = iCBM;
		}
		public String getGeoRegion() {
			return geoRegion;
		}
		public void setGeoRegion(String geoRegion) {
			this.geoRegion = geoRegion;
		}
		public String getMapString() {
			return mapString;
		}
		public void setMapString(String mapString) {
			this.mapString = mapString;
		}
		public String getRegionLocal() {
			return regionLocal;
		}
		public void setRegionLocal(String regionLocal) {
			this.regionLocal = regionLocal;
		}
		
		public void addNeighborCity(City city){
			neigborCity.add(city);
		}
		
		public List<City> getNeigborCity() {
			return neigborCity;
		}
		
		@Override
		public String toString() {
			String cityNameCorrected = toUpperFirstLetters(cityName);
			String[] coordinate = geoPosition.split(";");
			return String.format("INSERT city " + 
								"(region_id, city_name, city_name_latin, geo_placename, geo_position, geo_region, ICBM, zip_code, country, lat, lng) " + 
								" SELECT region_id,'%s','%s','%s','%s','%s','%s','%s','%s',%s,%s FROM region WHERE abbr = '%s' ON DUPLICATE KEY UPDATE lat = %s, lng = %s;",
								cityNameCorrected, cityNameCorrected.replaceAll("\\s+", "-"), geoPlacename, geoPosition, 
								geoRegion,ICBM, geoPlacename.replaceAll("[^0-9]*", "").trim(), geoPlacename.split(",")[1].trim(), 
								coordinate[0],coordinate[1], regionLocal, coordinate[0],coordinate[1]);
		}
	}
	
	private String toUpperFirstLetters(String input){
		StringBuffer strBuf = new StringBuffer();
		for(String word : input.split(" ")){
			strBuf.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
		}
		
		if(strBuf.length() > 0){
			strBuf.setLength(strBuf.length()-1);
		}
		
		return strBuf.toString();
	}
	
	private void appendLineToFile(String str, File file) throws IOException {

		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file,true), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();

		} finally {
			//Close the BufferedWriter
			if (bufferedWriter != null) {
				bufferedWriter.flush();
				bufferedWriter.close();
			}
		}
	}
	
	private String toFirstUpperLetter(String input){
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(input.substring(0,1).toUpperCase()).append(input.substring(1).toLowerCase());
		
		return strBuf.toString();
	}
}
