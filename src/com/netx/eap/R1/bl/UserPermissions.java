package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserPermissions extends Association<UserPermissionsMetaData,UserPermission> {

	// TYPE:
	public static UserPermissions getInstance() {
		return EAP.getUserPermissions();
	}
	
	// INSTANCE:
	UserPermissions() {
		super(new UserPermissionsMetaData(), EAP.getUsers(), EAP.getPermissions());
	}
	
	// For Users:
	protected int save(Connection c, AssociationMap<UserPermission> permissions) throws BLException {
		return insertOrUpdate(c, permissions);
	}
}
