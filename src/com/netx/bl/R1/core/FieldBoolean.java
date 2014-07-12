package com.netx.bl.R1.core;
import com.netx.bl.R1.spi.DatabaseDriver;


public class FieldBoolean extends Field {

	// TODO change defaultValue's type to be Boolean? (and cascade to other field types?)
	public FieldBoolean(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly) {
		super(Field.TYPE.BOOLEAN, owner, name, columnName, defaultValue, mandatory, readOnly);
	}
	
	public Comparable<?> toObject(String value, DatabaseDriver driver) throws WrongFormatException {
		if(value.length() == 1) {
			char c = value.charAt(0);
			if(c == 'T' || c == 't') {
				return new Boolean(true);
			}
			if(c == 'F' || c == 'f') {
				return new Boolean(false);
			}
		}
		// If we are here, the format is wrong:
		throw new WrongFormatException(this, value);
	}

	protected void checkType(Object value) {
		value = (Boolean)value;
	}
}
