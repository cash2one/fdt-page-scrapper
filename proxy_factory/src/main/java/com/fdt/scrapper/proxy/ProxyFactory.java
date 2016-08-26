package com.fdt.scrapper.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@Scope("singleton")
public class ProxyFactory 
{
	private static final Logger log = Logger.getLogger(ProxyFactory.class);
	
	private static Random rand = new Random();
	
	/**Delay in ms*/
	@Value("${proxy.proxy_delay:10000}")
	private long delayProxy;
	
	@Value("${proxy.proxy_type:HTTP}")
	private String proxyType;
	
	@Value("${proxy.proxy_login:}")
	private String proxyLogin;
	
	@Value("${proxy.proxy_pass:}")
	private String proxyPass;
	
	//proxy_connector
	private ArrayList<ProxyConnector> proxyList = new ArrayList<ProxyConnector>();
	private ArrayList<ProxyConnector> bannedProxyList = new ArrayList<ProxyConnector>();
	//release_date
	private ArrayList<Long> proxyDelay = new ArrayList<Long>();
	
	@Value("${proxy.proxy_list_file_path:./proxy.txt}")
	private String pathToProxyList;

	public ProxyFactory() {
		super();
	}

	@PostConstruct
	public void init(){
		proxyList = loadProxyList(pathToProxyList);
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						proxyLogin, proxyLogin.toCharArray()
				);
			}
		});
	}


	/**
	 * Singleton for TaskFactory
	 * 
	 * @return
	 */
	/*public static ProxyFactory getInstance(){
		if(null == instance){
			synchronized (ProxyFactory.class) {
				if(null == instance){
					instance = new ProxyFactory(); 
				}
			}
		}
		return instance;
	}*/

	public ArrayList<ProxyConnector> loadProxyListFromInet(String fileURL) throws MalformedURLException, IOException{
		ArrayList<ProxyConnector> proxyList = null;
		String fileNameProxy = String.valueOf(System.currentTimeMillis());
		File proxyFile = new File(fileNameProxy);
		FileUtils.copyURLToFile(new URL(fileURL), proxyFile);
		proxyList = loadProxyList(fileNameProxy);
		if(proxyFile.exists()){
			proxyFile.delete();
		}
		return proxyList;
	}

	public ArrayList<ProxyConnector> loadProxyList(String cfgFilePath){
		proxyList = new ArrayList<ProxyConnector>();
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

	public ProxyConnector getProxyConnector(){
		ProxyConnector prConnector = getProxyConnector(0);

		while(bannedProxyList.contains(prConnector) || prConnector == null)
		{
			if(prConnector != null){
				releaseProxy(prConnector);
			}
			prConnector = getProxyConnector(0);
		}
		return prConnector;
	}

	public ProxyConnector getRandomProxyConnector(){

		int proxyIndex = proxyList.size() > 0 ? rand.nextInt(proxyList.size()):0;

		ProxyConnector prConnector = getProxyConnector(proxyIndex);

		while(bannedProxyList.contains(prConnector) || prConnector == null)
		{
			if(prConnector != null){
				releaseProxy(prConnector);
			}
			
			proxyIndex = proxyList.size() > 0 ? rand.nextInt(proxyList.size()):0;
			prConnector = getProxyConnector(proxyIndex);
		}

		return prConnector; 
	}

	private  ProxyConnector getProxyConnector(int index)
	{
		synchronized(proxyList){
			long curTime = System.currentTimeMillis();

			if(proxyDelay.size() <= index || proxyDelay.get(index) > (curTime - delayProxy)){
				return null;
			}

			log.info("NOT USED proxy servers: " + (proxyList.size()-1) );

			proxyDelay.remove(index);
			return proxyList.remove(index);
		}
	}

	public  void releaseProxy(ProxyConnector proxyConnector){
		synchronized(proxyList){
			proxyDelay.add(System.currentTimeMillis());
			proxyList.add(proxyConnector);
			proxyList.notifyAll();
		}
	}

	public int getFreeProxyCount(){
		return proxyList.size();
	}

	public void addToBannedList(ProxyConnector proxyConnector){
		if(!bannedProxyList.contains(proxyConnector)){
			log.info("Proxy " + proxyConnector + " was banned. And will not used again.");
			bannedProxyList.add(proxyConnector);
			log.info("Total banned proxy count is: " + bannedProxyList.size());
		}
	}

	public ArrayList<ProxyConnector> getBannedProxyList(){
		return bannedProxyList;
	}

	public String getProxyType() {
		return proxyType;
	}

	public void setProxyType(String proxyType) {
		this.proxyType = proxyType;
	}

	public Long getDelayProxy() {
		return delayProxy;
	}

	public void setDelayProxy(Long delayProxy) {
		this.delayProxy = delayProxy;
	}
}
