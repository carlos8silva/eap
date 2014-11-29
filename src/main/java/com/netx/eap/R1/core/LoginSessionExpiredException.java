package com.netx.eap.R1.core;
import javax.servlet.http.HttpServletResponse;


class LoginSessionExpiredException extends SecurityCheckException {

	public LoginSessionExpiredException(String message) {
		super(message);
	}
	
	public int getHttpStatusCode() {
		return HttpServletResponse.SC_OK;
	}
}
