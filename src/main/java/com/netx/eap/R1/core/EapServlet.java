package com.netx.eap.R1.core;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.time.TimeValue;
import com.netx.generics.R1.time.TimeValue.MEASURE;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Sessions;
import com.netx.eap.R1.bl.Session;
import com.netx.eap.R1.bl.Session.EndReason;
import com.netx.eap.R1.core.EapRequest.METHOD;


public abstract class EapServlet extends HttpServlet {

	private final boolean _requiresAuthentication;
	
	protected EapServlet(boolean requiresAuthentication) {
		_requiresAuthentication = requiresAuthentication;
	}

	protected EapServlet() {
		this(false);
	}

	public void init(ServletConfig config) {
		try {
			super.init(config);
		}
		catch(ServletException se) {
			getEapContext().addInitError(this, null, se);
		}
	}

	public EapContext getEapContext() {
		return (EapContext)getServletContext().getAttribute(Constants.SRVCTX_EAP_CTX);
	}

	protected final void doGet(HttpServletRequest request, HttpServletResponse response) {
		_service(METHOD.GET, request, response);
	}
	
	protected final void doPost(HttpServletRequest request, HttpServletResponse response) {
		_service(METHOD.POST, request, response);
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		throw new MethodNotAllowedException(METHOD.GET);
	}

	protected void doPost(EapRequest request, EapResponse response) throws IOException, BLException {
		throw new MethodNotAllowedException(METHOD.POST);
	}

	// For SrvLogin, SrvLogout and SrvFileDispatcher:
	UserSession findUserSession(EapRequest request, EapResponse response, boolean lenient) throws BLException {
		Cookie cSessionId = request.getCookie(Constants.COOKIE_SESSION_ID);
		if(cSessionId == null) {
			if(lenient) {
				// When in lenient mode, we do not throw security exceptions:
				return null;
			}
			// Try to find reason for the session to have been terminated:
			Cookie cLastSessionId = request.getCookie(Constants.COOKIE_LAST_SESSION_ID);
			if(cLastSessionId == null) {
				return null;
			}
			Connection c = request.getConnection();
			try {
				Session userSession = Sessions.getInstance().get(c, cLastSessionId.getValue());
				if(userSession == null) {
					Config.LOGGER.warn("could not find a session '"+cLastSessionId+"' in the database when it was referred to in cookie "+Constants.COOKIE_LAST_SESSION_ID);
					return null;
				}
				EndReason er = userSession.getEndReason();
				if(er == null) {
					Config.LOGGER.warn("session '"+cLastSessionId+"' was not marked as closed but it was referred to in cookie "+Constants.COOKIE_LAST_SESSION_ID);
					return null;
				}
				throw new SessionTerminatedException(er, userSession.getEndMessage());
			}
			finally {
				c.close();
			}
		}
		// OK, we have a session-id:
		UserSession s = getEapContext().getSessionManager().getSession(cSessionId.getValue());
		if(s == null) {
			// Retrieve session record from database:
			Connection c = request.getConnection();
			try {
				Session userSession = Sessions.getInstance().get(c, cSessionId.getValue());
				if(userSession == null) {
					cSessionId.setMaxAge(0);
					response.addCookie(cSessionId);
					return null;
				}
				EndReason er = userSession.getEndReason();
				if(er != null) {
					throw new SessionTerminatedException(er, userSession.getEndMessage());
				}
				// Session is active, reload it:
				s = getEapContext().loadSession(c, userSession);
			}
			finally {
				c.close();
			}
		}
		// Please note that, before authentication finishes, an EAP_SESSIONS row is not
		// created so we would never reach this point after a restart of the app server:
		if(s.getAttribute(Constants.SATTR_AUTH_STEP) != null) {
			// TODO is this "lenient" check required? Logic does not seem to make sense
			if(lenient) {
				// Check if the session has not been used for 10 minutes:
				// TODO find a more user friendly way to subtract Timestamps. If does not make sense
				// to return Timestamp on "subtract", but rather TimeValue.
				// Do instead: Timestamp.timeElapsedSince(Timestamp):TimeValue
				Timestamp now = new Timestamp();
				TimeValue tv = new TimeValue(now.subtract(s.getLastAccessed()).getTimeInMilliseconds(), MEASURE.MILLISECONDS);
				if(tv.getAs(MEASURE.MINUTES) >= 10) {
					throw new LoginSessionExpiredException("Your login session has expired. Please enter your username and password below to continue:");
				}
				s.resetLastAccessed();
				request.setAttribute(Constants.RATTR_USER_SESSION, s);
				return s;
			}
			throw new AuthenticationOngoingException();
		}
		// TODO check if timed out
		// TODO check IP address changes
		s.resetLastAccessed();
		request.setAttribute(Constants.RATTR_USER_SESSION, s);
		return s;
	}

	private void _service(METHOD method, HttpServletRequest request, HttpServletResponse response) {
		// Create request and response wrappers:
		EapRequestImpl eapReq = new EapRequestImpl(request, getServletContext());
		EapResponseImpl eapResp = new EapResponseImpl(response, getServletContext());
		try {
			// Set the correct character encoding:
			request.setCharacterEncoding(Config.CHARSET);
			// Check application not initialized:
			if(getEapContext().getApplicationStatus() == EapContext.APP_STATES.NOT_INITIALIZED) {
				if(Config.SEND_STATUS_CODES) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				UI.showNotInitializedError(eapResp);
				return;
			}
			// Check authentication:
			if(_requiresAuthentication) {
				UserSession s = findUserSession(eapReq, eapResp, false);
				if(s == null) {
					response.sendRedirect(Constants.URL_LOGIN);
					return;
				}
			}
			// Call HTTP method:
			if(method == METHOD.GET) {
				doGet(eapReq, eapResp);
			}
			else if(method == METHOD.POST) {
				doPost(eapReq, eapResp);
			}
			else {
				throw new IntegrityException(method);
			}
		}
		catch(Throwable t) {
			Helper.handleException(eapReq, eapResp, t);
		}
		finally {
			// Close database connection(s):
			// Note that this method prevents exceptions from being raised
			eapReq.closeConnections();
		}
	}
}
