package com.fdt.scrapper.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfigManager
{
    private static final long serialVersionUID = 1L;	
    private static ConfigManager instance = null;
    private static final Logger log = Logger.getLogger(ConfigManager.class);

    private Properties properties = new Properties();

    private ConfigManager(){
    }

    public void loadProperties(String cfgFilePath){
	synchronized (this){ 
	    
	    BufferedReader in = null;
	    
	    try {
	    	in = new BufferedReader(
	 	 		   new InputStreamReader( new FileInputStream(new File(cfgFilePath)), "UTF8"));
		properties.load(in);
	    } catch (FileNotFoundException e) {
		log.error("Reading PROPERTIES file: FileNotFoundException exception occured: " + e.getMessage());
	    } catch (IOException e) {
		log.error("Reading PROPERTIES file: IOException exception occured: " + e.getMessage());
	    } finally {
		try {
		    in.close();
		} catch (Throwable e) {
		    log.warn("Error while initializtion", e);
		}
	    }
	}
    }

    public static ConfigManager getInstance(){
	if(null == instance){
	    synchronized (ConfigManager.class) {
		if(null == instance){
		    instance = new ConfigManager();
		}
	    }
	}
	return instance;
    }

    public String getProperty(String key){
	return properties.getProperty(key);
    }
}
