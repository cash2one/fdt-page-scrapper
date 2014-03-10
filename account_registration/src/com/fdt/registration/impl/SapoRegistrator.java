package com.fdt.registration.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.fdt.registration.IRegistrator;
import com.fdt.registration.account.Account;
import com.fdt.scrapper.proxy.ProxyConnector;


public class SapoRegistrator extends IRegistrator{

	private static final Logger log = Logger.getLogger(SapoRegistrator.class);

	@Override
	public String register(Account account) {
		//String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + task.getKeyWords() + "delete/";

		ProxyConnector proxyCnctr = this.getProxyFactory().getProxyConnector();
		//Get email for account
		String email = this.getMailWorker().getEmail();
		account.setEmail(email);

		try {
			//post news
			URL url = new URL("https://login.sapo.pt/UserRegister.do");
			HttpsURLConnection.setFollowRedirects(false);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxyCnctr.getConnect(Type.SOCKS.toString()));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU");
			conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(this.getRegFormFactory().getRegFormParams(account)));
			writer.flush();
			writer.close();
			os.close();

			//if(returnCode == HttpStatus.SC_OK){
			/*conn.disconnect();
	    conn = (HttpURLConnection) url.openConnection(proxy);
	    conn.setRequestMethod("GET");
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
	    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*//*;q=0.8");
	    conn.setRequestProperty("Cookie", account.getCookie());*/

			//conn.getRequestProperties()
			int code = conn.getResponseCode();

			InputStream is = conn.getInputStream();

			String link = "";
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String json = reader.readLine();

			}
			/*catch (ParseException e) {
				System.out.println("Error occured during posting news");
				//logExtarnal.error("Error occured during posting news",e);
			}*/
			finally{
				if(reader != null){
					reader.close();
				}
			}

			return "";
		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		} catch (XPathExpressionException e) {
			log.error("Error occured during posting news",e);
		}
		finally{
			this.getProxyFactory().releaseProxy(proxyCnctr);
		}

		return "";
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

}
