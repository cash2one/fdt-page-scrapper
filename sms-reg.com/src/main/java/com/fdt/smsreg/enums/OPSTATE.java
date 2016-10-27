package com.fdt.smsreg.enums;

public enum OPSTATE {

	active("active","текущие незавершенные операции;"),
	completed("completed","операции, которые уже завершились;"); 
	
	private String description;
	private String value;
	
	private OPSTATE(final String value, final String description){
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
