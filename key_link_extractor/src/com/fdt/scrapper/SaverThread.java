package com.fdt.scrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
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
    private static int KEYS_BUFFER_SIZE = 10;

    private ArrayList<String> keysBuffer = new ArrayList<String>();

    private TaskFactory taskFactory;
    private String keysFilePath;
    private String outputLinksFilePath;

    public boolean running = true;

    public SaverThread(TaskFactory taskFactory, String keysFilePath, String outputLinksFilePath) {
	super();
	this.taskFactory = taskFactory;
	this.keysFilePath = keysFilePath;
	this.outputLinksFilePath = outputLinksFilePath;
    }

    @Override
    public void start(){
	super.start();
    }

    @Override
    public void run() {
	synchronized (this) {
	    try{
		while(running){	
		    try{
			SnippetTask task = null;
			if(taskFactory.getSuccessQueue().size() > 0){
			    task = taskFactory.getSuccessQueue().remove(0);
			    if(task != null){
				saveResultToFile(task.getResult());
				keysBuffer.add(task.getKeyWords().replace('+', ' '));
			    }

			    if(keysBuffer.size() >= KEYS_BUFFER_SIZE){
				//save rest_keys
				saveKeys(keysBuffer);
				keysBuffer.clear();
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
		//save keys
		saveKeys(keysBuffer);
		keysBuffer.clear();
	    }
	}
    }

    private void saveResultToFile(String content){
	ArrayList<String> contentArray = new ArrayList<String>();
	contentArray.add(content);
	saveResultToFile(contentArray);
    }

    private void saveResultToFile(ArrayList<String> content){
	BufferedWriter bufferedWriter = null;
	//save success tasks
	try {
	    //Construct the BufferedWriter object
	    log.debug("Starting saving success results...");
	    bufferedWriter = new BufferedWriter(bufferedWriter = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(new File(outputLinksFilePath),true), "UTF8")));
	    for(String line:content){
		bufferedWriter.write(line);
	    }
	    log.debug("Success results was saved successfully.");
	} catch (FileNotFoundException ex) {
	    log.error("Error occured during saving Success results",ex);
	} catch (IOException ex) {
	    log.error("Error occured during saving Success results",ex);
	} finally {
	    //Close the BufferedWriter
	    try {
		if (bufferedWriter != null) {
		    bufferedWriter.flush();
		    bufferedWriter.close();
		}
	    } catch (IOException ex) {
		log.error("Error occured during closing output streams during saving Success results",ex);
	    }
	}
    }

    public void saveKeys(ArrayList<String> keys){
	//read account list
	FileReader fr = null;
	BufferedReader br = null;

	BufferedWriter bufferedWriter = null;

	File restKeysFile = null;
	File restKeysFileNew = null;

	try {
	    restKeysFile = new File("rest_" + keysFilePath);
	    restKeysFileNew = new File("new_rest_" + keysFilePath);

	    if(!restKeysFile.exists()){
		File keysFile = new File(keysFilePath);
		//copy keys to rest_key
		FileUtils.copyFile(keysFile, restKeysFile);
	    }
	    fr = new FileReader(restKeysFile);
	    br = new BufferedReader(fr);

	    bufferedWriter = new BufferedWriter(bufferedWriter = new BufferedWriter(
		    new OutputStreamWriter(
			    new FileOutputStream(restKeysFileNew), 
		    "UTF8")
	    )
	    );

	    String line = br.readLine();
	    while(line != null){
		String utf8Line = new String(line.getBytes(),"UTF-8");
		if(!keys.contains(utf8Line)){
		    bufferedWriter.write(utf8Line);
		    bufferedWriter.newLine();
		}
		line = br.readLine();
	    }
	} catch (FileNotFoundException e) {
	    log.error("Reading PROPERTIES file: FileNotFoundException exception occured",e);
	} catch (IOException e) {
	    log.error("Reading PROPERTIES file: IOException exception occured", e);
	} finally {
	    try {
		if(br != null)
		    br.close();
	    } catch (Throwable e) {
		log.warn("Error while initializtion", e);
	    }
	    try {
		if(fr != null)
		    fr.close();
	    } catch (Throwable e) {
		log.warn("Error while initializtion", e);
	    }
	    try {
		if (bufferedWriter != null) {
		    bufferedWriter.flush();
		    bufferedWriter.close();
		}
	    } catch (IOException ex) {
		log.error("Error occured during closing output streams during saving Success results",ex);
	    }
	    try {
		if(restKeysFile.exists()){
		    restKeysFile.delete();
		}
		FileUtils.moveFile(restKeysFileNew, restKeysFile);
	    } catch (IOException e) {
		log.error("Error occured during moving files",e);
	    }
	}
    }
}
