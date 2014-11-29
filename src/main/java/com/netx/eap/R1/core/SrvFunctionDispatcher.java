package com.netx.eap.R1.core;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletConfig;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Permission;
import com.netx.eap.R1.bl.UserEvents;


public final class SrvFunctionDispatcher extends EapServlet {

	private Map<String,Function> _functions = null;
	
	public SrvFunctionDispatcher() {
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
			// Note: do not remove Functions from the ServletContext as SrvStart also uses it
		}
		catch(Throwable t) {
			getEapContext().addInitError(this, null, t);
		}
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		Function action = _authorize(request, response);
		UserEvents.getInstance().registerAFUsed(request);
		action.doGet(request, response);
	}

	protected void doPost(EapRequest request, EapResponse response) throws IOException, BLException {
		Function action = _authorize(request, response);
		UserEvents.getInstance().registerInfoSubmitted(request);
		action.doPost(request, response);
	}
	
	private Function _authorize(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		final String path = request.getServletPath();
		final String afAlias = path.substring(1, path.lastIndexOf('.'));
		Function af = _functions.get(afAlias);
		if(af == null) {
			throw new FunctionNotFoundException();
		}
		request.setAttribute(Constants.RATTR_FUNCTION_ALIAS, af.getAlias());
		// Check if AF requires permission:
		if(af.getPermissionId() == null) {
			return af;
		}
		// It does require permission; check if user is authorized to use AF:
		UserSession s = request.getUserSession();
		Permission permission = s.getRolePermissions().get(af.getPermissionId());
		if(permission != null) {
			return af;
		}
		permission = s.getUserPermissions().get(af.getPermissionId());
		if(permission != null) {
			return af;
		}
		// No permission to use this AF:
		// TODO L10n
		throw new NotAuthorizedException();
	}
}
