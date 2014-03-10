package com.fdt.registration.form;

import java.util.List;

import org.apache.http.NameValuePair;

import com.fdt.registration.account.Account;

public interface IRegistrationForm {
	//TODO Add list of "User-Agent"
	public List<NameValuePair> getRegFormParams(Account account);
}
