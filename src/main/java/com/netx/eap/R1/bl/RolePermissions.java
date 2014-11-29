package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class RolePermissions extends Association<RolePermissionsMetaData,RolePermission> {

	// TYPE:
	public static RolePermissions getInstance() {
		return EAP.getRolePermissions();
	}
	
	// INSTANCE:
	RolePermissions() {
		super(new RolePermissionsMetaData(), EAP.getRoles(), EAP.getPermissions());
	}
	
	// For Roles:
	protected int save(Connection c, AssociationMap<RolePermission> updates) throws BLException {
		return insertOrUpdate(c, updates);
	}
}
