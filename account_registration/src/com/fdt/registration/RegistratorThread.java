package com.fdt.registration;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.fdt.registration.account.Account;
import com.fdt.registration.exception.NoRegisteredException;

public class RegistratorThread implements Callable<Account>{
	
	private static final Logger log = Logger.getLogger(RegistratorRunner.class);
	
	private IRegistrator registrator;
	private Account account;
	
	public RegistratorThread(IRegistrator registrator, Account account) {
		super();
		this.registrator = registrator;
		this.account = account;
	}

	@Override
	public Account call() throws Exception {
		boolean isRegistered = false;
		try {
			isRegistered = registrator.register(account);
			if(isRegistered){
				registrator.verify(account);
			}else{
				log.error("Can't submit form for registration account: " + account);
				return null;
			}
		} catch (NoRegisteredException e) {
			log.error("Can't register account for user: " + account.toString());
		}
		return account;
	}
}
