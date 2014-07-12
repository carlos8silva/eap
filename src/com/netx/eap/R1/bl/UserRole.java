package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserRole extends AssociationInstance<UserRolesMetaData,UserRoles,User> {

	// For User and DAFacade:
	public UserRole(Long userId, Long roleId) throws ValidationException {
		setPrimaryKey(getMetaData().userId, userId);
		setPrimaryKey(getMetaData().roleId, roleId);
	}

	public UserRoles getEntity() {
		return UserRoles.getInstance();
	}

	public Long getUserId() {
		return (Long)getValue(getMetaData().userId);
	}

	public User getUser(Connection c) throws BLException {
		Long userId = getUserId();
		if(userId == null) {
			return null;
		}
		return Users.getInstance().get(c, userId);
	}

	public User getHolder(Connection c) throws BLException {
		return getUser(c);
	}

	public Long getRoleId() {
		return (Long)getValue(getMetaData().roleId);
	}

	public Role getRole(Connection c) throws BLException {
		Long roleId = getRoleId();
		if(roleId == null) {
			return null;
		}
		return Roles.getInstance().get(c, getRoleId());
	}

	public Role getAssociatedInstance(Connection c) throws BLException {
		return getRole(c);
	}

	public Boolean getPrimaryRole() {
		return (Boolean)getValue(getMetaData().primaryRole);
	}

	public UserRole setPrimaryRole(Boolean primaryRole) throws ValidationException {
		safelySetValue(getMetaData().primaryRole, primaryRole);
		return this;
	}
}
