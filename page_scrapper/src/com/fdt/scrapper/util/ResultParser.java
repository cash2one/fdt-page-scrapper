package com.fdt.scrapper.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.fdt.scrapper.Domain;
import com.fdt.scrapper.task.AlexaTask;
import com.fdt.scrapper.task.GoogleAllTimeTask;
import com.fdt.scrapper.task.GoogleWeekTask;
import com.fdt.scrapper.task.PageTasks;
import com.fdt.scrapper.task.Task;

public class ResultParser {
	
	private static final Logger log = Logger.getLogger(ResultParser.class);

	public static void main(String[] args){
		ResultParser rp = new ResultParser();
		ArrayList<PageTasks> pt = rp.parseResultFile("success_result.csv");
		System.out.println(pt.toString());
	}

	public ArrayList<PageTasks> parseResultFile(String resultFilePath){
		ResultParserFilter filter = new ResultParserFilter();
		return parseResultFile(resultFilePath, filter);
	}

	public ArrayList<PageTasks> parseResultFile(String resultFilePath, ResultParserFilter filter){
		ArrayList<PageTasks> tasksList = new ArrayList<PageTasks>();
		BufferedReader bufferedReader = null;
		try
		{
			bufferedReader = new BufferedReader(new FileReader(resultFilePath));
			String line = bufferedReader.readLine();
			while(line != null){
				String[] values = line.split(PageTasks.ITEMS_SEPARATOR,6);
				PageTasks tasks = parseLine(values);
				tasksList.add(tasks);
				line = bufferedReader.readLine();
			}
		} catch (FileNotFoundException ex) {
			log.error("Error occured during parsing results",ex);
		} catch (IOException ex) {
			log.error("Error occured during parsing results",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException ex) {
				log.error("Error occured during closing input streams during parsing results",ex);
			}
		}
		return tasksList;
	}

	private PageTasks parseLine(String[] values){
		Domain domain = new Domain(values[0]);
		domain.setCount(Integer.valueOf(values[1]));
		String[] subDomains = null;
		if(!isEmpty(values[2])){
			subDomains = values[2].split(",");
		}
		if(subDomains != null){
			for(String subDomain:subDomains){
				String[] subDomainInfo = subDomain.split(":");
				Domain sbDomain = new Domain(subDomainInfo[0]);
				sbDomain.setCount(Integer.valueOf(subDomainInfo[1]));
				domain.getSubDomainsList().add(sbDomain);
				domain.getSubDomainsIndexList().put(sbDomain.getName(), domain.getSubDomainsList().size()-1);
			}
		}

		PageTasks tasks = new PageTasks(domain);
		for(Task task:tasks.getTasks()){
			int taskIndex = -1;
			if(task instanceof AlexaTask){
				taskIndex = 3;
			}else if(task instanceof GoogleAllTimeTask){
				taskIndex = 4;
			}else if(task instanceof GoogleWeekTask){
				taskIndex = 5;
			}

			if(taskIndex != -1 && !isEmpty(values[taskIndex])){
				task.setResultAsIs(new ArrayList<String>(Arrays.asList(values[taskIndex].split(":"))));
			} else{
				task.setResultAsIs(new ArrayList<String>(Arrays.asList("-1")));
			}
		}

		return tasks;
	}

	public ArrayList<PageTasks> filterResults(ArrayList<PageTasks> tasks, ResultParserFilter filter){
		ArrayList<PageTasks> filtered = new ArrayList<PageTasks>();
		//check filter
		if(!filter.filterExist()){
			//add all results
			return tasks;
		}else{
			for(PageTasks task:tasks){
				if(task.getDomain().getCount() >= filter.getMinDomainCount()){
					long alexaIndex =  task.getTasks().get(0).getResult()==null?-1:Long.valueOf(task.getTasks().get(0).getResult().get(0));
					long googleAll =  task.getTasks().get(1).getResult()==null?-1:Long.valueOf(task.getTasks().get(1).getResult().get(0));
					long googleWeek =  task.getTasks().get(2).getResult()==null?-1:Long.valueOf(task.getTasks().get(2).getResult().get(0));
					if(alexaIndex <= filter.getMaxAlexaRank() && alexaIndex > 0 &&
							googleAll >= filter.getMinAllIndex() &&
							googleWeek >= filter.getMinWeekIndex()){
						log.debug("Task filtered: " + task);
						filtered.add(task);
					}
				}
			}
		}
		log.debug("Total task ["+tasks.size()+"]; filtered task["+filtered.size()+"]");
		return filtered;
	}

	private boolean isEmpty(String str){
		if(str == null || "".equals(str.trim())){
			return true;
		}
		return false;
	}
}
