package com.fdt.filemapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class RowMappingData 
{
	private static final Logger log = Logger.getLogger(RowMappingData.class);
	
	private static final String LINE_FEED = "\r\n";
	
	private File inputFile;

	private RowMapping mapping;
	private HashMap<String, String> mappingData = new HashMap<String, String>();

	public RowMappingData(File inputFile, RowMapping mapping) 
	{
		super();
		this.inputFile = inputFile;
		this.mapping = mapping;
		
		loadDataFromFile(this.inputFile);
	}

	public String getDataByName(String name){
		return mappingData.get(name) != null?mappingData.get(name):"";
	}
	
	public boolean updateDataByName(String name, String value){
		if(mappingData.containsKey(name)){
			mappingData.put(name, value);
			return true;
		}else{
			return false;
		}
	}
	
	public void setDataByName(String name, String value){
		mappingData.put(name, value);
	}
	
	public void loadDataFromFile(File inputFile)
	{
		//TODO Load file as string list
		BufferedReader br = null;
		List<String> fileStrList = new ArrayList<String>();
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8" ));

			String line = br.readLine();

			while(line != null) 
			{
				fileStrList.add(line);
				line = br.readLine();
			}

			if(br != null){
				br.close();
				br = null;
			}
			
			for(String key : mapping.getScopeNameList())
			{
				StringBuffer strBuf = new StringBuffer();
				RowScope rowScope = mapping.getRowScopeByName(key);
				
				int firstRow = rowScope.getFirst();
				int lastRow = rowScope.getLast();
				
				if(lastRow <= 0){
					lastRow = fileStrList.size()-lastRow;
				}
				
				for(int i = firstRow-1; i <= lastRow-1; i++)
				{
					if(strBuf.length() > 0)
					{
						strBuf.append(LINE_FEED);
					}
					
					strBuf.append(fileStrList.get(i));
				}
				
				mappingData.put(key, strBuf.toString());
			}
			
		} catch (FileNotFoundException e) {
			log.error("Reading input data file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading input data file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error closing stream", e);
			}
		}
	}
}
