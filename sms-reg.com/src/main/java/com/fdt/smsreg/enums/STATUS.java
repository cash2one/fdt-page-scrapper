package com.fdt.smsreg.enums;

public enum STATUS {
	//custom response value for return code 1
	TZ_OK("1","OK"),

	TZ_INPOOL("TZ_INPOOL","операция ожидает выделения номера"),
	TZ_NUM_PREPARE("TZ_NUM_PREPARE","выдан номер, ожидается выполнение метода SetReady"),
	TZ_NUM_WAIT("TZ_NUM_WAIT","ожидается ответ"),
	TZ_NUM_ANSWER("TZ_NUM_ANSWER","поступил ответ"),
	TZ_NUM_WAIT2("TZ_NUM_WAIT2","ожидается уточнение полученного кода"),
	TZ_NUM_ANSWER2("TZ_NUM_ANSWER2","поступил ответ после уточнения"),
	WARNING_NO_NUMS("WARNING_NO_NUMS","нету подходящих номеров"),

	//Также если время по операции уже истекло то получите следующие значения:
	TZ_OVER_OK("TZ_OVER_OK","операция завершена"),
	TZ_OVER_GR("TZ_OVER_GR","операция отмечена как ошибочная"),
	TZ_OVER_EMPTY("TZ_OVER_EMPTY","ответ не поступил за отведенное время"),
	TZ_OVER_NR("TZ_OVER_NR","вы не отправили запрос методом setReady"),
	TZ_OVER2_EMPTY("TZ_OVER2_EMPTY","уточнение не поступило за отведенное время"),
	TZ_OVER2_OK("TZ_OVER2_OK","операция завершена после уточнения"),
	TZ_DELETED("TZ_DELETED","операция удалена, средства возвращены");

	private String description;
	private String value;
	
	private STATUS(final String value, final String description){
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
