package com.netx.eap.R1.core;
import javax.servlet.http.HttpServletResponse;
import com.netx.eap.R1.core.EapRequest.METHOD;


public class MethodNotAllowedException extends SecurityCheckException {

	public MethodNotAllowedException(METHOD method) {
		super("method not allowed: "+method.name());
	}

	public int getHttpStatusCode() {
		return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
	}
}
