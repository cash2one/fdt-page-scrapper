package com.fdt.keyextractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class KeyExtractor
{
    private static final Logger log = Logger.getLogger(KeyExtractor.class);

    private static final String KEY_LIST_PATH = "keys.txt";
    private static final String REGION_LIST_PATH = "regions.txt";

    private ArrayList<String> keyList = new ArrayList<String>();
    private ArrayList<String> regionList = new ArrayList<String>();

    private ArrayList<String> regionResultKeysWithRegion = new ArrayList<String>();
    private ArrayList<String> regionResultKeysWithoutRegion = new ArrayList<String>();
    private ArrayList<String> nonRegionResultKeys = new ArrayList<String>();

    public static void main(String[] args){
	KeyExtractor keyExtractor = new KeyExtractor();
	keyExtractor.runProcess();
	/*StringBuffer str = null;
	String[] keyElements = "получить займ от частного инвестора".split(" ");
	for(int i = keyElements.length; i > 0; i--){
	    for(int j = 0; j <= keyElements.length-i; j++){
		str = new StringBuffer();
		for(int k = j; k < j+i; k++){
		    str.append(keyElements[k]).append(" ");
		}
		System.out.println(str.toString());
	    }
	}*/
    }

    private void runProcess(){
	keyList = readFile(KEY_LIST_PATH);
	regionList = readFile(REGION_LIST_PATH);

	boolean keyContainsRegion = false;
	String keyWithoutRegion = "";

	int keyIndex = 0;
	for(String key : keyList){
	    System.out.println(keyIndex++);
	    String[] keyElements = key.split(" ");
	    keyContainsRegion = false;
	    StringBuffer str = null;

	    /*for(int i = 2; i > 0; i--){
		for(int j = 0; j <= keyElements.length-i; j++){
		    str = new StringBuffer();
		    for(int k = j; k < j+i; k++){
			str.append(keyElements[k]).append(" ");
		    }
		    if(str.length() > 0){
			str.setLength(str.length()-1);
		    }
		    if(regionList.contains(str.toString())){
			keyContainsRegion = true;
			keyWithoutRegion = key.replaceAll(str.toString(), "");
			break;
		    }
		    if(keyContainsRegion){
			break;
		    }
		}
		if(keyContainsRegion){
		    break;
		}
	    }*/

	    for(String region : regionList){
		if(key.contains(region)){
		    //key contains region name
		    keyContainsRegion = true;
		    keyWithoutRegion = key.replaceAll(region, "");
		    break;
		}
	    }

	    if(keyContainsRegion){
		regionResultKeysWithRegion.add(key);
		regionResultKeysWithoutRegion.add(keyWithoutRegion);
	    }else{
		nonRegionResultKeys.add(key);
	    }
	}
	saveKeys(regionResultKeysWithRegion,"_regionResultKeysWithRegion.txt");
	saveKeys(regionResultKeysWithoutRegion,"_regionResultKeysWithoutRegion.txt");
	saveKeys(nonRegionResultKeys,"_nonRegionResultKeys.txt");
    }

    private ArrayList<String> readFile(String fileName){
	ArrayList<String> keyList = new ArrayList<String>();
	FileReader fr = null;
	BufferedReader br = null;
	try {
	    fr = new FileReader(new File(fileName));
	    br = new BufferedReader(fr);

	    String line = br.readLine();
	    while(line != null){
		keyList.add(line.trim().toLowerCase());
		line = br.readLine();
	    }
	} catch (FileNotFoundException e) {
	    log.error("Reading PROPERTIES file: FileNotFoundException exception occured",e);
	} catch (IOException e) {
	    log.error("Reading PROPERTIES file: IOException exception occured", e);
	} finally {
	    try {
		if(br != null)
		    br.close();
	    } catch (Throwable e) {
		log.warn("Error while initializtion", e);
	    }
	    try {
		if(fr != null)
		    fr.close();
	    } catch (Throwable e) {
		log.warn("Error while initializtion", e);
	    }
	}
	return keyList;
    }

    public void saveKeys(ArrayList<String> keys, String fileName){
	BufferedWriter bufferedWriter = null;

	try {

	    bufferedWriter = new BufferedWriter(bufferedWriter = new BufferedWriter(
		    new OutputStreamWriter(
			    new FileOutputStream(fileName), 
		    "UTF8")
	    )
	    );

	    for(String key : keys){
		bufferedWriter.write(key);
		bufferedWriter.newLine();
	    }
	} catch (FileNotFoundException e) {
	    log.error("Reading PROPERTIES file: FileNotFoundException exception occured",e);
	} catch (IOException e) {
	    log.error("Reading PROPERTIES file: IOException exception occured", e);
	} finally {

	    try {
		if (bufferedWriter != null) {
		    bufferedWriter.flush();
		    bufferedWriter.close();
		}
	    } catch (IOException ex) {
		log.error("Error occured during closing output streams during saving Success results",ex);
	    }

	}
    }
}
