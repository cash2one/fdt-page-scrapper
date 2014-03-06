package com.fdt.registration.form;

import java.util.HashMap;

import com.fdt.registration.account.Account;

public class RegistrationFormFactory {
	private IRegistrationForm regForm;

	public RegistrationFormFactory() {
		super();
	}
	
	public HashMap<String, String> getRegFormParams(Account account){
		return regForm.getRegFormParams(account);
	}

	public IRegistrationForm getRegForm() {
		return regForm;
	}

	public void setRegForm(IRegistrationForm regForm) {
		this.regForm = regForm;
	}
}
