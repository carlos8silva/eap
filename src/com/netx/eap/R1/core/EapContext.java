package com.netx.eap.R1.core;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.collections.IList;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.FileSystem;
import com.netx.basic.R1.io.FileNotFoundException;
import com.netx.basic.R1.io.AccessDeniedException;
import com.netx.basic.R1.io.ReadWriteException;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.shared.RUN_MODE;
import com.netx.bl.R1.core.Repository;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Sessions;
import com.netx.eap.R1.bl.Session;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.bl.UserInterface;
import com.netx.eap.R1.bl.Session.EndReason;


// TODO initialize with ServletContext so that we dont need to keep passing it as a reference to all methods
public class EapContext {
	
	// TYPE:
	public static enum APP_STATES {RUNNING, STOPPED, NOT_INITIALIZED};

	// INSTANCE:
	private FileSystem _root;
	private Repository _rep;
	private Map<String,Template> _templates;
	private List<InitError> _initErrors;
	private APP_STATES _appStatus;
	private final SessionManager _sm;

	// For SrvInitializer:
	EapContext() {
		_root = null;
		_rep = null;
		_templates = null;
		_initErrors = null;
		_appStatus = APP_STATES.RUNNING;
		_sm = new SessionManager();
	}
	
	public void addInitError(HttpServlet servlet, String message, Throwable t) {
		Checker.checkNull(servlet, "servlet");
		String finalMsg = "initialization error";
		if(!Strings.isEmpty(message)) {
			finalMsg += (": "+message);
		}
		Config.LOGGER.error(finalMsg, t);
		_addInitError(new InitError(servlet, message, t));
	}

	public IList<InitError> getInitErrors() {
		return new IList<InitError>(_initErrors);
	}

	private void _addInitError(InitError error) {
		if(_initErrors == null) {
			_initErrors = new ArrayList<InitError>();
		}
		_initErrors.add(error);
		_appStatus = APP_STATES.NOT_INITIALIZED;
	}

	public FileSystem getApplicationRoot() {
		return _root;
	}

	// For SrvInitializer:
	void setApplicationRoot(FileSystem root) {
		_root = root;
	}

	public Repository getRepository() {
		return _rep;
	}

	// For SrvInitializer:
	void setRepository(Repository rep) {
		_rep = rep;
	}

	public APP_STATES getApplicationStatus() {
		return _appStatus;
	}

	public File getFile(EapRequest request, String path) throws FileNotFoundException, AccessDeniedException, ReadWriteException, BLException {
		Directory dFiles = getApplicationRoot().getDirectory("files");
		if(dFiles == null) {
			throw new IntegrityException("required directory 'files' was not found on the web server");
		}
		// If user signed on, search under user-defined directory:
		UserSession session = request.getUserSession();
		if(session != null) {
			Connection c = request.getConnection();
			UserInterface ui = session.getUser(c).getPrimaryRole(c).getBaseUI(c);
			File file = dFiles.getFile(ui.getUiId()+"/"+path);
			if(file != null) {
				return file;
			}
		}
		// Search under default directory:
		File file = dFiles.getFile(Config.DEFAULT_UI+"/"+path);
		if(file != null) {
			return file;
		}
		// Search under 'eap' directory:
		if(Config.DEFAULT_UI.equals("eap")) {
			return null;
		}
		return dFiles.getFile("eap/"+path);
	}

	public Template getTemplate(EapRequest request, String name) throws FileNotFoundException, AccessDeniedException, ReadWriteException, BLException {
		return getTemplate(request, name, null);
	}
	
