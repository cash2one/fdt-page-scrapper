package com.ssa.rent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfigurationParser
{
    private static final Logger log = Logger.getLogger(ConfigurationParser.class);

    private String siteName = "";
    private Properties props = new Properties();
    private HashMap<PageElement, ElementLocation> parserParams = new HashMap<PageElement, ElementLocation>();

    public ConfigurationParser(String siteName, String cfgFilePath){
	this.siteName = siteName;
	this.props = loadProperties(cfgFilePath);
	fillParserParams(props);
    }
    
    private void fillParserParams(Properties props){
	for(PageElement element:PageElement.values()){
	    parserParams.put(element, new ElementLocation(element,props.getProperty(element.toString())));
	}
    }
    
    public ElementLocation getElementLocation(PageElement element){
	return parserParams.get(element);
    }

    public class ElementLocation {
	PageElement element;
	String elementXPath;

	public ElementLocation(PageElement element, String elementXPath) {
	    super();
	    this.element = element;
	    this.elementXPath = elementXPath;
	}

	public PageElement getElement()
	{
	    return element;
	}

	public void setElement(PageElement element)
	{
	    this.element = element;
	}

	public String getElementXPath()
	{
	    return elementXPath;
	}

	public void setElementXPath(String elementXPath)
	{
	    this.elementXPath = elementXPath;
	}
    }

    public Properties loadProperties(String cfgFilePath){
	Properties properties = new Properties();
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
	return properties;
    }
}
