package com.netx.eap.R1.bl;
import java.util.List;

import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;


public class Sessions extends Entity<SessionsMetaData,Session> {

	// TYPE:
	public static Sessions getInstance() {
		return EAP.getSessions();
	}

	// INSTANCE:
	private Select _qSelectActiveUserSessions;

	Sessions() {
		super(new SessionsMetaData());
	}
	
	protected void onLoad() {
		_qSelectActiveUserSessions = createSelect("select-active-user-session", "SELECT * FROM eap_sessions WHERE user_id = ? AND end_reason IS NULL ORDER BY start_time DESC");
	}

	public void create(Connection c, Session userSession) throws BLException, ValidationException {
		insert(c, userSession);
	}
	
	public void save(Connection c, Session userSession) throws BLException, ValidationException {
		updateInstance(c, userSession);
	}

	public List<Session> listActiveSessionsFor(Connection c , Long userId) throws BLException {
		Checker.checkNull(userId, "userId");
		return selectList(c, _qSelectActiveUserSessions, userId);
	}
}
