package com.fdt.jimbo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Account {
	private String login = "";
	private String email = "";
	private String pass = "";
	private String secPass = "";
	private String site = "";
	private String siteWOHttp = "";
	private HashMap<String,String> cookie = new HashMap<String, String>();
	private boolean logged = false;
	
	private boolean loginErr = false;
	
	private AccountFactory accountFactory;

	public Account(String email, String login, String pass, String site, String setPass, AccountFactory accountFactory) {
		super();
		this.email = email;
		this.login = login;
		this.pass = pass;
		this.site = site;
		this.siteWOHttp = site.substring(7);
		this.secPass = setPass;
		this.accountFactory = accountFactory;
	}

	public String getLogin()
	{
		return login;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	public String getPass()
	{
		return pass;
	}

	public void setPass(String pass)
	{
		this.pass = pass;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getCookies(String[] cookieKeyList)
	{
		return getCookieString(new HashSet<String>(Arrays.asList(cookieKeyList)));
	}
	
	public String getCookies()
	{
		return getCookieString(cookie.keySet());
	}
	
	private String getCookieString(Set<String> keySet){
		StringBuilder strBuilder = new StringBuilder();
		
		for(String key :keySet){
			if(cookie.get(key) != null){
				strBuilder.append(key).append("=").append(cookie.get(key)).append("; ");
			}
		}
		
		return strBuilder.toString();
	}

	public void addCookie(String key, String value)
	{
		if(!"httponly".equals(key.toLowerCase())){
			this.cookie.put(key, value);
		}
	}
	
	public String getCookie(String key){
		return cookie.get(key);
	}
	
	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
	}

	public String toString(){
		return String.format("%s;%s;%s;%s;%s", login, pass, site, email, secPass);
	}
	

	public AccountFactory getAccountFactory() {
		return accountFactory;
	}

	public String getSiteWOHttp() {
		return siteWOHttp;
	}

	public boolean isLoginErr() {
		return loginErr;
	}

	public void setLoginErr(boolean loginErr) {
		this.loginErr = loginErr;
	}
	
}
