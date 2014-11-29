package com.netx.bl.R1.core;


public class FieldByte extends FieldNumber {

	public FieldByte(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, ValidationExpr vExpr, Validator validator) {
		super(Field.TYPE.BYTE, owner, name, columnName, defaultValue, mandatory, readOnly, vExpr, validator);
	}

	protected void checkType(Object value) {
		value = (Byte)value;
	}
}
