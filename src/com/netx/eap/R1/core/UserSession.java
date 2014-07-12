package com.netx.eap.R1.core;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.BLException;
import com.netx.bl.R1.core.Connection;
import com.netx.eap.R1.bl.Session;
import com.netx.eap.R1.bl.Users;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.bl.Permission;
import com.netx.eap.R1.bl.RolePermission;
import com.netx.eap.R1.bl.UserPermission;
import com.netx.eap.R1.bl.UserRole;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.collections.IMap;
import com.netx.generics.R1.collections.ISet;


public class UserSession {

	// TYPE:
	// TODO move to Sessions
	public static String generateId() {
		return UUID.randomUUID().toString().substring(0, 12);
	}

	// INSTANCE:
	private final String _sessionId;
	private final Long _userId;
	private boolean _valid;
	private Timestamp _lastAccessed;
	private final Map<String,Object> _attrs;
	private final IMap<String,Permission> _rolePermissions;
	private final IMap<String,Permission> _userPermissions;

	// For EapServlet:
	UserSession(Connection c, Session s) throws BLException {
		_sessionId = s.getSessionId();
		_userId = s.getUserId();
		_valid = true;
		_lastAccessed = new Timestamp();
		_attrs = new HashMap<String,Object>();
		// Cache the user's permissions:
		Map<String,Permission> rolePermissions = new HashMap<String,Permission>();
		User user = Users.getInstance().get(c, s.getUserId());
		// Role permissions:
		for(UserRole userRole : user.getUserRoles(c)) {
			for(RolePermission permission : userRole.getRole(c).getRolePermissions(c)) {
				rolePermissions.put(permission.getPermissionId(), permission.getPermission(c));
			}
		}
		_rolePermissions = new IMap<String,Permission>(rolePermissions);
		// User permissions:
		Map<String,Permission> userPermissions = new HashMap<String,Permission>();
		for(UserPermission permission : user.getUserPermissions(c)) {
			userPermissions.put(permission.getPermissionId(), permission.getPermission(c));
		}
		_userPermissions = new IMap<String,Permission>(userPermissions);
	}

	public String getSessionId() {
		_checkState();
		return _sessionId;
	}

	public Long getUserId() {
		_checkState();
		return _userId;
	}

	public User getUser(Connection c) throws BLException {
		return Users.getInstance().get(c, _userId);
	}

	public Timestamp getLastAccessed() {
		_checkState();
		return _lastAccessed;
	}

	public void setAttribute(String name, Object value) {
		Checker.checkEmpty(name, "name");
		_checkState();
		if(value != null) {
			_attrs.put(name, value);
		}
		else {
			_attrs.remove(name);
		}
	}

	public Object getAttribute(String name) {
		Checker.checkEmpty(name, "name");
		_checkState();
		return _attrs.get(name);
	}

	public ISet<String> getAttributes() {
		_checkState();
		return new ISet<String>(_attrs.keySet());
	}

	public int hashCode() {
		return _sessionId.hashCode();
	}
	
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(!(o instanceof UserSession)) {
			return false;
		}
		return _sessionId.equals(((UserSession)o).getSessionId());
	}

	public void finalize() throws Throwable {
		_attrs.clear();
		super.finalize();
	}

	// For EapServlet:
	void resetLastAccessed() {
		_lastAccessed = new Timestamp();
	}

	// For SrvFormDispatcher:
	public IMap<String,Permission> getRolePermissions() {
		return _rolePermissions;
	}

	// For SrvFormDispatcher:
	public IMap<String,Permission> getUserPermissions() {
		return _userPermissions;
	}
	
	// TODO do we need this?
	private void _checkState() {
		if(!_valid) {
			throw new IllegalStateException();
		}
	}
}
