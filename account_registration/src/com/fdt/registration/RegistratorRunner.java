package com.fdt.registration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fdt.registration.account.Account;

public class RegistratorRunner {

	private final int MAX_THREAD_COUNT = 1;

	private static final Logger log = Logger.getLogger(RegistratorRunner.class);
	private final ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

	//private BlockingQueue <Future<Account>> threadResultList = new ArrayBlockingQueue<Future<Account>>(MAX_THREAD_COUNT);
	private List<Future<Account>> threadResultList = new ArrayList<Future<Account>>(MAX_THREAD_COUNT);
	private Object arrayLock = new Object();

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

		Future<Account> task = null;
		while(true){
			synchronized(arrayLock){
				try {
					if(task == null){
						String email = sapoRegistrator.getMailWorker().getEmail();
						Account account = new Account(email,email,email);
						task = pool.submit(new RegistratorThread(sapoRegistrator, account));
					}

					if(threadResultList.size() < MAX_THREAD_COUNT){
						threadResultList.add(task);
						task = null;
					}else{
						arrayLock.wait(500L);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			bufferWritter.flush();
			bufferWritter.close();
		}catch(IOException e){
			log.error("Error during saving account to result file.");
		}
	}

	private class ResultHandler implements Runnable{
		List<Future<Account>> threadResult; 

		public ResultHandler(List<Future<Account>> threadResultList){
			this.threadResult = threadResultList;
		}

		@Override
		public void run(){
			while(true)
			{
				try {
					synchronized(arrayLock){

						if(threadResult.size() > 0)
						{
							Future<Account> result = threadResult.remove(0);

							if(result.isDone()){
								if(result.get() != null){
									appendAccountToFile(result.get());
								}
								threadResult.remove(0);
							}else{
								threadResult.add(result);
							}
						}
						else{
							arrayLock.wait(500L);
						}
					}
				}
				catch (InterruptedException e) {
					log.error("Thread was interrupted.");
				} catch (ExecutionException e) {
					log.error("Error occured during getting registration result.", e);
				}
			}
		}
	}
}