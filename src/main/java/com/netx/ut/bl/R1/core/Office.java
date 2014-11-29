package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.*;


public class Office extends TimedInstance<OfficesMetaData,Offices> {

	// TYPE:
	public static enum OfficeType implements AllowedValue<Character> {
		OWNING('O'), BROADCAST('B');
		private final Character _value;
		private OfficeType(Character value) { _value = value; }
		public Character getCode() { return _value; }
	};
	
	// INSTANCE:
	public Office(String officeName, OfficeType officeType) throws ValidationException {
		setPrimaryKey(getMetaData().officeName, officeName);
		setPrimaryKey(getMetaData().officeType, officeType.getCode());
	}

	// For DAFacade:
	public Office(String officeName, Character officeType) throws ValidationException {
		setPrimaryKey(getMetaData().officeName, officeName);
		setPrimaryKey(getMetaData().officeType, officeType);
	}

	public Offices getEntity() {
		return Offices.getInstance();
	}

	public String getOfficeName() {
		return (String)getValue(getMetaData().officeName);
	}

	public OfficeType getOfficeType() {
		Character officeType = (Character)getValue(getMetaData().officeType);
		return (OfficeType)getAllowedValue(OfficeType.class, officeType);
	}
}
