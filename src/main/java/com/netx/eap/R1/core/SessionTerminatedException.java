package com.netx.eap.R1.core;
import javax.servlet.http.HttpServletResponse;
import com.netx.eap.R1.bl.Session.EndReason;


class SessionTerminatedException extends SecurityCheckException {

	private final EndReason _endReason;
	private final String _saMessage;
	
	public SessionTerminatedException(EndReason endReason, String saMessage) {
		super(null);
		_endReason = endReason;
		_saMessage = saMessage;
	}
	
	public int getHttpStatusCode() {
		return HttpServletResponse.SC_OK;
	}
	
	public EndReason getEndReason() {
		return _endReason;
	}

	public String getSAMessage() {
		return _saMessage;
	}
}
