package com.netx.eap.R1.app;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.Template;
import com.netx.eap.R1.core.Values;


public class FunctionUserInterfaceRIT extends Function {

	protected void doGet(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		Template startPage = request.getTemplate("rm-tracker.html");
		Values v = startPage.getValues();
		Connection c = request.getConnection();
		v.set("screen-name", request.getUserSession().getUser(c).getFullName());
		response.setDisableCache();
		startPage.render(MimeTypes.TEXT_HTML, response);
	}
}
