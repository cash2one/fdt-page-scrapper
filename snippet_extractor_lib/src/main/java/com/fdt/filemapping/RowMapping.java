package com.fdt.filemapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

public class RowMapping 
{
	private static final Logger log = Logger.getLogger(RowMapping.class);
	
	private File rowMappingFile;

	private HashMap<String, RowScope> mapping = new HashMap<String, RowScope>();

	public RowMapping(File rowMappingFile) 
	{
		super();
		this.rowMappingFile = rowMappingFile;
		
		loadRowScopeFile(this.rowMappingFile);
	}

	private void loadRowScopeFile(File rowMappingFile)
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(rowMappingFile), "UTF8" ));

			String line = br.readLine();

			while(line != null)	
			{
				if( !"".equals(line) )
				{
					String utf8Line = new String(line.getBytes(),"UTF-8");
					String[] data = utf8Line.split(":");
					mapping.put(data[0].trim(), RowScope.parseTmSrt(data[1])); 
				}
				line = br.readLine();
			}

			if(br != null){
				br.close();
				br = null;
			}
		} catch (FileNotFoundException e) {
			log.error("Reading mapping file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading mapping file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error closing stream", e);
			}
		}
	}
	
	public RowScope getRowScopeByName(String name){
		return mapping.get(name);
	}
	
	public Set<String> getScopeNameList(){
		return mapping.keySet();
	}
}
