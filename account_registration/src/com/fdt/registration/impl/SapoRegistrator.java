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
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.fdt.registration.IRegistrator;
import com.fdt.registration.account.Account;
import com.fdt.registration.email.Email;
import com.fdt.registration.exception.AuthorizationException;
import com.fdt.registration.exception.NoRegisteredException;
import com.fdt.scrapper.proxy.ProxyConnector;


public class SapoRegistrator extends IRegistrator{

	private static final String HTTPS_LOGIN_SAPO_PT_USER_REGISTER_DO = "https://login.sapo.pt/UserRegister.do";
	private static final String HTTPS_LOGIN_SAPO_PT_LOGIN_DO = "https://login.sapo.pt/Login.do";
	private static final Logger log = Logger.getLogger(SapoRegistrator.class);

	@Override
	public boolean register(Account account) throws NoRegisteredException{
		//String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + task.getKeyWords() + "delete/";

		//Get email for account
		boolean isFormSubmit = false;

		try {
			proxyCnctr = this.getProxyFactory().getProxyConnector();
			//post news
			URL url = new URL(HTTPS_LOGIN_SAPO_PT_USER_REGISTER_DO);
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
			conn.setRequestProperty("Host", "login.sapo.pt");

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			log.trace("---------------Register string:" + this.getRegFormFactory().getRegFormParams(account).toString());
			writer.write(getQuery(this.getRegFormFactory().getRegFormParams(account)));
			writer.flush();
			writer.close();
			os.close();

			//conn.getRequestProperties()
			int code = conn.getResponseCode();
			
			conn.getInputStream();
			
			InputStream is = conn.getInputStream();
			
			log.trace("HTML:-------------------------------------------------------------\r\n" 
			+ is2srt(is)
			+ "\r\nHTML:-------------------------------------------------------------\r\n");
			
			if(is != null){
				is.close();
			}

			log.debug("Responce code for submit form (" + account + "): " + code);

			isFormSubmit = true;
		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		} catch (XPathExpressionException e) {
			log.error("Error occured during posting news",e);
		}

		return isFormSubmit;
	}


	@Override
	public boolean verify(Account account) throws NoRegisteredException {

		List<Email> emails = this.getMailWorker().checkEmail(account, proxyCnctr);

		int attemptCount = 0;
		while(emails.size() == 0 && attemptCount < MAX_EMAIL_CHECK_ATTEMPT_COUNT)
		{
			log.debug("#" + attemptCount + ": Try to check verification email for account: " + account.toString());
			emails = this.getMailWorker().checkEmail(account, proxyCnctr);
			attemptCount++;
			
			if(emails.size() > 0){
				break;
			}else{
				synchronized(this){
					try {
						wait(500L);
					} catch (InterruptedException e) {
					}
				}
			}
		}

		if(emails.size() == 0){
			throw new NoRegisteredException("Can't getting verification email from mail box 'temp-mail.ru' for email: '" + account.getEmail() + "'");
		}

		//submit verify email
		String  verifyLink = getVerifyLink(emails);
		log.info("Verification link (for account" + account.toString() + "): " + verifyLink);

		if(verifyLink != null && !verifyLink.trim().isEmpty()){
			int code = submitLink(verifyLink);
			while(code != 200){
				log.info("Server responces for verification link(" + verifyLink + "): " + code);
				code = submitLink(verifyLink);
			}
			log.info("Server responces for verification link(" + verifyLink + "): " + code);
		}else{
			throw new NoRegisteredException("Can't get verification link for email: " + account.getEmail());
		}

		return true;
	}

	@Override
	public boolean postVerifyAction(Account account) throws Exception {
		// TODO Implement blog refistration



		return false;
	}
	
	private String is2srt(InputStream is) throws IOException{
		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
	 
		String line;
		StringBuffer strBuf =  new StringBuffer();
		while ((line = br.readLine()) != null) {
			strBuf.append(line);
		}
		return strBuf.toString();
	}

	private void userLogin(Account account) throws AuthorizationException{
		//Get email for account
		boolean isFormSubmit = false;

		try {
			//post news
			URL url = new URL(HTTPS_LOGIN_SAPO_PT_LOGIN_DO);
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

			//conn.getRequestProperties()
			int code = conn.getResponseCode();

			//read cookies
			Map<String,List<String>> cookies = conn.getHeaderFields();

			if(cookies.get("Set-Cookie") != null){
				for(String cookieOne: cookies.get("Set-cookie"))
				{
					account.addCookie(cookieOne);
				}
			}else{
				throw new AuthorizationException("Registration failed for user: " + code);
			}

			log.debug("Responce code for submit form (" + account + "): " + code);

			isFormSubmit = true;
		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		} catch (XPathExpressionException e) {
			log.error("Error occured during posting news",e);
		}
		finally{
			if(proxyCnctr != null){
				this.getProxyFactory().releaseProxy(proxyCnctr);
			}
		}
	}

	private int submitLink(String link){
		int code = -1;
		ProxyConnector proxyCnctr = null;

		try {
			proxyCnctr = this.getProxyFactory().getProxyConnector();
			//post news
			URL url = new URL(link);
			HttpsURLConnection.setFollowRedirects(false);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxyCnctr.getConnect(Type.SOCKS.toString()));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			//conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			//conn.setRequestProperty("Accept-Language", "ru-RU");
			//conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");

			code = conn.getResponseCode();
		} catch (ClientProtocolException e) {
			log.error("Error occured during posting news",e);
		} catch (IOException e) {
			log.error("Error occured during posting news",e);
		} catch (XPathExpressionException e) {
			log.error("Error occured during posting news",e);
		}
		finally{
			if(proxyCnctr != null){
				this.getProxyFactory().releaseProxy(proxyCnctr);
			}
		}

		return code;
	}

	private String getVerifyLink(List<Email> emails){
		String verifyLink = "";

		for(Email email : emails){
			if("no-reply@id.sapo.pt".equalsIgnoreCase(email.getMessageFrom())){
				HtmlCleaner cleaner = new HtmlCleaner();
				TagNode html = cleaner.clean(email.getHtmlBody());

				try {
					verifyLink = html.evaluateXPath("//a/text()")[0].toString();
					return verifyLink;
				} catch (XPatherException e) {
					log.error("Error occured during check emails");
				}
			}
		}

		return verifyLink;
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
