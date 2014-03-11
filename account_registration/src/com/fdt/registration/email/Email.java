package com.fdt.registration.email;

public class Email {
	
	private String address;
	private String messageFrom;
	private String title;
	private String htmlBody;
	
	public Email(){
		super();
	}
	
	public Email(String address, String messageFrom, String title,
			String htmlBody) {
		super();
		this.address = address;
		this.messageFrom = messageFrom;
		this.title = title;
		this.htmlBody = htmlBody;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMessageFrom() {
		return messageFrom;
	}
	public void setMessageFrom(String messageFrom) {
		this.messageFrom = messageFrom;
	}
	public String getHtmlBody() {
		return htmlBody;
	}
	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}
}
