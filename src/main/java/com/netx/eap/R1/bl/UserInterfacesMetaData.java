package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserInterfacesMetaData extends TimedMetaData {

	// Fields:
	// TODO implement a validator for folder names
	public final Field uiId = new FieldText(this, "uiId", "ui_id", null, true, true, 0, 20, false, null, new Validators.ReadableText());
	public final Field name = new FieldText(this, "name", "name", null, true, false, 0, 50, true, null, new Validators.ReadableText());
	public final Field functionId = new FieldForeignKey(this, "functionId", "function_id", null, true, false, EAP.getFunctions().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);

	public UserInterfacesMetaData() {
		super("UserInterfaces", "eap_uinterfaces");
		addPrimaryKeyField(uiId);
		addField(name);
		addField(functionId);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<UserInterface> getInstanceClass() {
		return UserInterface.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
