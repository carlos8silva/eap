package com.netx.eap.R1.core;
import javax.servlet.ServletConfig;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.BLException;


//TODO possibly cache (in memory) files that are frequently served?
//TODO process IF headers in case the browser sends them

// Note 1): there is no need to override any of the doXXX methods in this servlet.
// The default HttpServlet implementation sends SC_METHOD_NOT_ALLOWED
// (error code 405) for HTTP methods that are not overriden, except:
// - doTrace: simply returns the headers sent by the client
// - doOptions: shows what HTTP methods are supported by the servlet, based on 
//   the methods overriden by this servlet.
// Note 2): this servlet is a subclass of EapServlet (and not HttpServlet) to allow
// detection of user sessions.
public final class SrvFileDispatcher extends EapServlet {

	public SrvFileDispatcher() {
		super(false);
	}

	public void init(ServletConfig config) {
		super.init(config);
	}

	protected final void doHead(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		_service(request, response, false);
	}
	
	protected final void doGet(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		_service(request, response, true);
	}
	
	private void _service(EapRequest request, EapResponse response, boolean serveContent) throws BasicIOException, BLException {
		String path = request.getRequestPath().toLowerCase();
		if(path.equals("files") || path.equals("files/")) {
			response.sendError(EapResponse.SC_FORBIDDEN, path);
			return;
		}
		// Force a user session to be found, if it exists:
		findUserSession(request, response, true);
		File file = request.getFile(path.substring("files".length()+1));
		if(file == null) {
			response.sendError(EapResponse.SC_NOT_FOUND, path);
			return;
		}
		// Avoid serving files with 0 size:
		if(file.getSize() == 0) {
			serveContent = false;
		}
		if(serveContent) {
			response.sendFile(file);
		}
		else {
			((EapResponseImpl)response).setHeadersFor(file);
			response.setStatus(EapResponse.SC_ACCEPTED);
		}
	}
}
