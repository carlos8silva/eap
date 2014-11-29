package com.netx.eap.R1.core;
import java.io.Reader;
import java.io.StringReader;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.netx.bl.R1.core.Connection;
import com.netx.eap.R1.bl.UserEvents;
import com.netx.eap.R1.bl.UserEvent;


public final class SrvErrorHandler extends HttpServlet {

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected final void doGet(HttpServletRequest request, HttpServletResponse response) {
		final EapRequestImpl eapReq = new EapRequestImpl(request, getServletContext());
		final EapResponseImpl eapResp = new EapResponseImpl(response, getServletContext());
		try {
			final Integer statusCode = (Integer)request.getAttribute(Constants.JAVAX_STATUS_CODE);
			// Send HTTP OK if config is set not to send HTTP status codes:
			if(!Config.SEND_STATUS_CODES) {
				response.setStatus(HttpServletResponse.SC_OK);
			}
			// Direct call to error-handler:
			// TODO remove code to handle direct calls; we dont show the stack trace based on event id anymore
			if(statusCode == null) {
				final String doParameter = request.getParameter("do");
				final String sEventId = request.getParameter("event-id");
				final String redirection = eapReq.getCookie(Constants.COOKIE_SESSION_ID)==null ? Constants.URL_LOGIN : Constants.URL_START;
				if(doParameter == null && sEventId == null) {
					response.sendRedirect(redirection);
					return;
				}
				if(doParameter == null) {
					UserEvents.getInstance().registerTransgression(eapReq, new IllegalRequestException("do"));
					response.sendRedirect(redirection);
					return;
				}
				if(!doParameter.equals("view-stack-trace")) {
					UserEvents.getInstance().registerTransgression(eapReq, new IllegalParameterException("do", doParameter));
					response.sendRedirect(redirection);
					return;
				}
				if(sEventId == null) {
					UserEvents.getInstance().registerTransgression(eapReq, new IllegalRequestException("event-id"));
					response.sendRedirect(redirection);
					return;
				}
				Long eventId = null;
				try {
					eventId = new Long(sEventId);
				}
				catch(NumberFormatException nfe) {
					UserEvents.getInstance().registerTransgression(eapReq, new IllegalParameterException("event-id", sEventId));
					response.sendRedirect(redirection);
					return;
				}
				// Parameters OK, we can show the stack trace:
				// TODO handle cases where the event type is not an error, does not exist or stack trace is null
				final Connection c = eapReq.getConnection();
				UserEvent event = UserEvents.getInstance().get(c, eventId);
				Reader reader = new StringReader(event.getStackTrace());
				UI.showErrorStackTrace(eapResp, reader);
				c.close();
				return;
			}
			// 404 Page Not Found:
			if(statusCode == HttpServletResponse.SC_NOT_FOUND) {
				// Register the event:
				request.setAttribute(Constants.RATTR_PAGE_NAME, "404 Page not found");
				UserEvents.getInstance().registerPageDisplayed(eapReq);
				// Get the original request URI and remove the context path:
				String requestURI = (String)request.getAttribute(Constants.JAVAX_REQUEST_URI);
				requestURI = requestURI.substring(requestURI.indexOf("/", 1)+1);
				// Send the 404 page template:
				UI.showNotFoundError(eapReq, eapResp, requestURI);
				return;
			}
			// 403 Forbidden:
			if(statusCode == HttpServletResponse.SC_FORBIDDEN) {
				// Register the event:
				request.setAttribute(Constants.RATTR_PAGE_NAME, "403 Forbidden");
				UserEvents.getInstance().registerPageDisplayed(eapReq);
				// Send the 403 page template:
				UI.showForbiddenError(eapReq, eapResp);
				return;
			}
			// 500 Internal Error:
			// This should only happen on servlets which are not a subclass of EapServlet
			if(statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
				Throwable t = (Throwable)request.getAttribute(Constants.JAVAX_EXCEPTION);
				Config.LOGGER.error("unexpected error reached the error-handler servlet", t);
				UI.showFault(eapReq, eapResp, null, t);
				return;
			}
			// Unknows status code:
			if(statusCode != HttpServletResponse.SC_OK) {
				// TODO L10n
				final String message = "unexpected HTTP status code received: "+statusCode;
				Config.LOGGER.error(message, null);
				UI.showFault(eapReq, eapResp, message, null);
				return;
			}
		}
		catch(Throwable t) {
			Helper.handleCriticalException(eapResp, t, null);
		}
	}

	protected final void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}
}
