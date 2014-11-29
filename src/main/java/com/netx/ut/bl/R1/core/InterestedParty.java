package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;
import com.netx.ut.bl.R1.core.Interest.BenefitType;
import com.netx.ut.bl.R1.core.Interest.InterestType;
import com.netx.ut.bl.R1.core.Office.OfficeType;


public class InterestedParty extends AssociationInstance<InterestedPartiesMetaData,InterestedParties,Interest> {

	// For Interest and DAFacade:
	public InterestedParty(Long recordNumber, Integer benefitType, Character interestType, String officeName, Character officeType) throws ValidationException {
		setPrimaryKey(getMetaData().recordNumber, recordNumber);
		setPrimaryKey(getMetaData().benefitType, benefitType);
		setPrimaryKey(getMetaData().interestType, interestType);
		setPrimaryKey(getMetaData().officeName, officeName);
		setPrimaryKey(getMetaData().officeType, officeType);
	}

	public InterestedParties getEntity() {
		return InterestedParties.getInstance();
	}

	public Office getAssociatedInstance(Connection c) throws BLException {
		return getOffice(c);
	}

	public Long getRecordNumber() {
		return (Long)getValue(getMetaData().recordNumber);
	}

	public BenefitType getBenefitType() {
		Integer benefitType = (Integer)getValue(getMetaData().benefitType);
		return (BenefitType)getAllowedValue(BenefitType.class, benefitType);
	}

	public InterestType getInterestType() {
		Character interestType = (Character)getValue(getMetaData().interestType);
		return (InterestType)getAllowedValue(InterestType.class, interestType);
	}

	public Interest getHolder(Connection c) throws BLException {
		return Interests.getInstance().get(c, getRecordNumber(), getBenefitType().getCode(), getInterestType().getCode());
	}

	public Interest getInterest(Connection c) throws BLException {
		return getHolder(c);
	}

	public String getOfficeName() {
		return (String)getValue(getMetaData().officeName);
	}

	public OfficeType getOfficeType() {
		Character officeType = (Character)getValue(getMetaData().officeType);
		return (OfficeType)getAllowedValue(OfficeType.class, officeType);
	}

	public Office getOffice(Connection c) throws BLException {
		String officeName = getOfficeName();
		if(officeName == null) {
			return null;
		}
		OfficeType officeType = getOfficeType();
		if(officeType == null) {
			return null;
		}
		return Offices.getInstance().get(c, officeName, officeType.getCode());
	}
}
