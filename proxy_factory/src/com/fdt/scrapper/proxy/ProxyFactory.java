package com.fdt.scrapper.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

public class ProxyFactory 
{
	private static final Logger log = Logger.getLogger(ProxyFactory.class);
	private static ProxyFactory instance = null;
	private static Random rand = new Random();
	/**Delay in ms*/
	public static long DELAY_FOR_PROXY = 10000L;
	//proxy_connector
	private ArrayList<ProxyConnector> proxyList = new ArrayList<ProxyConnector>();
	//release_date
	private ArrayList<Long> proxyDelay = new ArrayList<Long>();

	private ProxyFactory() {
		super();
	}

	public void init(String pathToProxyList){
		proxyList = loadProxyList(pathToProxyList);
	}


	/**
	 * Singleton for TaskFactory
	 * 
	 * @return
	 */
	public static ProxyFactory getInstance(){
		if(null == instance){
			synchronized (ProxyFactory.class) {
				if(null == instance){
					instance = new ProxyFactory(); 
				}
			}
		}
		return instance;
	}

	private synchronized ArrayList<ProxyConnector> loadProxyList(String cfgFilePath){
		ArrayList<ProxyConnector> proxyList = new ArrayList<ProxyConnector>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(new File(cfgFilePath));
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				//parse proxy adress
				if(line.contains(":")){
					String[] proxy = line.trim().split(":");
					ProxyConnector proxyConnector = new ProxyConnector(proxy[0], Integer.valueOf(proxy[1]));
					proxyList.add(proxyConnector);
					proxyDelay.add(0L);
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
		return proxyList;
	}

	public synchronized ProxyConnector getProxyConnector(){
	    return getProxyConnector(0);
	}
	
	public synchronized ProxyConnector getRandomProxyConnector(){
	    int proxyIndex = rand.nextInt(proxyList.size());
	    return getProxyConnector(proxyIndex);
	}
	
	private synchronized ProxyConnector getProxyConnector(int index){
		long curTime = System.currentTimeMillis();
		while(proxyDelay.size() <= index || proxyDelay.get(index) > (curTime - DELAY_FOR_PROXY)){
			try {
				Thread.currentThread().wait(100L);
			} catch (InterruptedException e) {
				log.error("Error during waiting new proxy connector",e);
			}
			curTime = System.currentTimeMillis();
		}
		proxyDelay.remove(0);
		return proxyList.remove(0);
	}

	public synchronized void releaseProxy(ProxyConnector proxyConnector){
		proxyDelay.add(System.currentTimeMillis());
		proxyList.add(proxyConnector);
		notifyAll();
	}
	
	public int getFreeProxyCount(){
	    return proxyList.size();
	}
}
