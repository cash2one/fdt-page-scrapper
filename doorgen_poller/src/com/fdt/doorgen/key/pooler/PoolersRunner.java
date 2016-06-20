package com.fdt.doorgen.key.pooler;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.fdt.doorgen.key.pooler.content.PoolerType;
import com.fdt.doorgen.key.pooler.runner.DoorgenPoolerSnippetsRunner;
import com.fdt.scrapper.task.ConfigManager;

public abstract class PoolersRunner
{
	private static final Logger log = Logger.getLogger(DoorgenPoolerSnippetsRunner.class);
	
	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	
	private final static String POOLER_RUNNER_LABEL = "pooler_runner";
	
	/**
	 * args[0] - path to config file
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		if(args.length < 1){
			System.out.print("Not enought arguments....");
		}else{
			System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
			ConfigManager.getInstance().loadProperties(args[0]);
			System.out.println(args[0]);
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
							ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL),
							ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
							);
				}
			});
			DOMConfigurator.configure("log4j_pooler.xml");

			PoolerType poolerType = null;
			
			try {
				//TODO Get appropriated Runner by strategy
				poolerType = PoolerType.getByName(ConfigManager.getInstance().getProperty(POOLER_RUNNER_LABEL));
				if(poolerType == null){
					log.error(String.format("Appropriated pooler '%s' was not found", ConfigManager.getInstance().getProperty(POOLER_RUNNER_LABEL)));
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			poolerType.getPoller().executePooler();
		}
	}
	
	public abstract void executeWrapper() throws Exception;
}
