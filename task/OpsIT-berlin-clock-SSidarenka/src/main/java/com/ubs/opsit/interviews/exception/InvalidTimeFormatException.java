package com.ubs.opsit.interviews.exception;

public class InvalidTimeFormatException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1639174690033609240L;

	public InvalidTimeFormatException(String msg){
		super(msg);
	}
}
