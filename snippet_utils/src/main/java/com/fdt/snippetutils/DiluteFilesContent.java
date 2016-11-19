package com.fdt.snippetutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fdt.snippetutils.RandomTitleGenerator4Reddit.AlgAppendRndCharsAtStartAndEndInserter;
import com.fdt.snippetutils.RandomTitleGenerator4Reddit.AlgDivederByRndSpecSymblolsInserter;
import com.fdt.snippetutils.RandomTitleGenerator4Reddit.AlgPointInserter;
import com.fdt.snippetutils.RandomTitleGenerator4Reddit.AlgRndCaseCharsInserter;
import com.fdt.snippetutils.RandomTitleGenerator4Reddit.AlgRndPointInserter;
import com.fdt.snippetutils.RandomTitleGenerator4Reddit.AlgUpperCaseCharsInserter;
import com.fdt.snippetutils.RandomTitleGenerator4Reddit.EmptyConverter;
import com.fdt.snippetutils.RandomTitleGenerator4Reddit.IWordConverterAlgorithm;
import com.fdt.utils.Utils;

public class DiluteFilesContent {
	private static String path2InputFolder;
	private static String path2OutputFolder;

	//private static String path2FirstWords;
	//private static int repeatCount;

	private static Random rnd = new Random();
	//private static final String[] specSymbols = new String[]{"\"","!","@","#","$","%","^","&","*","(",")","_","+","|","~","-","'","{","}","[","]","<",">","?",".",",",";",":"};
	private static final String[] specSymbols = new String[]{"\"","!","@","$","%","^","&","*","(",")","_","+","|","~","-","'","{","}","[","]","<",">","?",".",",",";",":"};

	public enum CONVERTERS
	{
		EMPTY(new EmptyConverter(), true),
		POINTER_FULL(new AlgPointInserter(), true),
		POINTER_RND(new AlgRndPointInserter(), true),
		SPEC_SYMBOLS_DIVIDER_RND(new AlgDivederByRndSpecSymblolsInserter(), true),
		CHARCASE_RND(new AlgRndCaseCharsInserter(), true),
		UPPERCASE(new AlgUpperCaseCharsInserter(), true),
		RND_CHARSGROUP_START_END(new AlgAppendRndCharsAtStartAndEndInserter(), false);

		private CONVERTERS(IWordConverterAlgorithm converter, boolean isSerial){
			this.converter = converter;
			this.isRndApplicable = isSerial;
		}

		public String convert(String input){
			return converter.convert(input);
		}

		public static String rndConvertor(String input){

			CONVERTERS[] cnvrtArray = CONVERTERS.values();
			CONVERTERS cnvrt = cnvrtArray[rnd.nextInt(cnvrtArray.length)];
			while(!cnvrt.isRndApplicable){
				cnvrt = cnvrtArray[rnd.nextInt(cnvrtArray.length)];
			}

			return cnvrt.convert(input);
		}

		private IWordConverterAlgorithm converter;
		private boolean isRndApplicable;			
	}

	public DiluteFilesContent() {
		super();
	}

	public static void main(String[] args) {

		try{
			if(args.length < 2){
				System.out.println("Some arguments are absent. Please use next list of arguments: 1 - inputFolder; 2 - outputFolder;");
				System.exit(-1);
			}else{
				path2InputFolder = args[0].trim();
				path2OutputFolder = args[1].trim();
				//path2FirstWords = args[1].trim();
				//repeatCount =  Integer.valueOf(args[3].trim());
			}

			for(File inFile : new File(path2InputFolder).listFiles()){
				DiluteFilesContent generator = new DiluteFilesContent();

				String result = generator.process(inFile);
				Utils.saveStringToFile(result, new File(path2OutputFolder), false);
			}

			System.out.println("Completed.");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private String process(File inFile) throws IOException{
		Random rnd = new Random();

		StringBuffer strBufRslt = new StringBuffer();

		List<String> strList= Utils.loadFileAsStrList(inFile);

		for(String title : strList){
			if(!title.equals("[KEYWORD]")){
				String newStr = processStr(title);
				strBufRslt.append(newStr).append("\r\n");
			}
		}

		if(strBufRslt.length() > 0){
			strBufRslt.setLength(strBufRslt.length() - "\r\n".length());
		}

		return strBufRslt.toString();
	}

	private String processStr(String input){
		StringBuffer resutlStr = new StringBuffer();

		
		try{
			String list[] = input.split("\\s+");

			for(int i = 0; i < list.length; i++){
				//process word
				if(rnd.nextInt(10) < 1){
					// PRocess word
					resutlStr.append(convertStrRnd(list[i]));
				}else{
					resutlStr.append(list[i]);
				}

				//process space
				if((i < list.length-1) && rnd.nextInt(100) < 65){
					// PRocess space
					resutlStr.append(getRndSpaceSymbols());
				}else{
					resutlStr.append(" ");
				}

			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return resutlStr.toString();
	}

	private String convertStrRnd(String str) {
		return CONVERTERS.SPEC_SYMBOLS_DIVIDER_RND.converter.convert(str);
	}

	private String getRndSpaceSymbols() {
		return specSymbols[rnd.nextInt(specSymbols.length)];
	}

	private void appendStringToFile(String str, File file, boolean newFile) throws IOException {
		if(newFile && file.exists()){
			file.delete();
		}

		BufferedWriter bufferedWriter = null;
		try {
			//Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, !newFile), "UTF8"));
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

	private static String getRndStr(){
		Random random = new Random();
		int length = 2 + random.nextInt(2);
		String characters = "abcdefghijklmnopqrstuvwxyz0123456789,.!@#$%^&*()-=|{}[]<>/?~`\":;";
		char[] text = new char[length];
		for (int i = 0; i < length; i++)
		{
			text[i] = characters.charAt(random.nextInt(characters.length()));
		}
		return new String(text);
	}

	private static String getRndSpace(){
		Random rnd = new Random();
		int length = 1 + rnd.nextInt(2);
		String characters = ",.~:;+-";
		StringBuffer text = new StringBuffer();

		if(rnd.nextInt(4) > 1){
			for (int i = 0; i < length; i++)
			{
				text.append(characters.charAt(rnd.nextInt(characters.length())));
			}

			if(rnd.nextBoolean()){
				text.append(" ");
			}
		}else{
			text.append(" ");
		}

		return text.toString();
	}

}
