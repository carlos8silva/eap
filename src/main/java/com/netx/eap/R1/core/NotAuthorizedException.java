package com.netx.eap.R1.core;
import javax.servlet.http.HttpServletResponse;


// TODO L10n
public class NotAuthorizedException extends SecurityCheckException {

	public NotAuthorizedException() {
		super();
	}

	public int getHttpStatusCode() {
		return HttpServletResponse.SC_FORBIDDEN;
	}
}