	public Template getTemplate(EapRequest request, String path, Map<String, Template> snippets) throws FileNotFoundException, AccessDeniedException, ReadWriteException, BLException {
		Checker.checkEmpty(path, "path");
		// Try to retrieve cached template:
		if(Config.CACHE_TEMPLATES) {
			if(_templates == null) {
				_templates = new HashMap<String,Template>();
			}
			Template tplt = _templates.get(path);
			if(tplt != null) {
				return new Template(tplt);
			}
		}
		// Not in memory, load:
		File file = getFile(request, path);
		if(file == null) {
			throw new IllegalArgumentException("could not find template '"+path+"'");
		}
		Template tplt = new Template(file, snippets, Globals.getRunMode() == RUN_MODE.DEV ? false : true);
		if(Config.CACHE_TEMPLATES) {
			_templates.put(path, tplt);
			Config.LOGGER.info("cached template: "+file.getAbsolutePath());
			return new Template(tplt);
		}
		else {
			return tplt;
		}
	}

	// For SrvInitializer, ActionStopApplication, ActionStartApplication:
	void setApplicationStatus(APP_STATES status) {
		_appStatus = status;
	}

	// For SrvLogin, SrvActionDispatcher:
	SessionManager getSessionManager() {
		return _sm;
	}

	// For EapServlet:
	UserSession loadSession(Connection c, Session s) throws BLException {
		// Active session found:
		Config.LOGGER.warn("session '"+s.getSessionId()+"' was not in the SessionManager. Application may have been reloaded");
		// Create new session (this code automatically loads the user's permissions to the session
		// so that they are cached and any exterior changes do not affect the current session as per FDD)
		UserSession userSession = new UserSession(c, s);
		// Cache Session object:
		_sm.putSession(userSession);
		return userSession;
	}

	// For FormUMEditUser:
	public List<UserSession> getSessionsFor(Connection c, Long userId) throws BLException {
		List<UserSession> usList = _sm.getSessionsFor(userId);
		if(usList != null) {
			// Need to return a copy of the list to avoid concurrent modifications:
			List<UserSession> tmp = new ArrayList<UserSession>();
			tmp.addAll(usList);
			return tmp;
		}
		// TODO decide whether we really want to look in the database as it is expensive
		// and this only matters for situations where the app server has been restarted
		// (that's the only situation when sessions are not in the SessionManager)

		// Not in memory, search in the database:
		List<Session> sList = Sessions.getInstance().listActiveSessionsFor(c, userId);
		if(sList.isEmpty()) {
			// There are no sessions for this user:
			return null;
		}
		// Active sessions found; reload them:
		usList = new ArrayList<UserSession>();
		for(Session s : sList) {
			usList.add(loadSession(c, s));
		}
		return usList;
	}

	// For FormUMEditRole:
	public List<UserSession> getSessions(Connection c, List<Long> userIds) throws BLException {
		List<UserSession> usList = new ArrayList<UserSession>();
		for(Long userId : userIds) {
			List<UserSession> list = getSessionsFor(c, userId);
			if(list != null) {
				usList.addAll(list);
			}
		}
		return usList;
	}

	// For SrvLogout, FormMyAccount, FormUMEditUser, FormUMEditRole:
	// TODO this should not be public. Maybe we need to move the forms into eap.R1.core?
	public User endSession(EapRequest request, EapResponse response, Connection c, UserSession s, EndReason endReason, String endMessage) throws BLException {
		// Retrieve EAP_SESSIONS row with the same session-id:
		Session userSession = Sessions.getInstance().get(c, s.getSessionId());
		User user = s.getUser(c);
		// Update EAP_SESSIONS row in the database:
		userSession.setEndReason(endReason);
		userSession.setEndMessage(endMessage);
		userSession.setEndTime(new Timestamp());
		Sessions.getInstance().save(c, userSession);
		// Update memory:
		getSessionManager().removeSession(s);
		// Update cookies if self:
		if(s == request.getUserSession()) {
			// Delete session-id cookie:
			Cookie cSessionId = request.getCookie(Constants.COOKIE_SESSION_ID);
			cSessionId.setMaxAge(0);
			response.addCookie(cSessionId);
			// Set last-session-id cookie:
			Cookie cLastSessionId = new Cookie(Constants.COOKIE_LAST_SESSION_ID, s.getSessionId());
			cLastSessionId.setMaxAge(-1);
			response.addCookie(cLastSessionId);
		}
		return user;
	}
}
