package com.fdt.registration.exception;

@SuppressWarnings("serial")
public class NoRegisteredException extends Exception {
	public NoRegisteredException() {
		super();
	}

	public NoRegisteredException(Throwable t) {
		super(t);
	}

	public NoRegisteredException(String s) {
		super(s);
	}

}
