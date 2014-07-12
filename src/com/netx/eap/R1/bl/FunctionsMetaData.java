package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class FunctionsMetaData extends TimedMetaData {

	// Fields:
	public final Field alias = new FieldText(this, "alias", "alias", null, true, true, 0, 50, true, null, new Validators.TextIdentifier());
	public final Field permissionId = new FieldForeignKey(this, "permissionId", "permission_id", null, true, true, EAP.getPermissions().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);
	public final Field title = new FieldText(this, "title", "title", null, true, true, 0, 50, false, null, new Validators.ReadableText());
	public final Field className = new FieldText(this, "className", "class_name", null, true, true, 0, 50, false, null, new Validators.CodeIdentifier());
	public final Field packageName = new FieldText(this, "packageName", "package", null, true, true, 0, 50, false, null, new Validators.CodeIdentifier());

	public FunctionsMetaData() {
		super("Functions", "eap_functions");
		addPrimaryKeyField(alias);
		addField(permissionId);
		addField(title);
		addField(className);
		addField(packageName);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<FunctionInstance> getInstanceClass() {
		return FunctionInstance.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
