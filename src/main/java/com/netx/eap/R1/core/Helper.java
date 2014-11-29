package com.netx.eap.R1.core;
import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import com.netx.basic.R1.eh.ErrorHandler;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.io.Translator;
import com.netx.bl.R1.spi.DatabaseException;
import com.netx.bl.R1.core.ValidationException;
import com.netx.bl.R1.spi.ConstraintException;
import com.netx.eap.R1.bl.UserEvents;
import com.netx.eap.R1.bl.Session.EndReason;
import com.netx.eap.R1.core.EapContext.APP_STATES;


class Helper {

	private static final String _MSG_ERROR = "[Critical] Caught unexpected error on exception handling section:";
	private static final String _MSG_ORIGINAL = "[Critical] Original application error was:";
	private static final String _MSG_NO_MSG = "[Critical] There was no error reported by the application before the critical error";
	private static final String _MSG_SECOND = "[Critical] Caught second unexpected error on error-handler servlet";
	
	public static void handleException(EapRequest request, EapResponse response, Throwable t0) {
		try {
			if(!Config.SEND_STATUS_CODES) {
				response.setStatus(HttpServletResponse.SC_OK);
			}
			// Check whether this is a security check exception, which is not a fault:
			if(t0 instanceof SecurityCheckException) {
				final SecurityCheckException sce = (SecurityCheckException)t0;
				final Cookie cSessionId = request.getCookie(Constants.COOKIE_SESSION_ID);
				if(Config.SEND_STATUS_CODES) {
					response.setStatus(sce.getHttpStatusCode());
				}
				if(sce.getClass() == SessionTerminatedException.class) {
					if(cSessionId != null) {
						cSessionId.setMaxAge(0);
						response.addCookie(cSessionId);
						Cookie cLastSessionId = new Cookie(Constants.COOKIE_LAST_SESSION_ID, cSessionId.getValue());
						cLastSessionId.setMaxAge(-1);
						response.addCookie(cLastSessionId);
					}
					Cookie cUsername = request.getCookie(Constants.COOKIE_USERNAME);
					String username = cUsername == null ? Constants.EMPTY : cUsername.getValue();
					String errorMessage = _getSessionTerminationMessage((SessionTerminatedException)t0, request.getEapContext());
					UI.showLoginPage(request, response, errorMessage, username, "login", Constants.UI_FONT_ERROR);
					return;
				}
				else if(sce.getClass() == LoginSessionExpiredException.class) {
					if(cSessionId != null) {
						cSessionId.setMaxAge(0);
						response.addCookie(cSessionId);
					}
					Cookie cUsername = request.getCookie(Constants.COOKIE_USERNAME);
					String username = cUsername == null ? Constants.EMPTY : cUsername.getValue();
					UI.showLoginPage(request, response, t0.getMessage(), username, "login", Constants.UI_FONT_ERROR);
					return;
				}
				else if(sce.getClass() == AuthenticationOngoingException.class) {
					response.sendRedirect(Constants.URL_LOGIN);
					return;
				}
				else if(sce.getClass() == FunctionNotFoundException.class) {
					UserEvents.getInstance().registerTransgression(request, sce);
					String requestURI = request.getRequestURI();
					requestURI = requestURI.substring(requestURI.indexOf("/", 1)+1);
					// Send the 404 page template:
					UI.showNotFoundError(request, response, requestURI);
					return;
				}
				else if(sce.getClass() == NotAuthorizedException.class) {
					UserEvents.getInstance().registerTransgression(request, sce);
					UI.showForbiddenError(request, response);
					return;
				}
				else if(sce.getClass() == MethodNotAllowedException.class) {
					String message = L10n.getContent(L10n.EAP_MSG_METHOD_NOT_ALLOWED, request.getMethod());
					if(request.getXml()) {
						UI.showXmlError(response, sce.getMessage(), sce.getClass().getName());
						return;
					}
					else {
						UI.showFault(request, response, message, null);
						return;
					}
				}
				else if(sce.getClass() == IllegalParameterException.class || sce.getClass() == IllegalRequestException.class) {
					UserEvents.getInstance().registerTransgression(request, sce);
					if(request.getXml()) {
						UI.showXmlError(response, sce.getMessage(), sce.getClass().getName());
						return;
					}
					else {
						// We have to show this error in full screen, as the user can only cause
						// it to happen by entering wrong parameters into the browser's URL bar
						UI.showIllegalRequestError(request, response, sce);
						return;
					}
				}
				else {
					throw new IntegrityException(sce);
				}
			}
			// This is a genuine application fault, register event and show:
			Config.LOGGER.error(t0);
			if(t0.getCause() != null) {
				Config.LOGGER.error("Caused by", t0.getCause());
			}
			// Translate IOExceptions:
			if(t0 instanceof IOException) {
				t0 = Translator.translateIOE((IOException)t0, EapResponse.STREAM_NAME);
			}
			if(Config.SEND_STATUS_CODES) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			response.setDisableCache();
			UserEvents.getInstance().registerError(request, t0);
			if(request.getXml()) {
				UI.showXmlError(response, ErrorHandler.getMessage(t0), t0.getClass().getName());
				return;
			}
			else {
				if(t0 instanceof ValidationException || t0 instanceof ConstraintException) {
					UI.showValidationError(request, response, t0.getMessage());
					return;
				}
				UI.showFault(request, response, null, t0);
				return;
			}
		}
		catch(Throwable t1) {
			handleCriticalException(response, t1, t0);
		}
	}
	
