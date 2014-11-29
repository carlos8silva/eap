package com.netx.basic.R1.eh;


public class IllegalUsageException extends RuntimeException {

	public IllegalUsageException(String message) {
		super(message);
	}

	public IllegalUsageException(String message, Throwable cause) {
		super(message, cause);
	}
}
