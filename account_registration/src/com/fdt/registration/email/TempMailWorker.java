package com.fdt.registration.email;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy.Type;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.fdt.registration.account.Account;
import com.fdt.scrapper.proxy.ProxyConnector;

public class TempMailWorker extends MailWorker {

	private static final Logger log = Logger.getLogger(TempMailWorker.class);

	private Random rnd = new Random();

	private final String DOMAIN_GETTER_API_PATH = "http://api.temp-mail.ru/request/domains/format/xml/";
	private final String EMAIL_CHECK_API_PATH = "http://api.temp-mail.ru/request/mail/id/";

	private final String ALFABET_STR = "abcdefghijklmnopqrstuvwxyz";

	private List<String> emailDomains= new ArrayList<String>();

	public TempMailWorker(){
		super();
		emailDomains.add("@postalmail.biz");
		emailDomains.add("@rainmail.biz");
		emailDomains.add("@mailblog.biz");
	}

	private List<String> getEmailDomains(ProxyConnector proxyCnctr){
		InputStream inputStreamPage = null;
		List<String> emailDomains = new ArrayList<String>();

		try {
			//post news
			URL url = new URL(DOMAIN_GETTER_API_PATH);
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxyCnctr.getConnect(Type.SOCKS.toString()));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			conn.setRequestProperty("Host", "api.temp-mail.ru");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

			//conn.getRequestProperties()
			int code = conn.getResponseCode();

			HtmlCleaner cleaner = new HtmlCleaner();

			inputStreamPage = conn.getInputStream();

			TagNode html = null;

			html = cleaner.clean(inputStreamPage,"UTF-8");

			Object[] emails = html.evaluateXPath("//xml/item/text()");
			for(Object email : emails){
				emailDomains.add(email.toString());
			}


		} catch (ClientProtocolException e) {
			log.error("Error occured during getting email domains",e);
		} catch (IOException e) {
			log.error("Error occured during getting email domains",e);
		} catch (XPathExpressionException e) {
			log.error("Error occured during getting email domains",e);
		} catch (XPatherException e) {
			log.error("Error occured during getting email domains",e);
		}

		return emailDomains;
	}

	@Override
	public synchronized String getEmail() {
		while(emailDomains.size() == 0){
			log.debug("Getting emails domain...");
			//this.emailDomains = getEmailDomains(proxyCnctr);
		}
		String email =  ALFABET_STR.charAt(rnd.nextInt(ALFABET_STR.length())) + 
				String.valueOf(System.currentTimeMillis()) + 
				ALFABET_STR.charAt(rnd.nextInt(ALFABET_STR.length())) +
				emailDomains.get(rnd.nextInt(emailDomains.size()));
		log.debug("Generated email: " + email);
		return email;
	}

	@Override
	public List<Email> checkEmail(Account account, ProxyConnector proxyCnctr) {
		InputStream inputStreamPage = null;
		List<Email> emailsLst = new ArrayList<Email>();

		try {
			//post news
			URL url = new URL(EMAIL_CHECK_API_PATH + str2md5(account.getEmail()) + "/");
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxyCnctr.getConnect(Type.SOCKS.toString()));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			conn.setRequestProperty("Host", "api.temp-mail.ru");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

			//conn.getRequestProperties()
			int code = conn.getResponseCode();

			HtmlCleaner cleaner = new HtmlCleaner();

			inputStreamPage = conn.getInputStream();

			TagNode html = null;

			html = cleaner.clean(inputStreamPage,"UTF-8");

			Object[] emails = html.evaluateXPath("//xml/item");

			for(Object email : emails){
				emailsLst.add(parseXml2Email((TagNode)email));
			}


		} catch (ClientProtocolException e) {
			log.error("Error occured during checking emails ("+account+")",e);
		} catch (IOException e) {
			log.error("Error occured during checking emails ("+account+")",e);
		} catch (XPathExpressionException e) {
			log.error("Error occured during checking emails ("+account+")",e);
		} catch (XPatherException e) {
			log.error("Error occured during checking emails ("+account+")",e);
		}
		finally{
			if(inputStreamPage != null){
				try {
					inputStreamPage.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return emailsLst;
	}

	private Email parseXml2Email(TagNode emailTagNode) throws XPatherException{
		Email email = new Email();

		email.setHtmlBody(emailTagNode.evaluateXPath("//mail_html/text()")[0].toString().replaceAll("&lt;","<").replaceAll("&gt;", ">"));
		email.setMessageFrom(emailTagNode.evaluateXPath("//mail_from/text()")[0].toString());

		return email;
	}

	private String str2md5(String str) {

		MessageDigest md;
		StringBuffer sb = new StringBuffer();

		try {
			md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());

			byte byteData[] = md.digest();

			//convert the byte to hex format method 1
			sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();
	}

}