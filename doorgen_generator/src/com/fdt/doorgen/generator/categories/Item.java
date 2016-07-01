package com.fdt.doorgen.generator.categories;

public class Item {
	
	private String key;
	
	public static Item parseItem(String key){
		Item newItem = new Item();
		newItem.key = key;
		return newItem;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	} 
}
