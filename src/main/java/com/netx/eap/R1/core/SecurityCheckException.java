package com.netx.eap.R1.core;


// TODO this must be child of L10nRuntimeException
public abstract class SecurityCheckException extends RuntimeException {

	protected SecurityCheckException() {
		super();
	}

	protected SecurityCheckException(String msg) {
		super(msg);
	}

	public abstract int getHttpStatusCode();
}
