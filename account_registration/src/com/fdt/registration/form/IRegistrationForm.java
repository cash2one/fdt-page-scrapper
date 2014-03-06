package com.fdt.registration.form;

import java.util.HashMap;

import com.fdt.registration.account.Account;

public interface IRegistrationForm {
	public HashMap<String, String> getRegFormParams(Account account);
}
