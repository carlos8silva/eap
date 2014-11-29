package com.netx.basic.R1.eh;


public class IntegrityException extends RuntimeException {

	public IntegrityException() {
		super();
	}
	
	public IntegrityException(String message) {
		super(message);
	}

	public IntegrityException(Throwable cause) {
		super(cause);
	}

	public IntegrityException(String message, Throwable cause) {
		super(message, cause);
	}

	public IntegrityException(boolean b) {
		super(b+"");
	}

	public IntegrityException(char c) {
		super(c+"");
	}

	public IntegrityException(byte b) {
		super(b+"");
	}

	public IntegrityException(short s) {
		super(s+"");
	}

	public IntegrityException(int i) {
		super(i+"");
	}

	public IntegrityException(long l) {
		super(l+"");
	}

	public IntegrityException(float f) {
		super(f+"");
	}

	public IntegrityException(double d) {
		super(d+"");
	}

	public IntegrityException(Object o) {
		super(o+"");
	}
}
