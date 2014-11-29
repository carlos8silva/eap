package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class RolesMetaData extends TimedMetaData {

	// Fields:
	public final Field roleId = new FieldLong(this, "roleId", "role_id", null, true, true, true, null, null);
	public final Field name = new FieldText(this, "name", "name", null, true, false, 0, 50, true, null, new Validators.ReadableText());
	public final Field description = new FieldText(this, "description", "description", null, true, false, 0, 300, true, null, null);
	public final Field systemRole = new FieldBoolean(this, "systemRole", "system_role", new Boolean(false), true, true);
	public final Field baseUi = new FieldForeignKey(this, "baseUI", "base_ui", "eap", true, false, EAP.getUserInterfaces().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);

	public RolesMetaData() {
		super("Roles", "eap_roles");
		addPrimaryKeyField(roleId);
		addField(name);
		addField(description);
		addField(systemRole);
		addField(baseUi);
		addDefaultFields();
		addUnique(name);
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<Role> getInstanceClass() {
		return Role.class;
	}

	public Field getAutonumberKeyField() {
		return roleId;
	}
}
