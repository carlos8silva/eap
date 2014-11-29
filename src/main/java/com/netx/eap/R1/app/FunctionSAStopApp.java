package com.netx.eap.R1.app;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.Template;
//import com.netx.eap.R1.core.Values;

public class FunctionSAStopApp extends Function {
	protected void doGet(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		Template page = request.getTemplate("templates/sa-stop-app.html");
		//Values v = page.getValues();
		response.setDisableCache();
		page.render(MimeTypes.TEXT_HTML, response);
	}
}
