package com.netx.bl.R1.core;

public class FieldFloat extends FieldNumber {

	public FieldFloat(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, ValidationExpr vExpr, Validator validator) {
		super(Field.TYPE.FLOAT, owner, name, columnName, defaultValue, mandatory, readOnly, vExpr, validator);
	}
	
	protected void checkType(Object value) {
		value = (Float)value;
	}
}
