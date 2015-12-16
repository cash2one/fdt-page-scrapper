package com.fdt.doorgen.key.pooler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;


public class TimeTable {
	private static final Logger log = Logger.getLogger(TimeTable.class);
	
	public static TimeString getTimeString(File timeTableFile){
		return loadTmTblFile(timeTableFile);
	}

	private static TimeString loadTmTblFile(File timeTableFile){
		TimeString timeStr = TimeString.DEFAUL_TM_STR;
		BufferedReader br = null;
		BufferedWriter bw = null;
		File tmpFile = new File(timeTableFile,"_tmp");
		boolean tnStrProcessed = false;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(timeTableFile), "UTF8" ));
			bw =  new BufferedWriter( new OutputStreamWriter( new FileOutputStream(tmpFile), "UTF8"));
			

			String line = br.readLine();
			
			while(line != null)	{
				if( !tnStrProcessed && !"".equals(line) ){
					tnStrProcessed = true;
					String utf8Line = new String(line.getBytes(),"UTF-8");
					timeStr = TimeString.parseTmSrt(utf8Line);
					line = br.readLine();
					continue;
				}
				
				bw.write(line);
				line = br.readLine();
			}
			
			if(br != null)
				br.close();
			
			tmpFile.renameTo(timeTableFile);
		} catch (FileNotFoundException e) {
			log.error("Reading PROPERTIES file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading PROPERTIES file: IOException exception occured", e);
		} finally {
			try {
				if(bw != null)
					bw.close();
			} catch (Throwable e) {
				log.warn("Error closing stream", e);
			}
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error closing stream", e);
			}
		}
		
		return timeStr;
	}
}