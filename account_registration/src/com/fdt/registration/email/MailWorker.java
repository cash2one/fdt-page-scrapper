package com.fdt.registration.email;

import java.util.List;

import com.fdt.scrapper.proxy.ProxyFactory;

public abstract class MailWorker {
	
	private ProxyFactory proxyFactory;
	
	public MailWorker() {
		super();
	}
	
	public abstract String getEmail();
	public abstract List<Email> checkEmail(String address);
	
	public ProxyFactory getProxyFactory() {
		return proxyFactory;
	}
	public void setProxyFactory(ProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}
	
	
}