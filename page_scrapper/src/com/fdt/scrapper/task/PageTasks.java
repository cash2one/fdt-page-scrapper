package com.fdt.scrapper.task;

import java.util.ArrayList;

import com.fdt.scrapper.Domain;
import com.fdt.scrapper.exception.ScrapperException;

public class PageTasks
{
	public static final String ITEMS_SEPARATOR = ";";
	private int attempsCount = 1;
	private ArrayList<Task> tasks = new ArrayList<Task>();

	private Domain domain;

	public PageTasks(Domain domain)
	{
		super();
		this.domain = domain;
		fillTask();
	}
	
	private void fillTask(){
		tasks.add(new AlexaTask(domain));
		tasks.add(new GoogleAllTimeTask(domain));
		tasks.add(new GoogleWeekTask(domain));
	}

	public void addTask(Task task) throws ScrapperException{
		task.setUrlToScrap(domain.getName());
		tasks.add(task);
	}

	public String getUrlToScrap() throws ScrapperException{
		return domain.getName();
	}

	public ArrayList<Task> getTasks() {
		return tasks;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("---Tasks for URL: ").append(domain.getName()).append("\r\n");
		for(Task task:tasks){
			sb.append(task.toString()).append("\r\n");
		}
		return sb.toString();
	}

	public int getAttempsCount() {
		return attempsCount;
	}

	public void incAttempsCount() {
		this.attempsCount++;
	}
	
	public String toCsv(){
		StringBuilder csvStr = new StringBuilder();
		csvStr.append(domain.getName()).append(ITEMS_SEPARATOR).append(domain.getCount()).append(ITEMS_SEPARATOR);
		for(Domain subDomain : domain.getSubDomainsList()){
			csvStr.append(subDomain.getName()).append(":").append(subDomain.getCount()).append(",");
		}
		if(domain.getSubDomainsList().size() > 0){
			csvStr.setLength(csvStr.length()-1);
		}
		csvStr.append(ITEMS_SEPARATOR);
		
		for(Task task : tasks){
			csvStr.append(task.getResult()).append(ITEMS_SEPARATOR);
		}
		if(tasks.size() > 0){
			csvStr.setLength(csvStr.length()-1);
		}
		
		return csvStr.toString();
	}

	public Domain getDomain()
	{
	    return domain;
	}
}
