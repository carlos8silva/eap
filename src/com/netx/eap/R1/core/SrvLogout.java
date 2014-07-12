package com.netx.eap.R1.core;
import java.io.IOException;
import javax.servlet.ServletConfig;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.bl.Session.EndReason;


public final class SrvLogout extends EapServlet {

	public SrvLogout() {
		super(false);
	}
	
	public void init(ServletConfig config) {
		super.init(config);
	}

	public void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		UserSession s = findUserSession(request, response, true);
		if(s == null) {
			response.sendRedirect(Constants.URL_LOGIN);
			return;
		}
		Connection c = request.getConnection();
		User user = getEapContext().endSession(request, response, c, s, EndReason.LOGGED_OUT, null);
		c.close();
		// Show goodbye page:
		response.sendRedirectPage(Constants.URL_LOGIN+"?do=logout&screen-name="+user.getFirstName());
	}
}
