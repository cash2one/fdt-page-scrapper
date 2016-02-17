package com.fdt.keywords.scrapper;

import java.util.ArrayList;
import java.util.Arrays;

public class KeyWord 
{
	//private static final String SPLIT_STR = "[\\s\\:\\-\\'\"\\%\\$\\-]+";
	private static final String SPLIT_STR = "[\\s\\:\\-\"\\%\\$\\-]+";
	
	private final String keyWord;
	
	private ArrayList<String> wordsList = new ArrayList<String>();
	
	public KeyWord(String keyWord)
	{
		this.keyWord = keyWord.toLowerCase();
		String[] list = keyWord.toLowerCase().split(SPLIT_STR);
		wordsList.addAll(Arrays.asList(list));
	}

	public String getKeyWord() {
		return keyWord;
	}

	public ArrayList<String> getWordsList() {
		return wordsList;
	}

	@Override
	public int hashCode() {
		int result = 0;
		for(String word : wordsList){
			result = result	+ ((word == null) ? 0 : word.hashCode());
		}
		return result;
	}
	
	public int size(){
		return wordsList.size();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		KeyWord other = (KeyWord) obj;
		if(other.size() != this.size())
			return false;
		
		if (wordsList == null && other.wordsList != null || wordsList != null && other.wordsList == null) 
				return false;
		
		if(wordsList.containsAll(other.wordsList) && other.wordsList.containsAll(wordsList))
			return true;
		
		return false;
	}

	@Override
	public String toString() {
		return "KeyWord [keyWord=" + keyWord + "]";
	}
}
