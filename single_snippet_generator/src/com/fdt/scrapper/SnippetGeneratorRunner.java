/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.scrapper;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.scrapper.task.ConfigManager;

/**
 *
 * @author Administrator
 */
public class SnippetGeneratorRunner{

	private static final String MAX_EXECUTION_TIME_LABEL = "max_execution_time";

	private static final Logger log = Logger.getLogger(SnippetGeneratorRunner.class);

	private final static String PROXY_LOGIN_LABEL = "proxy_login";
	private final static String PROXY_PASS_LABEL = "proxy_pass";
	private final static String PROXY_LIST_FILE_PATH_LABEL = "proxy_list_file_path";
	private final static String LINKS_LIST_FILE_PATH_LABEL = "links_list_file_path";


	Random rnd = new Random();
	/**
	 * args[0] - keyword
	 * args[1] - language
	 * args[2] - path to config file
	 */
	public static void main(String[] args){
		if(args.length < 3){
			System.out.print("Not enought arguments....");
		}else{
			System.out.println("Working Directory = " +  System.getProperty("user.dir")); 
			ConfigManager.getInstance().loadProperties(args[2]);
			System.out.println(args[0] + " " + args[1] + " " + args[2]);
			ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL);
			try {
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(
								ConfigManager.getInstance().getProperty(PROXY_LOGIN_LABEL),
								ConfigManager.getInstance().getProperty(PROXY_PASS_LABEL).toCharArray()
								);
					}
				});
				SnippetGeneratorThread generator = new SnippetGeneratorThread(args[0],args[1],ConfigManager.getInstance().getProperty(LINKS_LIST_FILE_PATH_LABEL),ConfigManager.getInstance().getProperty(PROXY_LIST_FILE_PATH_LABEL));
				Thread generatorThread = new Thread(generator);
				generatorThread.start();
				generatorThread.join(Long.valueOf(ConfigManager.getInstance().getProperty(MAX_EXECUTION_TIME_LABEL).trim()));
				if(generatorThread.isAlive()){
					generatorThread.stop();
					generatorThread.join();
				}
				System.out.print("Main thread finished.");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
