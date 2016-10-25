package com.fdt.snippetutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fdt.utils.Utils;

public class DuplicateFileCleaner {

	private static String pathToFiles;

	private Map<String, List<File>> filesData = new HashMap<String, List<File>>();

	public DuplicateFileCleaner() {
		super();
	}

	public static void main(String[] args) {

		try{
			if(args.length < 1){
				System.out.println("Some arguments are absent. Please use next list of arguments: 1 - pathToFile4Clean");
				System.exit(-1);
			}else{
				pathToFiles = args[0].trim();
			}

			DuplicateFileCleaner cleaner = new DuplicateFileCleaner();

			String result = cleaner.process(pathToFiles);

			//System.out.println(result);
		}catch(Exception e){
			//System.exit(-1);
		}
	}

	private String process(String pathToFile4Clean) throws IOException{
		String result = "";

		File deleteDir = new File(new File(pathToFiles), "delete");
		if(!deleteDir.exists()){
			deleteDir.mkdirs();
		}

		//add record with empty first string

		File dummy = new File(deleteDir,"empty_dummy.txt");
		Utils.saveStringToFile(" ", dummy , false);

		List<File> emptyFileList = new ArrayList<File>();
		emptyFileList.add(dummy);

		filesData.put("", emptyFileList);

		for(File file : new File(pathToFile4Clean).listFiles()){
			if(file.exists() && file.isFile()){
				//TODO Read first string
				String fileFirstStrKey = Utils.loadFileAsString(file, 1, 1).trim();
				if( filesData.get(fileFirstStrKey) != null ){
					filesData.get(fileFirstStrKey).add(file);
				}else{
					List<File> fileList = new ArrayList<File>();
					fileList.add(file);
					filesData.put(fileFirstStrKey, fileList);
				}
			}
		}

		StringBuffer filesResult = new StringBuffer();

		for(String fileKey : filesData.keySet()){
			filesResult.append(fileKey).append("::");
			for(File file: filesData.get(fileKey)){
				filesResult.append(file.getName()).append(";");
			}
			filesResult.append("\r\n");
		}

		Utils.saveStringToFile(filesResult.toString(), new File("baza.txt"), false);

		cleanDuplicatedFiles(filesData);

		return result;
	}

	private void cleanDuplicatedFiles(Map<String, List<File>> filesData){
		for(String strKey : filesData.keySet()){
			List<File> filesList = filesData.get(strKey);
			filesList.remove(0);
			while(filesList.size() > 0){
				//TODO Move file to delete folder
				filesList.get(0).delete();
				filesList.remove(0);
			}
		}
	}
}
