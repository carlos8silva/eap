package com.netx.bl.R1.core;

public class FieldInt extends FieldNumber {

	public FieldInt(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, ValidationExpr vExpr, Validator validator) {
		super(Field.TYPE.INT, owner, name, columnName, defaultValue, mandatory, readOnly, vExpr, validator);
	}

	protected void checkType(Object value) {
		value = (Integer)value;
	}
}
