package com.netx.eap.R1.core;
import javax.servlet.http.HttpServletResponse;


class FunctionNotFoundException extends SecurityCheckException {

	// TODO L10n
	public FunctionNotFoundException() {
		super("attempted to access non-existant Application Function");
	}

	public int getHttpStatusCode() {
		return HttpServletResponse.SC_NOT_FOUND;
	}
}
