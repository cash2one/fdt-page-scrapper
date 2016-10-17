package com.fdt.reddit;

@SuppressWarnings("serial")
public class LoginBannedException  extends Exception 
{
	public LoginBannedException() {
		super();
	}

	public LoginBannedException(Throwable t) {
		super(t);
	}

	public LoginBannedException(String s) {
		super(s);
	}

	public LoginBannedException(String s, Throwable t) {
		super(s, t);
	}
}