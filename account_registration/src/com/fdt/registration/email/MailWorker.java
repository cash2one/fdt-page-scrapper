package com.fdt.registration.email;

import java.util.List;

import com.fdt.registration.account.Account;
import com.fdt.scrapper.proxy.ProxyConnector;

public abstract class MailWorker {
	
	public MailWorker() {
		super();
	}
	
	public abstract String getEmail();
	public abstract List<Email> checkEmail(Account account, ProxyConnector proxyCnctr);
}
