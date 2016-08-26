package com.fdt.utils;

public class  PreConditionCheck {
	public static String notEmpty(String value, String message){
		if (value == null || "".equals(value.trim())){
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public static int notZero(int value, String message){
		if (value == 0){
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public static int notZero(String stringValue, String message){		
		try {
			Integer value = Integer.parseInt(stringValue);
			if (value != 0){
				return value;
			}
		} catch(Exception e){
			
		}		
		throw new IllegalArgumentException(message);
	}
}
