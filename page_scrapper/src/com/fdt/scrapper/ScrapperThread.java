package com.fdt.scrapper;

import java.net.Proxy;

import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.PageTasks;
import com.fdt.scrapper.task.Task;

public class ScrapperThread extends Thread{

	private static final Logger log = Logger.getLogger(ScrapperThread.class);

	private PageTasks tasks;
	private TaskFactory taskFactory;
	private ProxyFactory proxyFactory;

	public ScrapperThread(PageTasks tasks, TaskFactory taskFactory, ProxyFactory proxyFactory) {
		this.tasks = tasks;
		this.taskFactory = taskFactory;
		this.proxyFactory = proxyFactory;
	}

	public void start(){
		taskFactory.incRunThreadsCount();
		super.start();
	}

	@Override
	public void run() {
		boolean taskReturned = false;
		ProxyConnector proxyConnector = null;
		synchronized (this) {
			try{
				for(Task task : tasks.getTasks()){
					try {
						if(task.isResultEmpty()){
							proxyConnector = proxyFactory.getProxyConnector();
							log.debug("Free proxy count: " + (proxyFactory.getFreeProxyCount()-1));
							log.debug("Task (" + task.toString() +") is using proxy connection: " +proxyConnector.getProxyKey());
							Proxy proxy = proxyConnector.getConnect();
							PageScrapper ps;
							ps = new PageScrapper(task, proxy);
							task.setResult(ps.extractResult());
							taskFactory.putTaskInSuccessQueue(tasks);
						}
						
					}
					catch (Exception e) {
						taskFactory.reprocessingTask(tasks);
						log.error("Error occured during process task: " + task.toString(), e);
						break;
					}finally{
					    if(proxyConnector != null){
						proxyFactory.releaseProxy(proxyConnector);
						proxyConnector = null;
					    }
					}
				}
			} finally {

				taskFactory.decRunThreadsCount(tasks);
			}
		}
	}

	public PageTasks getTasks(){
		return tasks;
	}
}
