package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class Interest extends HolderInstance<InterestsMetaData,Interests> {

	// TYPE:
	public static enum InterestType implements AllowedValue<Character> {
		CLERICAL('C'), SYSTEM('S');
		private final Character _value;
		private InterestType(Character value) { _value = value; }
		public Character getCode() { return _value; }
	};

	public static enum BenefitType implements AllowedValue<Integer> {
		UNEMPLOYMENT(134), SINGLE_PARENT(299), ASYLUM_SEEKER(876);
		private final Integer _value;
		private BenefitType(Integer value) { _value = value; }
		public Integer getCode() { return _value; }
	};
	
	// INSTANCE:
	public Interest(Long recordNumber, BenefitType benefitType, InterestType interestType) throws ValidationException {
		setPrimaryKey(getMetaData().recordNumber, recordNumber);
		setPrimaryKey(getMetaData().benefitType, benefitType.getCode());
		setPrimaryKey(getMetaData().interestType, interestType.getCode());
	}

	public Interest(Long recordNumber, Integer benefitType, Character interestType) throws ValidationException {
		setPrimaryKey(getMetaData().recordNumber, recordNumber);
		setPrimaryKey(getMetaData().benefitType, benefitType);
		setPrimaryKey(getMetaData().interestType, interestType);
	}

	public Interests getEntity() {
		return Interests.getInstance();
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

	protected InterestedParty createAssociationFor(MetaData metaData, Comparable<?> ... targetKey) throws ValidationException {
		return new InterestedParty(getRecordNumber(), getBenefitType().getCode(), getInterestType().getCode(), (String)targetKey[0], (Character)targetKey[1]);
	}

	public AssociationMap<InterestedParty> getInterestedParties(Connection c) throws BLException {
		return InterestedParties.getInstance().getAssociationsFor(c, this);
	}
}
