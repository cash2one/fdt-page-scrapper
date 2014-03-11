package com.fdt.registration;

import com.fdt.registration.account.Account;
import com.fdt.registration.email.MailWorker;
import com.fdt.registration.exception.NoRegisteredException;
import com.fdt.registration.form.RegistrationFormFactory;
import com.fdt.scrapper.proxy.ProxyFactory;

public abstract class IRegistrator {
	
	protected static final int MAX_EMAIL_CHECK_ATTEMPT_COUNT = 1;
	
	private ProxyFactory proxyFactory;
	private RegistrationFormFactory regFormFactory;
	private MailWorker mailWorker;
	
	public abstract boolean register(Account account) throws NoRegisteredException;
	public abstract boolean verify(Account account) throws NoRegisteredException;
	
	public ProxyFactory getProxyFactory() {
		return proxyFactory;
	}
	public void setProxyFactory(ProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}
	public RegistrationFormFactory getRegFormFactory() {
		return regFormFactory;
	}
	public void setRegFormFactory(RegistrationFormFactory regFormFactory) {
		this.regFormFactory = regFormFactory;
	}
	public MailWorker getMailWorker() {
		return mailWorker;
	}
	public void setMailWorker(MailWorker mailWorker) {
		this.mailWorker = mailWorker;
	}
}
