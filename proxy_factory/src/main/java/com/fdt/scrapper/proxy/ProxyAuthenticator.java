package com.fdt.scrapper.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxyAuthenticator extends Authenticator {

	private String loginPA;
	private String passPA;
	public ProxyAuthenticator(String login, String pass) {
		super();
		this.loginPA = login;
		this.passPA = pass;
	}
	
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.loginPA, this.passPA.toCharArray());
	}
}
