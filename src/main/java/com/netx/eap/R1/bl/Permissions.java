package com.netx.eap.R1.bl;
import java.util.List;
import com.netx.bl.R1.core.*;


public class Permissions extends Entity<PermissionsMetaData,Permission> {

	// TYPE:
	public static Permissions getInstance() {
		return EAP.getPermissions();
	}

	// INSTANCE:
	Permissions() {
		super(new PermissionsMetaData());
	}
	
	public List<Permission> listAll(Connection c) throws BLException {
		return selectAll(c);
	}
}
