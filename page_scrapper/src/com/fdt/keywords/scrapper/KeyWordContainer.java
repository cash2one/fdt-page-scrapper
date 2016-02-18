package com.fdt.keywords.scrapper;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fdt.utils.Constants;
import com.fdt.utils.Utils;

public class KeyWordContainer {
	
	private Set<KeyWord> keyWords = new HashSet<KeyWord>();
	
	public KeyWordContainer(File input){
		super();
		loadFromFile(input);
	}
	
	private void loadFromFile(File input){
		
		List<String> strList =  Utils.loadFileAsStrList(input);
		for(String str : strList){
			keyWords.add(new KeyWord(str));
		}
	}

	public Set<KeyWord> getKeyWords() {
		return keyWords;
	}
	
	public void saveKeysToFile(File file)
	{
		StringBuffer strBuf = new StringBuffer();
		
		int buffered = 0;
		
		for(KeyWord keyWord : keyWords)
		{
			strBuf.append(keyWord.getKeyWordCleaned()).append(Constants.LINE_FEED);
			buffered++;
			
			if(buffered > 1000){
				strBuf.setLength(strBuf.length() - Constants.LINE_FEED.length());
				Utils.appendStringToFile(strBuf.toString(), file);
				
				strBuf.setLength(0);
				buffered = 0;
			}
		}
		
		if(strBuf.length() > 0){
			strBuf.setLength(strBuf.length() - Constants.LINE_FEED.length());
			Utils.appendStringToFile(strBuf.toString(), file);
		}
	}
}
