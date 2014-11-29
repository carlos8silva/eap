package com.netx.eap.R1.core;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.time.TimeValue;
import com.netx.generics.R1.time.TimeValue.MEASURE;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Sessions;
import com.netx.eap.R1.bl.Users;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.bl.Session;
import com.netx.eap.R1.bl.UserEvents;
import com.netx.eap.R1.bl.User.LockedReason;
import com.netx.eap.R1.core.EapContext.APP_STATES;


public final class SrvLogin extends EapServlet {

	// TYPE:
	private static final String _PAGE_NAME = "Login page";

	// INSTANCE:
	public SrvLogin() {
		super(false);
	}

	public void init(ServletConfig config) {
		super.init(config);
	}

	public void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		request.setAttribute(Constants.RATTR_PAGE_NAME, _PAGE_NAME);
		UserEvents.getInstance().registerPageDisplayed(request);
		UserSession s = findUserSession(request, response, true);
		if(s == null) {
			// Get username:
			Cookie cClientId = request.getCookie(Constants.COOKIE_USERNAME);
			String username = cClientId == null ? null : cClientId.getValue();
			// Get message:
			final String doParam = request.getParameter("do");
			String message = null;
			String style = null;
			if(doParam == null) {
				if(getEapContext().getApplicationStatus() == APP_STATES.RUNNING) {
					message = "Please enter your username and password to continue:";
					style = Constants.UI_FONT_NORMAL;
				}
				else if(getEapContext().getApplicationStatus() == APP_STATES.STOPPED) {
					// TODO show time until it becomes back up
					message = "The application is currently stopped. Please wait until the application is brought back up before logging in.";
					style = Constants.UI_FONT_ERROR;
				}
				else {
					throw new IntegrityException(getEapContext().getApplicationStatus());
				}
			}
			else if(doParam.equals("logout")) {
				final String screenName = request.getParameter("screen-name", true);
				message = "See you next time, "+screenName+"! Thanks for using "+Config.APP_NAME+".";
				style = Constants.UI_FONT_NORMAL;
			}
			else if(doParam.equals("failed")) {
				message = "The username or password that you have entered does not match our records. Please try again:";
				style = Constants.UI_FONT_ERROR;
			}
			else if(doParam.equals("expired")) {
				message = "Your previous authentication session has expired. Please log in again:";
				style = Constants.UI_FONT_ERROR;
			}
			else if(doParam.equals("locked")) {
				Connection c = request.getConnection();
				User user = Users.getInstance().getUserByUsername(c, username);
				c.close();
				if(user.getLockedReason() == LockedReason.FAILED_LOGINS) {
					message = "For security reasons, your account has been locked (the limit of failed login attempts has been reached). Please contact the support team on "+
					Config.SUPPORT_PHONE+" or <a href='mailto:"+Config.SUPPORT_EMAIL+"'>"+Config.SUPPORT_EMAIL+"</a> to re-set your password and unlock your account.";
				}
				else if(user.getLockedReason() == LockedReason.USER_MANAGER) {
					message = "Your account has been locked by a System Administrator. Please contact the support team on " +
					Config.SUPPORT_PHONE+" or <a href='mailto:"+Config.SUPPORT_EMAIL+"'>"+Config.SUPPORT_EMAIL+"</a> to unlock your account.";
				}
				else {
					throw new IntegrityException(user.getLockedReason());
				}
				// TODO show error page instead of the login page?
				style = Constants.UI_FONT_ERROR;
			}
			else {
				throw new IllegalParameterException("do", doParam);
			}
			// Show login page:
			UI.showLoginPage(request, response, message, username, "login", style);
			return;
		}
		String authStep = (String)s.getAttribute(Constants.SATTR_AUTH_STEP);
		if(authStep == null) {
			response.sendRedirect(Constants.URL_START);
		}
		else if(authStep.equals("existing-session")) {
			// TODO page with decision to either continue or cancel and be directed to the login page
			response.setDisableCache();
			response.setContentType("text/plain");
			response.getWriter().println("EXISTING SESSION");
		}
		else {
			throw new IntegrityException(authStep);
		}
	}

	public void doPost(EapRequest request, EapResponse response) throws IOException, BLException {
		// Register event:
		request.setAttribute(Constants.RATTR_PAGE_NAME, _PAGE_NAME);
		UserEvents.getInstance().registerInfoSubmitted(request);
		// Check action parameter:
		final String action = request.getParameter("action", true);
		UserSession userSession = findUserSession(request, response, true);
		if(userSession == null && !action.equals("login")) {
	        // This can happen if cookies are deleted between login screens:
			response.sendRedirect(Constants.URL_LOGIN);
			return;
		}
		// Please note that from here all redirects here must be page
		// redirects to allow manipulating cookies in the same request:
		// Actions:
		if(action.equals("login")) {
			String username = request.getParameter("username", true);
			String password = request.getParameter("password", true);
			// Add or refresh username cookie:
			Cookie cUsername = new Cookie(Constants.COOKIE_USERNAME, username);
			TimeValue tv = new TimeValue(15, MEASURE.DAYS);
			cUsername.setMaxAge((int)tv.getAs(MEASURE.SECONDS));
			response.addCookie(cUsername);
			Connection c = request.getConnection();
			try {
				// Check username and password:
				User user = Users.getInstance().getUserByUsername(c, username);
				if(user == null) {
					response.sendRedirectPage(Constants.URL_LOGIN+"?do=failed");
					return;
				}
				if(user.getStatus() == User.STATUS.LOCKED) {
					response.sendRedirectPage(Constants.URL_LOGIN+"?do=locked");
					return;
				}
				// Note: no need to check for disabled users. Since the username is updated to the user's code so
				// that the username can be reused, the getUserByUsername query does not pick up disabled users.
				if(!user.getPassword().equals(password)) {
					UserEvents.getInstance().registerLoginFailed(request, username, password);
					int failedLoginAttempts = user.getFailedLoginAttempts();
					user.setFailedLoginAttempts(failedLoginAttempts + 1);
					String url = null;
					if(failedLoginAttempts >= Config.MAX_FAILED_LOGIN_TRIES) {
						user.lock(LockedReason.FAILED_LOGINS);
						UserEvents.getInstance().registerAccountLocked(request);
						url = Constants.URL_LOGIN+"?do=locked";
					}
					else {
						url = Constants.URL_LOGIN+"?do=failed";
					}
					Users.getInstance().save(c, user, null, null);
					response.sendRedirectPage(url);
					return;
				}
				// Login OK!
				// Create session record on the database:
				Session s = new Session(UserSession.generateId());
				s.setUserId(user.getUserId());
				s.setIpAddress(request.getRemoteAddr());
				s.setBrowser("CH");
				s.setStartTime(new Timestamp());
				Sessions.getInstance().create(c, s);
				// Update num tries to 0:
				user.setFailedLoginAttempts(0);
				Users.getInstance().save(c, user, null, null);
				// Register user event:
				UserEvents.getInstance().registerLogin(request);
				// Establish HTTP session:
				userSession = new UserSession(c, s);
				getEapContext().getSessionManager().putSession(userSession);
				Cookie cSessionId = new Cookie(Constants.COOKIE_SESSION_ID, userSession.getSessionId());
				cSessionId.setMaxAge(-1);
				response.addCookie(cSessionId);
				Cookie cLastSessionId = request.getCookie(Constants.COOKIE_LAST_SESSION_ID);
				if(cLastSessionId != null) {
					cLastSessionId.setMaxAge(0);
					response.addCookie(cLastSessionId);
				}
			}
			finally {
				c.close();
			}
		}
		else {
			// TODO other actions:
			response.setContentType("text/plain");
			response.getWriter().println("login action: "+action);
			return;
		}
		// TODO checks
		
		// TODO check if this needs to be moved above
		// Clear eap-auth-step attribute:
		userSession.setAttribute(Constants.SATTR_AUTH_STEP, null);
		// Done:
		// TODO this must direct to Role's starting AF / User's choice of starting AF
		response.sendRedirectPage(Constants.URL_START);
		return;
	}
}
