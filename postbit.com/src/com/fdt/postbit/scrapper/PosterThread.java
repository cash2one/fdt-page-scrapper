package com.fdt.postbit.scrapper;

import java.net.Proxy;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.fdt.postbit.scrapper.task.NewsTask;
import com.fdt.scrapper.proxy.ProxyConnector;
import com.fdt.scrapper.proxy.ProxyFactory;


public class PosterThread extends Thread{

    private static final Logger log = Logger.getLogger(PosterThread.class);

    private NewsTask task;
    private PostbitTaskFactory taskFactory;
    private ProxyFactory proxyFactory;
    private ArrayList<String> linkList = null;

    public PosterThread(NewsTask task, PostbitTaskFactory taskFactory, ProxyFactory proxyFactory, ArrayList<String> linkList) {
	this.task = task;
	this.taskFactory = taskFactory;
	this.proxyFactory = proxyFactory;
	this.linkList = linkList;
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
			Proxy proxy = proxyConnector.getConnect("SOCKS");
			PostbitNewsPoster ps;
			ps = new PostbitNewsPoster(task, proxy, taskFactory, linkList);
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
