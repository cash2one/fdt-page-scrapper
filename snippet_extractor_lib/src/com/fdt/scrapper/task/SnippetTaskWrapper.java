package com.fdt.scrapper.task;

import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fdt.scrapper.SnippetExtractor;

public class SnippetTaskWrapper {
	
	private static final Logger log = Logger.getLogger(SnippetTaskWrapper.class);
	
	private int cursorTask;

	private ArrayList<SnippetTask> tasks = new ArrayList<SnippetTask>();
	private int[] frequency = null;
	
	public SnippetTaskWrapper(String sourcesSrt, int[] frequencies, String key, String lang) throws Exception{
		String[] sources = null;
		
		if( sourcesSrt != null && !"".equals(sourcesSrt.trim()) && frequencies != null && frequencies.length > 0 ){
			sources = sourcesSrt.split(":");
			if(sources.length != frequencies.length){
				throw new IllegalArgumentException(String.format("Size of sources(%s) and frequency(%s) array should be equal.", sources.length, frequencies.length));
			}
			
			//init tasks array and frequency
			initSource(sources, frequencies,key,lang);
		}else{
			throw new IllegalArgumentException(String.format("String of sources(%s) and frequency(%s) array should not be empty.", sourcesSrt, frequencies));
		}
		
		selectRandTask();
	}
	
	public SnippetTaskWrapper(SnippetTask task){
		tasks.add(task);
		frequency = new int[1];
		frequency[0] = 1;
		
		selectRandTask();
	}
	
	private void initSource(String[] sources, int[] frequencies, String key, String lang) throws Exception{
		//init frequency
		frequency = new int[frequencies.length];
		frequency[0] = frequencies[0];
		for(int i = 1; i < frequencies.length; i++){
			frequency[i] = frequencies[i-1] + frequencies[i];
		}
		
		//init sources
		for(int i = 0; i < sources.length; i++){
			tasks.add(TaskFactory.makeSnippetTask(key, sources[i], lang));
		}
	}
	
	public SnippetTask selectRandTask(){
		Random rnd = new Random();
		int rndFreq= rnd.nextInt(frequency[frequency.length-1]);
		
		for(int i = 0; i < frequency.length; i++){
			if(rndFreq <= frequency[i] ){
				cursorTask = i;
				break;
			}
		}
		
		return getCurrentTask();
	}
	
	public SnippetTask getCurrentTask(){
		return tasks.get(cursorTask);
	}
	
	public int getAttemptCount(){
		int count = 0;
		for(int i = 0; i < frequency.length; i++){
			count += tasks.get(i).getAttemptCount();
		}
		return count;
	}
}
