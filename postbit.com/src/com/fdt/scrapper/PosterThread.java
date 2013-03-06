package com.fdt.scrapper;

import java.net.Proxy;

import org.apache.log4j.Logger;

import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;
import com.fdt.scrapper.task.NewsTask;

public class PosterThread extends Thread{

    private static final Logger log = Logger.getLogger(PosterThread.class);

    private NewsTask task;
    private PostbitTaskFactory taskFactory;
    private ProxyFactory proxyFactory;

    public PosterThread(NewsTask task, PostbitTaskFactory taskFactory, ProxyFactory proxyFactory) {
	this.task = task;
	this.taskFactory = taskFactory;
	this.proxyFactory = proxyFactory;
    }

    @Override
    public void start(){
	taskFactory.incRunThreadsCount();
	super.start();
    }

    @Override
    public void run() {
	ProxyConnector proxyConnector = null;
	synchronized (this) {
	    try{
		boolean errorExist = false;
		try {
		    if(task.isResultEmpty()){
			proxyConnector = proxyFactory.getProxyConnector();
			log.debug("Free proxy count: " + (proxyFactory.getFreeProxyCount()-1));
			log.debug("Task (" + task.toString() +") is using proxy connection: " +proxyConnector.getProxyKey());
			Proxy proxy = proxyConnector.getConnect();
			PostbitNewsPoster ps;
			ps = new PostbitNewsPoster(task, proxy, taskFactory);
			String newsResult = ps.executePostNews();
			task.setResult(newsResult);
		    }
		}
		catch (Exception e) {
		    errorExist = true;
		    taskFactory.reprocessingTask(task);
		    log.error("Error occured during process task: " + task.toString(), e);
		}finally{
		    if(proxyConnector != null){
			proxyFactory.releaseProxy(proxyConnector);
			proxyConnector = null;
		    }
		}
		if(!errorExist){
		    if(!task.isResultEmpty()){
			taskFactory.putTaskInSuccessQueue(task);
		    }else{
			taskFactory.reprocessingTask(task);
		    }
		}
	    } finally {
		taskFactory.decRunThreadsCount(task);
	    }
	}
    }

    public NewsTask getTask(){
	return task;
    }
}
