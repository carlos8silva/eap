package com.netx.eap.R1.bl;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.core.Constants;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.UserSession;
import com.netx.eap.R1.core.SecurityCheckException;


public class UserEvents extends Entity<UserEventsMetaData,UserEvent> {

	// TYPE:
	public static UserEvents getInstance() {
		return EAP.getUserEvents();
	}

	// INSTANCE:
	UserEvents() {
		super(new UserEventsMetaData());
	}
	
	public Long registerError(EapRequest request, Throwable t) throws BLException {
		Checker.checkNull(request, "request");
		String pageName = (String)request.getAttribute(Constants.RATTR_PAGE_NAME);
		String functionId = (String)request.getAttribute(Constants.RATTR_FUNCTION_ALIAS);
		return _registerEvent(request, UserEventTypes.ERROR, pageName, functionId, false, null, t);
	}

	public Long registerWarning(EapRequest request, String message, Throwable t) throws BLException {
		Checker.checkNull(request, "request");
		String pageName = (String)request.getAttribute(Constants.RATTR_PAGE_NAME);
		String functionId = (String)request.getAttribute(Constants.RATTR_FUNCTION_ALIAS);
		Map<String,String> details = new HashMap<String,String>();
		if(!Strings.isEmpty(message)) {
			details.put("message", message);
		}
		return _registerEvent(request, UserEventTypes.WARNING, pageName, functionId, false, details, t);
	}

	public Long registerPageDisplayed(EapRequest request) throws BLException {
		Checker.checkNull(request, "request");
		String pageName = (String)request.getAttribute(Constants.RATTR_PAGE_NAME);
		return _registerEvent(request, UserEventTypes.PAGE_DISPLAYED, pageName, null, false, null, null);
	}

	public Long registerAFUsed(EapRequest request) throws BLException {
		Checker.checkNull(request, "request");
		String functionId = (String)request.getAttribute(Constants.RATTR_FUNCTION_ALIAS);
		return _registerEvent(request, UserEventTypes.FUNCTION_USED, null, functionId, true, null, null);
	}

	public Long registerInfoSubmitted(EapRequest request) throws BLException {
		Checker.checkNull(request, "request");
		String pageName = (String)request.getAttribute(Constants.RATTR_PAGE_NAME);
		String functionId = (String)request.getAttribute(Constants.RATTR_FUNCTION_ALIAS);
		return _registerEvent(request, UserEventTypes.INFORMATION_SUBMITTED, pageName, functionId, false, null, null);
	}

	public Long registerLogin(EapRequest request) throws BLException {
		Checker.checkNull(request, "request");
		String pageName = (String)request.getAttribute(Constants.RATTR_PAGE_NAME);
		return _registerEvent(request, UserEventTypes.LOGGED_IN, pageName, null, false, null, null);
	}

	public Long registerLoginFailed(EapRequest request, String username, String password) throws BLException {
		Checker.checkNull(request, "request");
		String pageName = (String)request.getAttribute(Constants.RATTR_PAGE_NAME);
		Map<String,String> details = new HashMap<String,String>();
		details.put("username", username);
		details.put("password", password);
		return _registerEvent(request, UserEventTypes.LOGIN_FAILED, pageName, null, false, details, null);
	}

	public Long registerAccountLocked(EapRequest request) throws BLException {
		Checker.checkNull(request, "request");
		String pageName = (String)request.getAttribute(Constants.RATTR_PAGE_NAME);
		return _registerEvent(request, UserEventTypes.ACCOUNT_LOCKED, pageName, null, false, null, null);
	}

	public Long registerTransgression(EapRequest request, SecurityCheckException sce) throws BLException {
		Checker.checkNull(request, "request");
		String pageName = (String)request.getAttribute(Constants.RATTR_PAGE_NAME);
		String functionId = (String)request.getAttribute(Constants.RATTR_FUNCTION_ALIAS);
		Map<String,String> details = new HashMap<String,String>();
		details.put("message", sce.getMessage());
		return _registerEvent(request, UserEventTypes.TRANSGRESSION, pageName, functionId, true, details, null);
	}

	private Long _registerEvent(EapRequest request, int eventType, String page, String functionId, boolean addURL, Map<String,String> details, Throwable t) throws BLException {
		Connection c = null;
		try {
			c = request.getConnection();
		}
		catch(IllegalStateException ise) {
			// This happens when the database fails to initialize. In this case, we do not register events
			return null;
		}
		try {
			UserEvent event = new UserEvent();
			event.setTime(new Timestamp());
			UserSession s = request.getUserSession();
			event.setSessionId(s == null ? null : s.getSessionId());
			try {
				event.setServerAddress(InetAddress.getLocalHost().getHostAddress());
			}
			catch(UnknownHostException uhe) {
				throw new IntegrityException(uhe);
			}
			event.setClientAddress(request.getRemoteAddr());
			// TODO correct
			event.setBrowser("CH");
			// TODO implement
			// event.setBrowserVersion(whatever);
			event.setTypeId(eventType);
			event.setPage(page);
			event.setFunction(functionId);
			if(addURL) {
				if(details == null) {
					details = new HashMap<String,String>();
					details.put("url", request.getCompleteRequestURL(true));
				}
			}
			if(details != null && details.size() > 0) {
				StringBuilder sb = new StringBuilder();
				Iterator<Map.Entry<String,String>> it = details.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String,String> e = it.next();
					if(!Strings.isEmpty(e.getKey()) && !Strings.isEmpty(e.getValue())) {
						sb.append(Strings.escape(e.getKey(), "|"));
						sb.append("|");
						sb.append(Strings.escape(e.getValue(), "|"));
						if(it.hasNext()) {
							sb.append("|");
						}
					}
				}
				String detailsText = sb.toString();
				event.setDetails(detailsText);
			}
			if(t != null) {
				// TODO handle this with a direct stream to the database
				StringWriter stackTrace = new StringWriter();
				t.printStackTrace(new PrintWriter(stackTrace));
				String traceText = stackTrace.toString();
				event.setStackTrace(traceText);
			}
			insert(c, event);
			return event.getEventId();
		}
		finally {
			c.close();
		}
	}
}
