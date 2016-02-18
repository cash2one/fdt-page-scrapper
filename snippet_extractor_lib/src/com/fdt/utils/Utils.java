package com.fdt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Utils {

	private static final Logger log = Logger.getLogger(Utils.class);

	public static synchronized List<String> loadFileAsStrList(String cfgFilePath)
	{
		return loadFileAsStrList(new File(cfgFilePath));
	}

	public static synchronized List<String> loadFileAsStrList(File cfgFile){
		ArrayList<String> linkList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader( new FileInputStream(cfgFile), "UTF8" ));

			String line = br.readLine();

			while(line != null)
			{
				String utf8Line = new String(line.getBytes(),"UTF-8");

				if( !"".equals(line.trim()))
				{
					linkList.add(utf8Line.trim());
				}

				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.error("Reading file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
		return linkList;
	}
	
	public static synchronized String loadFileAsString(File inputFile)
	{
		StringBuffer output  = new StringBuffer();
		
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new InputStreamReader( new FileInputStream(inputFile), "UTF8" ));

			String line = br.readLine();

			while(line != null)
			{
				String utf8Line = new String(line.getBytes(),"UTF-8");
				output.append(utf8Line).append(Constants.LINE_FEED);

				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.error("Reading file: FileNotFoundException exception occured",e);
		} catch (IOException e) {
			log.error("Reading file: IOException exception occured", e);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
				log.warn("Error while initializtion", e);
			}
		}
		return output.toString();
	}

	public static StringBuilder getResponseAsString(HttpURLConnection conn)
			throws IOException {
		InputStream is = conn.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder responseStr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			responseStr.append(line).append(Constants.LINE_FEED);
		}
		is.close();
		return responseStr;
	}
	
	public static void appendStringToFile(String str, File file) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();
		} catch (FileNotFoundException ex) {
			log.error("Error during saving string to file",ex);
		} catch (IOException ex) {
			log.error("Error during saving string to file",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error during closing output stream",ex);
			}
		}
	}
	
	public static void saveStringToFile(String str, File file, boolean append) {
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, append), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();
		} catch (FileNotFoundException ex) {
			log.error("Error during saving string to file",ex);
		} catch (IOException ex) {
			log.error("Error during saving string to file",ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				log.error("Error during closing output stream",ex);
			}
		}
	}
}