	public static void handleCriticalException(EapResponse response, Throwable t1, Throwable t0) {
		// Log the error first:
		Config.LOGGER.error(_MSG_ERROR, t1);
		if(t0 != null) {
			Config.LOGGER.error(_MSG_ORIGINAL, t0);
		}
		else {
			Config.LOGGER.error(_MSG_NO_MSG, null);
		}
		// Try showing the new error as simple text:
		// (this may not work if the response has already been committed)
		try {
			response.setContentType(MimeTypes.TEXT_PLAIN);
			PrintWriter out = response.getWriter();
			out.println(_MSG_ERROR);
			t1.printStackTrace(out);
			if(t1 instanceof DatabaseException) {
				DatabaseException de = (DatabaseException)t1;
				out.println("Query: "+de.getQuery());
				out.println();
			}
			if(t0 != null) {
				out.println(_MSG_ORIGINAL);
				t0.printStackTrace(out);
				if(t0 instanceof DatabaseException) {
					DatabaseException de = (DatabaseException)t0;
					out.println("Query: "+de.getQuery());
					out.println();
				}
			}
			else {
				out.println(_MSG_NO_MSG);
			}
			out.close();
		}
		catch(Throwable t2) {
			// Unrecoverable:
			Config.LOGGER.error(_MSG_SECOND, t2);
		}
	}

	private static String _getSessionTerminationMessage(SessionTerminatedException ste, EapContext ctx) {
		EndReason endReason = ste.getEndReason();
		if(endReason == EndReason.LOGGED_OUT) {
			return "You have logged out of your session. Please enter your username and password below to log back in:";
		}
		else if(endReason == EndReason.ABORTED) {
			return "Your session has been closed because you have logged in on another location. If you are not using the other session anymore, please enter your username and password below to log back in:";
		}
		else if(endReason == EndReason.TIMED_OUT) {
			return "Your session has timed out because you have not used the application for a long period of time. Please log in again:";
		}
		else if(endReason == EndReason.FORCED_OUT_SA) {
			return "You have been logged out of your session by a system administrator. Reason: "+ste.getSAMessage();
		}
		else if(endReason == EndReason.FORCED_OUT_SYS) {
			// TODO in which situations does this happen?
			return "You have automatically been logged out of your session by the application";
		}
		else if(endReason == EndReason.STOPPED) {
			String message = "You have been logged out of your session because the application has been stopped for maintenance. ";
			if(ctx.getApplicationStatus() == APP_STATES.STOPPED) {
				return message + "Please wait until the application is started again before logging in.";
			}
			else if(ctx.getApplicationStatus() == APP_STATES.RUNNING) {
				return message + "The application is back up; please enter your username and password below to continue:";
			}
			else {
				throw new IntegrityException(ctx.getApplicationStatus());
			}
			
		}
		else if(endReason == EndReason.PASSWORD) {
			return "You have failed to confirm your current password too many times. For your security, you have been logged out and will need to log in again in order to continue.";
		}
		else {
			throw new IntegrityException(endReason);
		}
	}
}
