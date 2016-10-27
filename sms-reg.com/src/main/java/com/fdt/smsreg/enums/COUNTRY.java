package com.fdt.smsreg.enums;

public enum COUNTRY {
	all("all","любой свободный номер мобильного телефона;"),
	ru("ru","номер российских операторов;"),
	ua("ua","номер украинских операторов;"),
	RU_MOS("RU-MOS","Москва и московская область"),
	RU_LEN("RU-LEN","Санкт-Петербург и Ленинградская область");

	private String description;
	private String value;
	
	private COUNTRY(final String value, final String description){
		this.value = value;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public String getValue() {
		return value;
	}
	
}
