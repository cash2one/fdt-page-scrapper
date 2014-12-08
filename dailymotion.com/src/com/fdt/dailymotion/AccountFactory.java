package com.fdt.dailymotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;

public class AccountFactory
{
	private static final Logger log = Logger.getLogger(AccountFactory.class);
	private ArrayList<Account> accounts = new ArrayList<Account>();

	public final static String MAIN_URL_LABEL = "main_url";
	private final static String LOGIN_URL_LABEL = "login_url";

	private ProxyFactory proxyFactory = null;

	public AccountFactory(ProxyFactory proxy){
		super();
		this.proxyFactory = proxy;
	}

	public void fillAccounts(String accListFilePath) throws Exception{
		//read account list
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(new File(accListFilePath));
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				//parse proxy adress
				if(line.contains(";")){
					String[] account = line.trim().split(";");
					accounts.add(new Account(account[0],account[2],account[1]));
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
		//getting cookie for each account
		try {
			ArrayList<Account> accountToRemove = new ArrayList<Account>();
			ProxyConnector proxy = proxyFactory.getProxyConnector();
			for(Account account : accounts){
				String postUrl = Constants.getInstance().getProperty(MAIN_URL_LABEL) + Constants.getInstance().getProperty(LOGIN_URL_LABEL);
				URL url = new URL(postUrl);
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy.getConnect());
				conn.setReadTimeout(60000);
				conn.setConnectTimeout(60000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				conn.addRequestProperty("Referer","http://www.dailymotion.com/ru");
				conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:18.0) Gecko/20100101 Firefox/18.0"); 
				//conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
				conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("form_name", "dm_pageitem_login"));
				nameValuePairs.add(new BasicNameValuePair("username", account.getEmail()));
				nameValuePairs.add(new BasicNameValuePair("password", account.getPass()));
				nameValuePairs.add(new BasicNameValuePair("_fid", ""));
				nameValuePairs.add(new BasicNameValuePair("from_request", "/ru"));

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(getQuery(nameValuePairs));
				writer.flush();
				writer.close();
				os.close();


				// Execute HTTP Post Request
				Map<String,List<String>> cookies = conn.getHeaderFields();//("Set-Cookie").getValue();
				if(cookies.get("Set-Cookie") != null && cookies.get("Set-Cookie").toString().contains("notexists")){
					log.error("Account doesn't exist: \""+ account.getLogin() + "\". Please check email and password.");
					accountToRemove.add(account);
					continue;
				}

				for(String cookieOne: cookies.get("Set-Cookie"))
				{
					String cookiesValues[] = cookieOne.split(";");
					for(String cookiesArrayItem : cookiesValues){
						String singleCookei[] = cookiesArrayItem.split("=");
						account.addCookie(singleCookei[0].trim(), singleCookei[1].trim());
					}
				}
				
				conn.disconnect();
			}
			proxyFactory.releaseProxy(proxy);

			for(Account account : accountToRemove){
				accounts.remove(account.getLogin());
			}
		} catch (Exception e) {
			log.error("Error during filling account from list and getting cookies for account",e);
			throw e;
		}
	}

	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
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
	
	public ArrayList<Account> getAccounts(){
		return accounts;
	}
}
