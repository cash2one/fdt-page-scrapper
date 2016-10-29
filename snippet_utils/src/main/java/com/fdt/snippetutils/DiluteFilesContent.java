package com.fdt.snippetutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fdt.snippetutils.RandomTitleGenerator4Reddit.AlgAppendRndCharsAtStartAndEndInserter;
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
	private static final String[] specSymbols = new String[]{"\"","!","@","#","$","%","^","&","*","(",")","_","+","|","~","-","'","{","}","[","]","<",">","?",".",",",";",":"};

	private ArrayList<IWordConverterAlgorithm> convertes = new ArrayList<IWordConverterAlgorithm>();

	public enum CONVERTERS
	{
		EMPTY(new EmptyConverter(), true),
		POINTER_FULL(new AlgPointInserter(), true),
		POINTER_RND(new AlgRndPointInserter(), true),
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
		convertes.add(new AlgPointInserter());
		convertes.add(new AlgRndPointInserter());
		convertes.add(new AlgRndCaseCharsInserter());
		convertes.add(new AlgUpperCaseCharsInserter());
		convertes.add(new AlgAppendRndCharsAtStartAndEndInserter());

	}

	public static void main(String[] args) {

		try{
			if(args.length < 4){
				System.out.println("Some arguments are absent. Please use next list of arguments: 1 - pathToTitlesList; 2 - pathToFirstWords; 3 - path2ResultFile; 4 - repeat count");
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
				generator.appendStringToFile(result, new File(path2OutputFolder), true);
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
			String newStr = processTitle(title);
			strBufRslt.append(newStr).append("\r\n");
		}

		if(strBufRslt.length() > 0){
			strBufRslt.setLength(strBufRslt.length() - "\r\n".length());
		}

		return strBufRslt.toString();
	}

	private String processTitle(String str){
		StringBuffer newTitle = new StringBuffer();
		String filmName = "";
		String firstTtlWrd = "";

		Pattern ptrn = Pattern.compile("^(.*)\\{(.*?)\\}(.*)$");
		Matcher mtchr = ptrn.matcher(str);

		if(mtchr.matches()){
			firstTtlWrd = mtchr.group(1).trim();
			filmName = mtchr.group(2).trim();

			newTitle.append(CONVERTERS.RND_CHARSGROUP_START_END.convert(CONVERTERS.rndConvertor(firstTtlWrd))).append(getRndSpace());
			newTitle.append("\"").append(filmName).append("\"").append(getRndSpace());

			newTitle.append(getRndStr());
		}else{
			return str;
		}

		return newTitle.toString();
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
