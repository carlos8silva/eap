package com.netx.eap.R1.core;
import javax.servlet.http.HttpServletResponse;


class AuthenticationOngoingException extends SecurityCheckException {

	public AuthenticationOngoingException() {
	}

	public int getHttpStatusCode() {
		return HttpServletResponse.SC_OK;
	}
}
