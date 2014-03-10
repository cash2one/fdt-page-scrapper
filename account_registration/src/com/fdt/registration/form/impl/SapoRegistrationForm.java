package com.fdt.registration.form.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.fdt.registration.account.Account;
import com.fdt.registration.form.IRegistrationForm;

public class SapoRegistrationForm implements IRegistrationForm{

	private List<NameValuePair> params;
	
	public SapoRegistrationForm() {
		super();
		this.params = new ArrayList<NameValuePair>();
		this.params.add(new BasicNameValuePair("tos", "1"));
		this.params.add(new BasicNameValuePair("sapo_widget_registo_form_submit", ""));
	}

	@Override
	public List<NameValuePair> getRegFormParams(Account account) {
		//email
		this.params.add(new BasicNameValuePair("name", account.getLogin()));
		this.params.add(new BasicNameValuePair("SAPO_LOGIN_USERNAME", account.getEmail()));
		this.params.add(new BasicNameValuePair("SAPO_LOGIN_PASSWORD", account.getPass()));
		this.params.add(new BasicNameValuePair("confpassword", account.getPass()));
		this.params.add(new BasicNameValuePair("phone", ""));

		return this.params;
	}
}
