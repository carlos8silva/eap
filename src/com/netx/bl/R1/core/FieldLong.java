package com.netx.bl.R1.core;

public class FieldLong extends FieldNumber {

	private final boolean _autonumber;
	
	public FieldLong(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, boolean autonumber, ValidationExpr vExpr, Validator validator) {
		super(Field.TYPE.LONG, owner, name, columnName, defaultValue, mandatory, readOnly, vExpr, validator);
		_autonumber = autonumber;
	}
	
	public boolean isAutonumber() {
		return _autonumber;
	}

	protected void checkType(Object value) {
		value = (Long)value;
	}
}
