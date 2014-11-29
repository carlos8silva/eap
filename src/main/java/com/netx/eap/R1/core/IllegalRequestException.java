package com.netx.eap.R1.core;
import javax.servlet.http.HttpServletResponse;


public class IllegalRequestException extends SecurityCheckException {

	public IllegalRequestException(String parameterName) {
		super(L10n.getContent(L10n.EAP_MSG_ILLEGAL_REQUEST, parameterName));
	}

	public int getHttpStatusCode() {
		return HttpServletResponse.SC_BAD_REQUEST;
	}
}
