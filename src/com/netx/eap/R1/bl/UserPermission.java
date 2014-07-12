package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserPermission extends AssociationInstance<UserPermissionsMetaData,UserPermissions,User> {

	// For User and DAFacade:
	public UserPermission(Long userId, String functionId) throws ValidationException {
		setPrimaryKey(getMetaData().userId, userId);
		setPrimaryKey(getMetaData().permissionId, functionId);
	}

	public UserPermissions getEntity() {
		return UserPermissions.getInstance();
	}

	public Permission getAssociatedInstance(Connection c) throws BLException {
		return getPermission(c);
	}

	public Long getUserId() {
		return (Long)getValue(getMetaData().userId);
	}

	public User getHolder(Connection c) throws BLException {
		Long id = getUserId();
		if(id == null) {
			return null;
		}
		return Users.getInstance().get(c, id);
	}

	public User getUser(Connection c) throws BLException {
		return getHolder(c);
	}

	public String getPermissionId() {
		return (String)getValue(getMetaData().permissionId);
	}

	public Permission getPermission(Connection c) throws BLException {
		String id = getPermissionId();
		if(id == null) {
			return null;
		}
		return Permissions.getInstance().get(c, id);
	}
}
