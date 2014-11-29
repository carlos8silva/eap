package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class InterestsMetaData extends TimedMetaData {

	// Fields:
	public final Field recordNumber = new FieldLong(this, "recordNumber", "record_number", null, true, true, false, null, null);
	public final Field benefitType = new FieldInt(this, "benefitType", "benefit_type", null, true, true, null, null);
	public final Field interestType = new FieldChar(this, "interestType", "interest_type", null, true, true, true);

	// For Entities:
	InterestsMetaData() {
		super("Interests", "dwp_interests");
		addPrimaryKeyField(recordNumber);
		addPrimaryKeyField(benefitType);
		addPrimaryKeyField(interestType);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<Interest> getInstanceClass() {
		return Interest.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
