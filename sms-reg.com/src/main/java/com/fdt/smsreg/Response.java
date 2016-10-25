package com.fdt.smsreg;

import com.fdt.smsreg.enums.SERVICE;
import com.fdt.smsreg.enums.STATUS;

public class Response {
	private String tzid;
	private SERVICE service;
	private String phone;
	private String answer;
	private STATUS status;
	
	public Response(String tzid, SERVICE service, String phone, String answer, STATUS status) {
		super();
		this.tzid = tzid;
		this.service = service;
		this.phone = phone;
		this.answer = answer;
		this.status = status;
	}
	
	
	public Response parseResponse(String json){
		//TODO Implement parsing response
		return null;
	}
	
	public String getTzid() {
		return tzid;
	}
	public SERVICE getService() {
		return service;
	}
	public String getPhone() {
		return phone;
	}
	public String getAnswer() {
		return answer;
	}
	public STATUS getStatus() {
		return status;
	}
	
	
}
