package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class RolePermission extends AssociationInstance<RolePermissionsMetaData,RolePermissions,Role> {

	// For User and DAFacade:
	public RolePermission(Long roleId, String functionId) throws ValidationException {
		setPrimaryKey(getMetaData().roleId, roleId);
		setPrimaryKey(getMetaData().permissionId, functionId);
	}

	public RolePermissions getEntity() {
		return RolePermissions.getInstance();
	}

	public Permission getAssociatedInstance(Connection c) throws BLException {
		return getPermission(c);
	}

	public Long getRoleId() {
		return (Long)getValue(getMetaData().roleId);
	}

	public Role getHolder(Connection c) throws BLException {
		Long roleId = getRoleId();
		if(roleId == null) {
			return null;
		}
		return Roles.getInstance().get(c, roleId);
	}

	public Role getRole(Connection c) throws BLException {
		return getHolder(c);
	}

	public String getPermissionId() {
		return (String)getValue(getMetaData().permissionId);
	}

	public Permission getPermission(Connection c) throws BLException {
		String permissionId = getPermissionId();
		if(permissionId == null) {
			return null;
		}
		return Permissions.getInstance().get(c, getPermissionId());
	}
}
