package com.netx.eap.R1.core;
import javax.servlet.http.HttpServletResponse;


public class IllegalParameterException extends SecurityCheckException {

	public IllegalParameterException(String parameterName, String value) {
		super(L10n.getContent(L10n.EAP_MSG_ILLEGAL_PARAMETER, parameterName, value));
	}
	
	public int getHttpStatusCode() {
		return HttpServletResponse.SC_BAD_REQUEST;
	}
}
