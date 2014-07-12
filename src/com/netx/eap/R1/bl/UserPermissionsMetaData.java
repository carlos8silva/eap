package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserPermissionsMetaData extends TimedMetaData {

	// Fields:
	public final Field userId = new FieldForeignKey(this, "userId", "user_id", null, true, true, EAP.getUsers().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field permissionId = new FieldForeignKey(this, "permissionId", "permission_id", null, true, true, EAP.getPermissions().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);

	// For Entities:
	UserPermissionsMetaData() {
		super("UserPermissions", "eap_user_permissions");
		addPrimaryKeyField(userId);
		addPrimaryKeyField(permissionId);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<UserPermission> getInstanceClass() {
		return UserPermission.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
