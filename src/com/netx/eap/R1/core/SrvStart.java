package com.netx.eap.R1.core;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletConfig;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.User;


public class SrvStart extends EapServlet {

	// TODO move this to EapContext?
	private Map<String,Function> _functions = null;

	public SrvStart() {
		super(true);
	}

	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) {
		super.init(config);
		try {
			_functions = (Map<String,Function>)getServletContext().getAttribute(Constants.SRVCTX_EAP_FUNCTIONS);
			if(_functions == null) {
				getEapContext().addInitError(this, "unable to retrieve Functions from the Servlet context. Has the initializer Servlet been called?", null);
			}
			// Note: do not remove Functions from the ServletContext as SrvFunctionDispatcher also uses it
		}
		catch(Throwable t) {
			getEapContext().addInitError(this, null, t);
		}
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		Connection c = request.getConnection();
		User user = request.getUserSession().getUser(c);
		String defaultAF = user.getPrimaryRole(c).getBaseUI(c).getFunctionId();
		_functions.get(defaultAF).doGet(request, response);
	}
}
