package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class InterestedPartiesMetaData extends TimedMetaData {

	// Fields:
	public final Field recordNumber = new FieldForeignKey(this, "recordNumber", "record_number", null, true, true, Seller.getInterests().getMetaData().recordNumber, FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field benefitType = new FieldForeignKey(this, "benefitType", "benefit_type", null, true, true, Seller.getInterests().getMetaData().benefitType, FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field interestType = new FieldForeignKey(this, "interestType", "interest_type", null, true, true, Seller.getInterests().getMetaData().interestType, FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field officeName = new FieldForeignKey(this, "officeName", "office_name", null, true, true, Seller.getOffices().getMetaData().officeName, FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);
	public final Field officeType = new FieldForeignKey(this, "officeType", "office_type", null, true, true, Seller.getOffices().getMetaData().officeType, FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);

	// For Entities:
	InterestedPartiesMetaData() {
		super("InterestedParties", "dwp_interested_parties");
		addPrimaryKeyField(recordNumber);
		addPrimaryKeyField(benefitType);
		addPrimaryKeyField(interestType);
		addPrimaryKeyField(officeName);
		addPrimaryKeyField(officeType);
		addDefaultFields();
	}

	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<InterestedParty> getInstanceClass() {
		return InterestedParty.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
