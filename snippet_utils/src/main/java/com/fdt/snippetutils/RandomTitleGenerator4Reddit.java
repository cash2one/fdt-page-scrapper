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

import com.fdt.utils.Utils;

public class RandomTitleGenerator4Reddit {

	private static String path2TitlesList;
	private static String path2FirstWords;
	private static String path2ResultFile;
	private static int repeatCount;

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

	public RandomTitleGenerator4Reddit() {
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
				path2TitlesList = args[0].trim();
				path2FirstWords = args[1].trim();
				path2ResultFile = args[2].trim();
				repeatCount =  Integer.valueOf(args[3].trim());
			}

			RandomTitleGenerator4Reddit generator = new RandomTitleGenerator4Reddit();
			boolean newFile = true;
			
			for(int i = 0; i < repeatCount; i++){
				String result = generator.process(path2TitlesList, path2FirstWords);
				generator.appendStringToFile(result, new File(path2ResultFile), true && newFile);
				newFile = false;
			}

			System.out.println("Completed.");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private String process(String pathToTitlesList, String pathToFirstWords) throws IOException{
		Random rnd = new Random();

		StringBuffer strBufRslt = new StringBuffer();

		List<String> titleList= Utils.loadFileAsStrList(pathToTitlesList);
		List<String> firstWrdList= Utils.loadFileAsStrList(pathToFirstWords);

		for(String title : titleList){
			String newTitle = processTitle(title, firstWrdList.get(rnd.nextInt(firstWrdList.size())));
			strBufRslt.append(newTitle).append("\r\n");
		}
		
		if(strBufRslt.length() > 0){
			strBufRslt.setLength(strBufRslt.length() - "\r\n".length());
		}

		return strBufRslt.toString();
	}

	private String processTitle(String title, String firstRndWrd){
		StringBuffer newTitle = new StringBuffer();
		String filmName = "";
		String firstTtlWrd = "";
		ArrayList<String> lastWrds = new ArrayList<String>();

		Pattern ptrn = Pattern.compile("^(.*)\\{(.*?)\\}(.*)$");
		Matcher mtchr = ptrn.matcher(title);

		if(mtchr.matches()){
			firstTtlWrd = mtchr.group(1).trim();
			filmName = mtchr.group(2).trim();
			lastWrds  = new ArrayList<String>(Arrays.asList(mtchr.group(3).trim().split("\\s")));

			//shuffle collection
			Collections.shuffle(lastWrds);

			newTitle.append(CONVERTERS.RND_CHARSGROUP_START_END.convert(CONVERTERS.rndConvertor(firstRndWrd)));
			newTitle.append(CONVERTERS.RND_CHARSGROUP_START_END.convert(CONVERTERS.rndConvertor(firstTtlWrd))).append(getRndSpace());
			newTitle.append("\"").append(filmName).append("\"").append(getRndSpace());

			for(String word : lastWrds)
			{
				String word4Cnvrt = word.trim();
				
				if(rnd.nextInt(4) > 1){
					word4Cnvrt = CONVERTERS.rndConvertor(word);

					/*if(rnd.nextBoolean()){
					word4Cnvrt = CONVERTERS.rndConvertor(word4Cnvrt);
				}*/

					newTitle.append(word4Cnvrt).append(getRndSpace());
				}else{
					newTitle.append(word4Cnvrt).append(getRndSpace());
				}
			}

			newTitle.append(getRndStr());
		}else{
			return title;
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

	protected static interface IWordConverterAlgorithm
	{
		public String convert(String input);
	}
	
	protected static class EmptyConverter implements IWordConverterAlgorithm
	{

		@Override
		public String convert(String input) {
			return input;
		}
	}

	protected static class AlgPointInserter implements IWordConverterAlgorithm{

		@Override
		public String convert(String input) {
			StringBuffer strBuf = new StringBuffer();

			for(char oneChar : input.toCharArray()){
				strBuf.append(oneChar).append('.');
			}

			if(strBuf.length() > 0){
				strBuf.setLength(strBuf.length()-1);
			}

			return strBuf.toString();
		}
	}

	protected static class AlgRndPointInserter implements IWordConverterAlgorithm{

		@Override
		public String convert(String input) {
			StringBuffer strBuf = new StringBuffer();

			for(char oneChar : input.toCharArray()){
				if(rnd.nextBoolean()){
					strBuf.append(oneChar).append('.');
				}else{
					strBuf.append(oneChar);
				}
			}

			if( strBuf.length() > 0 && strBuf.lastIndexOf(".") == (strBuf.length()-1) ){
				strBuf.setLength(strBuf.length()-1);
			}

			return strBuf.toString();
		}
	}

	protected static class AlgRndCaseCharsInserter implements IWordConverterAlgorithm{

		@Override
		public String convert(String input) {
			StringBuffer strBuf = new StringBuffer();

			for(char oneChar : input.toCharArray()){
				if(rnd.nextBoolean()){
					strBuf.append(Character.toUpperCase(oneChar));
				}else{
					strBuf.append(Character.toLowerCase(oneChar));
				}
			}

			return strBuf.toString();
		}
	}

	protected static class AlgUpperCaseCharsInserter implements IWordConverterAlgorithm{

		@Override
		public String convert(String input) {
			return input.toUpperCase();
		}
	}

	protected static class AlgAppendRndCharsAtStartAndEndInserter implements IWordConverterAlgorithm{

		@Override
		public String convert(String input) {
			StringBuffer strBuf = new StringBuffer();
			StringBuffer start = new StringBuffer();
			StringBuffer end = new StringBuffer();

			int rndCount = 1 + rnd.nextInt(3);

			for(int i = 0; i < rndCount; i++){
				String rndChar = specSymbols[rnd.nextInt(specSymbols.length)]; 
				start.append(rndChar);
				end.insert(0, rndChar);
			}

			strBuf.append(start).append(input).append(end);

			return strBuf.toString();
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
