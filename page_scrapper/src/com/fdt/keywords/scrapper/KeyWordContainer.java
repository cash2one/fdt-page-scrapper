package com.fdt.keywords.scrapper;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class KeyWordContainer {
	
	private Set<KeyWord> keyWords = new HashSet<KeyWord>();
	
	public KeyWordContainer(File input){
		super();
		loadFromFile(input);
	}
	
	private void loadFromFile(File input){
		
	}

	public Set<KeyWord> getKeyWords() {
		return keyWords;
	}
}
