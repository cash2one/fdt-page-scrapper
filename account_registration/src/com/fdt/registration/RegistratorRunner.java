package com.fdt.registration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fdt.registration.account.Account;
import com.fdt.registration.exception.NoRegisteredException;

public class RegistratorRunner {

	private static final Logger log = Logger.getLogger(RegistratorRunner.class);

	public static void main(String[] args) {
		

		ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");

		IRegistrator obj = (IRegistrator) context.getBean("sapoRegistrator");

		Authenticator proxyAuth = (Authenticator) context.getBean("proxyAuthenticator");
		Authenticator.setDefault(proxyAuth);

		while(true){
			boolean isRegistered = false;
			Account account = new Account();
			try {
				isRegistered = obj.register(account);
				if(isRegistered){
					obj.verify(account);
				}else{
					log.error("Can't submit form for registration account: " + account);
				}
			} catch (NoRegisteredException e) {
				log.error("Can't register account for user: " + account.toString());
			}
			appendAccountToFile(account);
		}

	}
	
	private static void appendAccountToFile(Account account){
		try{
    		File file =new File("registered-accounts.txt");
 
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
 
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	        bufferWritter.write(account.toResultString());
    	        bufferWritter.close();
 
    	}catch(IOException e){
    		log.error("Error during saving account to result file.");
    	}
	}
}
