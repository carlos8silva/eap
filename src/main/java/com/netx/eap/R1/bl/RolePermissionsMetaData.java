package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class RolePermissionsMetaData extends TimedMetaData {

	// Fields:
	public final Field roleId = new FieldForeignKey(this, "roleId", "role_id", null, true, true, EAP.getRoles().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field permissionId = new FieldForeignKey(this, "permissionId", "permission_id", null, true, true, EAP.getPermissions().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);

	// For Entities:
	RolePermissionsMetaData() {
		super("RolePermissions", "eap_role_permissions");
		addPrimaryKeyField(roleId);
		addPrimaryKeyField(permissionId);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<RolePermission> getInstanceClass() {
		return RolePermission.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
