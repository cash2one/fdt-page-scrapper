package com.fdt.dailymotion;

import java.util.HashMap;

public class Account {
	private String login = "";
	private String email = "";
	private String pass = "";
	private HashMap<String,String> cookie = new HashMap<String, String>();

	public Account(String email, String login, String pass) {
		super();
		this.email = email;
		this.login = login;
		this.pass = pass;
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

	public String getCookies()
	{
		return getCookieString();
	}
	
	private String getCookieString(){
		StringBuilder strBuilder = new StringBuilder();
		
		for(String key : cookie.keySet()){
			strBuilder.append(key).append("=").append(cookie.get(key)).append("; ");
		}
		
		return strBuilder.toString();
	}

	public void addCookie(String key, String value)
	{
		this.cookie.put(key, value);
	}
	
	public String getCookie(String key){
		return cookie.get(key);
	}
}
