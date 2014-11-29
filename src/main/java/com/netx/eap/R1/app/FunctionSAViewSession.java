package com.netx.eap.R1.app;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.MimeTypes;


public class FunctionSAViewSession extends Function {

	protected void doGet(EapRequest request, EapResponse response) throws BasicIOException {
		response.setContentType(MimeTypes.TEXT_PLAIN);
		response.getWriter().println("Session ID: "+request.getParameter("id"));
	}
}
