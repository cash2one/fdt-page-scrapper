package com.fdt.doorgen.key.pooler;

import org.apache.log4j.Logger;

import com.fdt.scrapper.task.SnippetTask;
import com.fdt.scrapper.task.TaskFactory;

/**
 * Thread class for saving results to DB
 * 
 * @author Administrator
 *
 */
public class SaverThread extends Thread
{
	private static final Logger log = Logger.getLogger(SaverThread.class);

	protected static Long RUNNER_QUEUE_EMPTY_WAIT_TIME = 500L;

	private TaskFactory taskFactory;
	
	private boolean running = true;
	public boolean stopped = false;
	
	private IResultProcessor resultProcessor;

	public SaverThread(TaskFactory taskFactory, IResultProcessor resultProcessor) {
		super();
		this.taskFactory = taskFactory;
		this.resultProcessor = resultProcessor;
	}

	@Override
	public void start(){
		super.start();
	}

	@Override
	public void run() {
		synchronized (this) {
			try{
				while(running || !stopped){	
					if(stopped){
						running = false;
					}
					try{
						SnippetTask task = null;
						while(taskFactory.getSuccessQueue().size() > 0){
							task = taskFactory.getSuccessQueue().remove(0).getCurrentTask();
							if(task != null){
								//saveResultToFile(task.getResult(),task.getKeyWords().replace('+', ' '));
								resultProcessor.processResult(task);
							}

							continue;
						}
						try {
							this.wait(RUNNER_QUEUE_EMPTY_WAIT_TIME);
						} catch (InterruptedException e) {
							log.error("InterruptedException occured during RequestRunner process",e);
						}
					}catch(Throwable e){
						log.error("Error occured during saving result to DB",e);
					}
				}
			} finally{
			}
		}
	}
}
