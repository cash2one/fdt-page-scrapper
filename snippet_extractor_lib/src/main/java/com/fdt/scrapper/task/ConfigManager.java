package com.fdt.scrapper.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
@Scope("singleton")
public class ConfigManager
{
	private static final Logger log = Logger.getLogger(ConfigManager.class);
	
	private static ConfigManager instance = null;

	private Properties properties = new Properties();

	public ConfigManager(){
		super();
	}
	
	@PostConstruct
	public void loadProperties(){
		ConfigManager.instance = this;
		synchronized (this){ 
			InputStream is = null;
			try {
				File configFile = new File("./config.ini");
				if(!configFile.exists()){
					configFile = new File(System.getProperty("config.file"));
				}
				is = new FileInputStream(configFile);
				//is = new FileInputStream(ClassLoader.getSystemClassLoader().getResource(System.getProperty("config.file")).getFile());
				properties.load(is);
				
			} catch (FileNotFoundException e) {
				log.error("Reading PROPERTIES file: FileNotFoundException exception occured: " + e.getMessage());
			} catch (IOException e) {
				log.error("Reading PROPERTIES file: IOException exception occured: " + e.getMessage());
			} finally {
				try {
					if(is != null){
						is.close();
					}
				} catch (Throwable e) {
					log.warn("Error while initializtion", e);
				}
			}
		}
	}
	
	public static ConfigManager getInstance(){
		return instance;
	}

	public String getProperty(String key){
		return properties.getProperty(key);
	}
	
	public String getProperty(String key, String defValue){
		return properties.getProperty(key, defValue);
	}
}
