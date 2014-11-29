package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;



public class OfficesMetaData extends TimedMetaData {

	// Fields:
	public final Field officeName = new FieldText(this, "officeName", "office_name", null, true, true, 0, 50, true, null, null);
	public final Field officeType = new FieldChar(this, "officeType", "office_type", null, true, true, true);

	// For Entities:
	OfficesMetaData() {
		super("Offices", "dwp_offices");
		addPrimaryKeyField(officeName);
		addPrimaryKeyField(officeType);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<Office> getInstanceClass() {
		return Office.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
