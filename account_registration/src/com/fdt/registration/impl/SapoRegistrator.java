package com.fdt.registration.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
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

	private static final String LOCATION = "Location";
	private static final String CSRF_IDENTIFIER_LABEL = "csrf_identifier";
	private static final String HTTPS_LOGIN_SAPO_PT_USER_REGISTER_DO = "https://login.sapo.pt/UserRegister.do";
	private static final String HTTPS_LOGIN_SAPO_PT_LOGIN_DO = "https://login.sapo.pt/Login.do?to=%2Fshibboleth-idp%2FSSO%3Fshire%3Dhttp%253A%252F%252Fblogs.sapo.pt%252FShibboleth.sso%252FSAML%252FArtifact%26time%3D1395929478%26target%3Dhttp%253A%252F%252Fblogs.sapo.pt%252Fcreate.bml%26providerId%3Dhttp%253A%252F%252Fblogs.sapo.pt&providerId=http%3A%2F%2Fblogs.sapo.pt";
	private static final String HTTPS_LOGIN_SAPO_PT = "https://login.sapo.pt/";
	private static final Logger log = Logger.getLogger(SapoRegistrator.class);

	static {
		//Code below needed for remove SSLHandshakeException during singOn login
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]
				{new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}
				}};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}

	}

	@Override
	public boolean register(Account account) throws NoRegisteredException{
		//String postUrl = Constants.getInstance().getProperty(AccountFactory.MAIN_URL_LABEL) + task.getKeyWords() + "delete/";

		//Get email for account
		boolean isFormSubmit = false;
		ProxyConnector proxyCnctr = null;

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
		}finally{
			if(proxyCnctr != null){
				this.getProxyFactory().releaseProxy(proxyCnctr);
			}
		}

		return isFormSubmit;
	}


	@Override
	public boolean verify(Account account) throws NoRegisteredException {

		ProxyConnector proxyCnctr = null;

		try{
			proxyCnctr = this.getProxyFactory().getProxyConnector();
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
		}
		finally{
			if(proxyCnctr != null){
				this.getProxyFactory().releaseProxy(proxyCnctr);
			}
		}

		return true;
	}

	@Override
	public boolean postVerifyAction(Account account) throws Exception {
		// TODO Implement blog refistration
		boolean signed = false;
		ProxyConnector proxyCnctr = null;

		//TODO Delete after test
		account = new Account("w1394625826897l@mailblog.biz", "w1394625826897l@mailblog.biz", "w1394625826897l@mailblog.biz");

		try{
			while(!signed){
				if(proxyCnctr != null){
					this.getProxyFactory().releaseProxy(proxyCnctr);
				}
				proxyCnctr = this.getProxyFactory().getProxyConnector();
				signed = getCookie(account, proxyCnctr);
			}

			signed = false;
			while(!signed){
				signed = userLogin(account, proxyCnctr);
			}

			while(account.getExtraParam(LOCATION) != null){
				followToRedirect(account, proxyCnctr);
			}
		}finally{
			if(proxyCnctr != null){
				this.getProxyFactory().releaseProxy(proxyCnctr);
			}
		}

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

	private boolean userLogin(Account account, ProxyConnector proxyCnctr) throws AuthorizationException{
		//Get email for account

		boolean signed = false;

		try {
			//proxyCnctr = new ProxyConnector("127.0.0.1", 8888);
			URL url = new URL(HTTPS_LOGIN_SAPO_PT_LOGIN_DO);
			HttpsURLConnection.setFollowRedirects(false);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxyCnctr.getConnect(Type.HTTP.toString()));

			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(CSRF_IDENTIFIER_LABEL, account.getExtraParam(CSRF_IDENTIFIER_LABEL)));
			params.add(new BasicNameValuePair("SAPO_LOGIN_USERNAME", account.getLogin()));
			params.add(new BasicNameValuePair("SAPO_LOGIN_PASSWORD", account.getPass()));
			params.add(new BasicNameValuePair("persistent","1"));
			params.add(new BasicNameValuePair("sapo_widget_login_form_submit", ""));

			String queryParams = getQuery(params);

			//conn.setRequestProperty("Host", "login.sapo.pt");
			conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");
			conn.setRequestProperty("Accept-Language", "ru-RU");
			conn.setRequestProperty("Referer","https://login.sapo.pt/");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)"); 
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
			conn.setRequestProperty("Connection", "Keep-Alive");
			//conn.setRequestProperty("Content-Length",String.valueOf(queryParams.getBytes("UTF-8").length));
			conn.setRequestProperty("Cookie", account.cookiesToStr());

			OutputStream os = conn.getOutputStream();

			//BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
			writer.write(queryParams);
			writer.flush();
			writer.close();
			os.close();

			//conn.getRequestProperties()
			int code = conn.getResponseCode();

			if(code != 302){
				signed = false;
			}else{
				signed = true;
			}

			Map<String,List<String>> headers = conn.getHeaderFields();

			if(headers.get("Set-Cookie") != null){
				for(String cookieOne: headers.get("Set-Cookie"))
				{
					account.addCookie(cookieOne);
				}
			}

			if(headers.get(LOCATION) != null){
				for(String cookieOne: headers.get(LOCATION))
				{
					account.addExtraParam(LOCATION, cookieOne);
				}
			}

			InputStream is = conn.getInputStream();

			//get csrf_identifier
			/*HtmlCleaner cleaner = new HtmlCleaner();
			TagNode csrf = cleaner.clean(is,"UTF-8");
			Object[] csrfIdnfr = csrf.evaluateXPath("//form/fieldset/input[@name]/@value");
			if(csrfIdnfr.length > 0){
				account.addExtraParam(CSRF_IDENTIFIER_LABEL, (String)csrfIdnfr[0]);
			}else{
				throw new AuthorizationException("Can't getting params '"+CSRF_IDENTIFIER_LABEL+"'");
			}*/

			log.trace("HTML:-------------------------------------------------------------\r\n" 
					+ is2srt(is)
					+ "\r\nHTML:-------------------------------------------------------------\r\n");

			if(is != null){
				is.close();
			}

			conn.disconnect();

			log.debug("Responce code for submit form (" + account + "): " + code);

		} catch (ClientProtocolException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} catch (IOException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} catch (XPathExpressionException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} /*catch (XPatherException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		}*/

		return signed;
	}

	private boolean followToRedirect(Account account, ProxyConnector proxyCnctr) throws AuthorizationException{
		//Get email for account
		boolean signed = false;

		try {
			//proxyCnctr = new ProxyConnector("127.0.0.1", 8888);
			URL url = new URL(account.getExtraParam(LOCATION));
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxyCnctr.getConnect(Type.HTTP.toString()));

			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setInstanceFollowRedirects(false);


			//conn.setRequestProperty("Host", "login.sapo.pt");
			conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");
			conn.setRequestProperty("Accept-Language", "ru-RU");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)"); 
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
			conn.setRequestProperty("Connection", "Keep-Alive");
			//conn.setRequestProperty("Content-Length",String.valueOf(queryParams.getBytes("UTF-8").length));
			conn.setRequestProperty("Cookie", account.cookiesToStr());

			//conn.getRequestProperties()
			int code = conn.getResponseCode();

			if(code != 302){
				signed = false;
			}else{
				signed = true;
			}

			Map<String,List<String>> headers = conn.getHeaderFields();

			if(headers.get("Set-Cookie") != null){
				for(String cookieOne: headers.get("Set-Cookie"))
				{
					account.addCookie(cookieOne);
				}
			}

			//account.addExtraParam(LOCATION, null);
			
			if(headers.get(LOCATION) != null){
				for(String cookieOne: headers.get(LOCATION))
				{
					account.addExtraParam(LOCATION, cookieOne);
				}
			}

			InputStream is = conn.getInputStream();

			//get csrf_identifier
			/*HtmlCleaner cleaner = new HtmlCleaner();
					TagNode csrf = cleaner.clean(is,"UTF-8");
					Object[] csrfIdnfr = csrf.evaluateXPath("//form/fieldset/input[@name]/@value");
					if(csrfIdnfr.length > 0){
						account.addExtraParam(CSRF_IDENTIFIER_LABEL, (String)csrfIdnfr[0]);
					}else{
						throw new AuthorizationException("Can't getting params '"+CSRF_IDENTIFIER_LABEL+"'");
					}*/

			log.trace("HTML:-------------------------------------------------------------\r\n" 
					+ is2srt(is)
					+ "\r\nHTML:-------------------------------------------------------------\r\n");

			if(is != null){
				is.close();
			}

			conn.disconnect();

			log.debug("Responce code for submit form (" + account + "): " + code);

		} catch (ClientProtocolException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} catch (IOException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} catch (XPathExpressionException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} /*catch (XPatherException e) {
					log.error("Error occured during sign in",e);
					signed = false;
				}*/

		return signed;
	}

	private boolean loadCreationBlogPage(Account account, ProxyConnector proxyCnctr) throws AuthorizationException{
		//Get email for account

		boolean signed = false;

		try {

			proxyCnctr = new ProxyConnector("127.0.0.1", 8888);

			URL url = new URL("http://blogs.sapo.pt/create.bml");
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxyCnctr.getConnect(Type.HTTP.toString()));

			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setInstanceFollowRedirects(true);

			//conn.setRequestProperty("Host", "login.sapo.pt");
			conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");
			conn.setRequestProperty("Accept-Language", "ru-RU");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)"); 
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
			conn.setRequestProperty("Connection", "Keep-Alive");
			//conn.setRequestProperty("Content-Length",String.valueOf(queryParams.getBytes("UTF-8").length));
			conn.setRequestProperty("Cookie", account.cookiesToStr());

			//conn.getRequestProperties()
			int code = conn.getResponseCode();

			Map<String,List<String>> cookies = conn.getHeaderFields();

			if(cookies.get("Set-Cookie") != null){
				for(String cookieOne: cookies.get("Set-Cookie"))
				{
					account.addCookie(cookieOne);
				}
			}/*else{
				throw new AuthorizationException("SignIn failed for user: " + code);
			}*/

			InputStream is = conn.getInputStream();

			//get csrf_identifier
			/*HtmlCleaner cleaner = new HtmlCleaner();
			TagNode csrf = cleaner.clean(is,"UTF-8");
			Object[] csrfIdnfr = csrf.evaluateXPath("//form/fieldset/input[@name]/@value");
			if(csrfIdnfr.length > 0){
				account.addExtraParam(CSRF_IDENTIFIER_LABEL, (String)csrfIdnfr[0]);
			}else{
				throw new AuthorizationException("Can't getting params '"+CSRF_IDENTIFIER_LABEL+"'");
			}*/

			log.trace("HTML:-------------------------------------------------------------\r\n" 
					+ is2srt(is)
					+ "\r\nHTML:-------------------------------------------------------------\r\n");

			if(is != null){
				is.close();
			}

			conn.disconnect();

			log.debug("Responce code for submit form (" + account + "): " + code);

		} catch (ClientProtocolException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} catch (IOException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} catch (XPathExpressionException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		} /*catch (XPatherException e) {
			log.error("Error occured during sign in",e);
			signed = false;
		}*/

		return signed;
	}

	private boolean getCookie(Account account, ProxyConnector proxyCnctr) throws AuthorizationException{
		//Get cookie for account
		InputStream inputStreamPage = null;

		boolean signed = false;

		try {
			URL url = new URL(HTTPS_LOGIN_SAPO_PT);
			HttpsURLConnection.setFollowRedirects(false);

			//proxyCnctr = new ProxyConnector("127.0.0.1", 8888);

			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection(proxyCnctr.getConnect(Type.HTTP.toString()));
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);

			//conn.setRequestProperty("Host", "login.sapo.pt");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0"); 
			conn.setRequestProperty("Accept-Language", "ru-RU");
			conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");


			//conn.getRequestProperties()
			int code = conn.getResponseCode();

			//read cookies
			Map<String,List<String>> cookies = conn.getHeaderFields();

			if(cookies.get("Set-Cookie") != null){
				for(String cookieOne: cookies.get("Set-Cookie"))
				{
					account.addCookie(cookieOne);
				}
				account.addCookie("lastUsedTab=sapo");
				account.addCookie("lastSessionTab=sapo");
			}else{
				throw new AuthorizationException("Registration failed for user: " + code);
			}

			log.debug("Responce code for submit form (" + account + "): " + code);

			TagNode csrf = null;
			HtmlCleaner cleaner = new HtmlCleaner();
			inputStreamPage = conn.getInputStream();

			csrf = cleaner.clean(inputStreamPage,"UTF-8");

			Object[] csrfIdnfr = csrf.evaluateXPath("//form/fieldset/input[@name]/@value");

			if(csrfIdnfr.length > 0){
				account.addExtraParam(CSRF_IDENTIFIER_LABEL, (String)csrfIdnfr[0]);
			}else{
				throw new AuthorizationException("Can't getting params '"+CSRF_IDENTIFIER_LABEL+"'");
			}

			conn.disconnect();

			signed = true;
		} catch (ClientProtocolException e) {
			log.error("Error occured during getting cookies",e);
		} catch (IOException e) {
			log.error("Error occured during getting cookies",e);
		} catch (XPathExpressionException e) {
			log.error("Error occured during getting cookies",e);
		} catch (XPatherException e) {
			log.error("Error occured during getting cookies",e);
		}

		return signed;
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
			/*result.append(pair.getName());
			result.append("=");
			result.append(pair.getValue());*/
		}

		return result.toString();
	}
}
