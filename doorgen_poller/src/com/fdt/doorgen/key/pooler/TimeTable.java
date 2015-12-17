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

	public static void returnTimeSrt(TimeString timeStr, File timeTableFile){
		loadTmTblFile(timeTableFile, false, timeStr);
	}

	public static TimeString loadTmTblFile(File timeTableFile){
		return loadTmTblFile(timeTableFile, true, null);
	}
	
	private static TimeString loadTmTblFile(File timeTableFile, boolean isGettingStr, TimeString timeSrtRet){
		TimeString timeStr = TimeString.DEFAUL_TM_STR;
		BufferedReader br = null;
		BufferedWriter bw = null;
		File tmpFile = new File(timeTableFile.getName() + ".tmp");
		boolean tmStrProcessed = false;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(timeTableFile), "UTF8" ));
			bw =  new BufferedWriter( new OutputStreamWriter( new FileOutputStream(tmpFile,false), "UTF8"));

			String line = br.readLine();

			while(line != null)	{
				//if get time string
				if(isGettingStr){
					if( !tmStrProcessed && !"".equals(line) ){
						tmStrProcessed = true;
						String utf8Line = new String(line.getBytes(),"UTF-8");
						timeStr = TimeString.parseTmSrt(utf8Line);
						line = br.readLine();
						continue;
					}
				}else{
					//if return time string
					if( !tmStrProcessed){
						tmStrProcessed = true;
						bw.write(timeSrtRet.toString());
						bw.write("\r\n");
						timeStr = timeSrtRet;
					}
				}

				if(!"".equals(line)){
					bw.write(line);
					bw.write("\r\n");
				}
				line = br.readLine();
			}

			if(br != null){
				br.close();
				br = null;
			}

			if(bw != null){
				bw.close();
				bw = null;
			}

			timeTableFile.delete();
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