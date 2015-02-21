package com.fdt.snippetutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UnusedTitleFinder {

	private static String pathToUsedList;
	private static String pathToFiles;
	private static String pathToToolDir;

	public UnusedTitleFinder() {
		super();
	}

	public static void main(String[] args) {

		try{
			if(args.length < 2){
				System.out.println("Some arguments are absent. Please use next list of arguments: 1 - pathToUsedList; 2 - output directory");
				System.exit(-1);
			}else{
				pathToUsedList = args[0].trim();
				pathToFiles = args[1].trim();
				pathToToolDir = args[2].trim();
			}

			String result = process(pathToUsedList, pathToFiles);
			appendStringToFile(result, new File(pathToToolDir));

			System.out.println(result);
		}catch(Exception e){
			System.exit(-1);
		}
	}

	private static String process(String pathToUsedList, String pathToFiles) throws IOException{
		String result = "";
		Random rnd = new Random();

		List<String> titleList= readFile(pathToUsedList);

		String[] fileNameArray= new File(pathToFiles).list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				File file = new File(dir,name); 
				if(file.isDirectory()){
					return false;
				}
				return true;
			}
		}
				);

		boolean isFound = false;
		int repeatCount = 0;
		while(!isFound && repeatCount < fileNameArray.length){
			repeatCount++;
			int rndIndex = rnd.nextInt(fileNameArray.length);
			if( !titleList.contains(fileNameArray[rndIndex]) ){
				result = pathToFiles + "\\" + fileNameArray[rndIndex];
				isFound = true;
			}
		}

		return result;
	}

	private static List<String> readFile(String filePath) throws IOException{

		FileReader fr = null;
		BufferedReader br = null;
		List<String> fileTitleList = new ArrayList<String>();
		String[] fileTitleArray;

		try {
			fr = new FileReader(new File(filePath));
			br = new BufferedReader(fr);

			String line = br.readLine();
			while(line != null){
				fileTitleList.add(line.trim());
				line = br.readLine();
			}

			//fileTitleArray = fileTitleList.toArray(new String[fileTitleList.size()]);
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (Throwable e) {
			}
			try {
				if(fr != null)
					fr.close();
			} catch (Throwable e) {
			}
		}

		return fileTitleList;
	} 

	private static void appendStringToFile(String str, File file) throws IOException {
		if(file.exists()){
			file.delete();
		}
		
		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8"));
			bufferedWriter.append(str);
			bufferedWriter.newLine();
		} finally {
			//Close the BufferedWriter
			if (bufferedWriter != null) {
				bufferedWriter.flush();
				bufferedWriter.close();
			}
		}
	}
}
