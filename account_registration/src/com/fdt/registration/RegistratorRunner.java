package com.fdt.registration;

import java.net.Authenticator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fdt.registration.account.Account;

public class RegistratorRunner {

	public static void main(String[] args) {
		Account account = new Account();
		account.setEmail("toweso@postalmail.biz");
		account.setLogin("toweso@postalmail.biz");
		account.setPass("toweso@postalmail.biz");
			
		 ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");

	      IRegistrator obj = (IRegistrator) context.getBean("sapoRegistrator");
	      
	      Authenticator proxyAuth = (Authenticator) context.getBean("proxyAuthenticator");
	      Authenticator.setDefault(proxyAuth);

	      String result = obj.register(account);
	      
	      System.out.println(result);

	}
}
