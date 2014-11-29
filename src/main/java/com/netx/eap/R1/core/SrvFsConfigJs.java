package com.netx.eap.R1.core;
import java.io.IOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.MimeTypes;


public class SrvFsConfigJs extends EapServlet {

	public SrvFsConfigJs() {
		super(true);
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		Template page = request.getTemplate("templates/fs-config.js");
		Values v = page.getValues();
		Connection c = request.getConnection();
		v.set("help-on", request.getUserSession().getUser(c).getHelpOn().toString());
		response.setDisableCache();
		page.render(MimeTypes.TEXT_JS, response);
	}
}
