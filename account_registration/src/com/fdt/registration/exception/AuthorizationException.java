package com.fdt.registration.exception;

@SuppressWarnings("serial")
public class AuthorizationException  extends Exception {
	public AuthorizationException() {
		super();
	}

	public AuthorizationException(Throwable t) {
		super(t);
	}

	public AuthorizationException(String s) {
		super(s);
	}

}