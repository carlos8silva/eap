package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class Permission extends TimedInstance<PermissionsMetaData,Permissions> {

	public Permission(String permissionId) throws ValidationException {
		setPrimaryKey(getMetaData().permissionId, permissionId);
	}

	public Permissions getEntity() {
		return Permissions.getInstance();
	}

	public String getPermissionId() {
		return (String)getValue(getMetaData().permissionId);
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public String getDescription() {
		return (String)getValue(getMetaData().description);
	}
}
