package com.fdt.registration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fdt.registration.account.Account;

public class RegistratorRunner {

	private final int MAX_THREAD_COUNT = 10;

	private static final Logger log = Logger.getLogger(RegistratorRunner.class);
	private final ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

	private BlockingQueue <Future<Account>> threadResultList = new ArrayBlockingQueue<Future<Account>>(MAX_THREAD_COUNT);

	public static void main(String[] args) {
		RegistratorRunner runner = new RegistratorRunner();
		runner.execute();
	}

	private void execute(){

		ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");

		IRegistrator sapoRegistrator = (IRegistrator) context.getBean("sapoRegistrator");

		Authenticator proxyAuth = (Authenticator) context.getBean("proxyAuthenticator");
		Authenticator.setDefault(proxyAuth);

		(new Thread(new ResultHandler(threadResultList))).start();

		while(true){
			try {
				synchronized(threadResultList){
					Account account = new Account();
					Future<Account> task = pool.submit(new RegistratorThread(sapoRegistrator, account));
					threadResultList.put(task);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void appendAccountToFile(Account account){
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

	private class ResultHandler implements Runnable{
		BlockingQueue<Future<Account>> threadResultList; 

		public ResultHandler(BlockingQueue<Future<Account>> threadResultList){
			this.threadResultList = threadResultList;
		}

		@Override
		public void run(){
			while(true){
				try {
					synchronized(threadResultList){
						Future<Account> result = threadResultList.take();
						if(result.isDone()){
							appendAccountToFile(result.get());
						}else{
							threadResultList.put(result);
						}
						wait(500L);
					}
				} catch (InterruptedException e) {
					log.error("Thread was interrupted.");
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
