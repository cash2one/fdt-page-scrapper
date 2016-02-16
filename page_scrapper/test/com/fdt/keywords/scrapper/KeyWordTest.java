package com.fdt.keywords.scrapper;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class KeyWordTest {
	
	private String str1;
	private String str2;
	private String str3;
	
	private KeyWord key1;
	private KeyWord key2;
	private KeyWord key3;
	
	@Before
	public void init(){
		str1 = "Payday online Loans connected";
		str2 = "online loans connected payday";
		str3 = "online Loans connected Payday Other";
		//  create mock
		key1 = new KeyWord(str1);
		key2 = new KeyWord(str2);
		key3 = new KeyWord(str3);
	}

	@Test
	public void testHashCodeEquals()  {
		

		// use mock in test.... 
		assertEquals(key1.hashCode(), key2.hashCode());
		assertEquals(true, key1.equals(key2));
		assertEquals(true, key2.equals(key1));
		assertEquals(false, key3.equals(key1));
		assertEquals(false, key1.equals(key3));
	}

	@Test
	public void testKeyWordsSet()  {
		Set<KeyWord> keyWordSet = new HashSet<KeyWord>();
		keyWordSet.add(key1);
		keyWordSet.add(key2);
		keyWordSet.add(key3);
		
		assertEquals(2, keyWordSet.size());
		
	}
	
}
