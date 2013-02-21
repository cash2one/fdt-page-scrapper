package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.ConfigManager;

public class AccountFactory
{
    private static final Logger log = Logger.getLogger(AccountFactory.class);
    private ArrayList<Account> accounts = new ArrayList<Account>();

    public final static String MAIN_URL_LABEL = "main_url";
    public final static String LOGIN_URL_LABEL = "login_url";
    public final static String POST_NEWS_URL_LABEL = "vote_url";

    private ProxyFactory proxyFactory = null;

    public AccountFactory(ProxyFactory proxy){
	super();
	this.proxyFactory = proxy;
    }

    public void fillAccounts(String accListFilePath){
	//read account list
	FileReader fr = null;
	BufferedReader br = null;
	try {
	    fr = new FileReader(new File(accListFilePath));
	    br = new BufferedReader(fr);

	    String line = br.readLine();
	    while(line != null){
		//parse proxy adress
		if(line.contains(":")){
		    String[] account = line.trim().split(":");
		    accounts.add(new Account(account[0],account[1],proxyFactory.getProxyConnector()));
		}
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
    }

    public synchronized Account getAccount(){
	if(accounts.size() > 0){
	    return accounts.remove(0);
	}else{
	    return null;
	}
    }

    public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
	StringBuilder result = new StringBuilder();
	boolean first = true;

	for (NameValuePair pair : params)
	{
	    if (first)
		first = false;
	    else
		result.append("&");

	    result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	    result.append("=");
	    result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	}

	return result.toString();
    }
}
