package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserRoles extends Association<UserRolesMetaData,UserRole> {

	// TYPE:
	public static UserRoles getInstance() {
		return EAP.getUserRoles();
	}
	
	// INSTANCE:
	UserRoles() {
		super(new UserRolesMetaData(), EAP.getUsers(), EAP.getRoles());
	}
	
	// For Users:
	protected int save(Connection c, AssociationMap<UserRole> roles) throws BLException {
		return insertOrUpdate(c, roles);
	}
}
