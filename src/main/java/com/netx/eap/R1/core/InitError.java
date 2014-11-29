package com.netx.eap.R1.core;
import javax.servlet.http.HttpServlet;


public class InitError {

	public final String servletName;
	public final Throwable t;
	public final String message;

	public InitError(HttpServlet servlet, String message, Throwable t) {
		servletName = servlet.getServletName();
		this.message = message;
		this.t = t;
	}
}
