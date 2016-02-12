package com.fdt.jimbo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config
{
	private static final long serialVersionUID = 1L;	
	private static Config instance = null;
	private static final Logger log = Logger.getLogger(Config.class);

	private Properties properties = new Properties();

	private Config(){
	}

	public void loadProperties(String cfgFilePath){
		synchronized (this){ 
			InputStream is = null;
			try {
				is = new FileInputStream(new File(cfgFilePath));
				properties.load(is);
			} catch (FileNotFoundException e) {
				log.error("Reading PROPERTIES file: FileNotFoundException exception occured: " + e.getMessage());
			} catch (IOException e) {
				log.error("Reading PROPERTIES file: IOException exception occured: " + e.getMessage());
			} finally {
				try {
					is.close();
				} catch (Throwable e) {
					log.warn("Error while initializtion", e);
				}
			}
		}
	}

	public static Config getInstance(){
		if(null == instance){
			synchronized (Config.class) {
				if(null == instance){
					instance = new Config();
				}
			}
		}
		return instance;
	}

	public String getProperty(String key){
		return properties.getProperty(key);
	}
	
	public String getProperty(String key, String defValue){
		return properties.getProperty(key, defValue);
	}
}
