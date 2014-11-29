package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class PermissionsMetaData extends TimedMetaData {

	// Fields:
	public final Field permissionId = new FieldText(this, "permissionId", "permission_id", null, true, true, 2, 30, true, null, null);// TODO new Validators.TextIdentifier());
	public final Field name = new FieldText(this, "name", "name", null, true, true, 0, 50, true, null, new Validators.ReadableText());
	public final Field description = new FieldText(this, "description", "description", null, true, true, 0, 300, true, null, new Validators.ReadableText());
	
	public PermissionsMetaData() {
		super("Permissions", "eap_permissions");
		addPrimaryKeyField(permissionId);
		addField(name);
		addField(description);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<Permission> getInstanceClass() {
		return Permission.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
