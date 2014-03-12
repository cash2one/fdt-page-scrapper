package com.fdt.registration.account;

import java.util.ArrayList;
import java.util.List;

public class Account {
	private String login = "";
	private String pass = "";
	private String email = "";
	private String groupId = "";

	private List<String> cookiesArray = new ArrayList<String>();

	public Account() {
		super();
	}

	public Account(String login, String pass,
			String email) {
		super();
		this.login = login;
		this.pass = pass;
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void addCookie(String cookie){
		this.cookiesArray.add(cookie);
	}

	public void addCookieArray(List<String> cookies){
		this.cookiesArray.addAll(cookies);
	}

	public void cleanCookieArray(){
		this.cookiesArray.clear();
	}

	public String cookiesToStr(){
		StringBuilder strBld = new StringBuilder();
		for(String cookie: this.cookiesArray){
			strBld.append(cookie).append("; ");
		}
		if(strBld.length() > 0){
			strBld.setLength(strBld.length()-2);
		}
		return strBld.toString();
	}

	@Override
	public String toString() {
		return "Account [login=" + login + ", pass=" + pass + ", groupId="
				+ groupId + "]";
	}

	public String toResultString() {
		return login + ":" + pass + ":" +groupId + "\r\n";
	}
}

