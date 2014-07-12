package com.netx.eap.R1.core;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;


// TODO cannot be public, see where this is used
public class SessionManager {

	private final Map<String,UserSession> _bySessionId;
	private final Map<Long,List<UserSession>> _byUserId;
	
	public SessionManager() {
		_bySessionId = new HashMap<String,UserSession>();
		_byUserId = new HashMap<Long,List<UserSession>>();
	}

	public UserSession getSession(String sessionId) {
		Checker.checkEmpty(sessionId, "sessionId");
		return _bySessionId.get(sessionId);
	}
	
	public List<UserSession> getSessionsFor(long userId) {
		Checker.checkMinValue(userId, 1, "userId");
		List<UserSession> list = _byUserId.get(new Long(userId));
		if(list == null || list.isEmpty()) {
			return null;
		}
		return list;
	}
	
	public void putSession(UserSession s) {
		_bySessionId.put(s.getSessionId(), s);
		List<UserSession> list = _byUserId.get(s.getUserId());
		if(list == null) {
			list = new ArrayList<UserSession>();
		}
		list.remove(s);
		list.add(s);
		_byUserId.put(s.getUserId(), list);
	}

	public void removeSession(UserSession s) {
		if(_bySessionId.remove(s.getSessionId()) == null) {
			throw new IntegrityException();
		}
		List<UserSession> list = _byUserId.get(s.getUserId());
		if(list == null) {
			throw new IntegrityException();
		}
		if(!list.remove(s)) {
			throw new IntegrityException();
		}
	}
}
