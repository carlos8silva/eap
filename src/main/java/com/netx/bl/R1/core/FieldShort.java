package com.netx.bl.R1.core;

public class FieldShort extends FieldNumber {

	public FieldShort(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, ValidationExpr vExpr, Validator validator) {
		super(Field.TYPE.SHORT, owner, name, columnName, defaultValue, mandatory, readOnly, vExpr, validator);
	}

	protected void checkType(Object value) {
		value = (Short)value;
	}
}
