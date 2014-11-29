package com.netx.bl.R1.core;
import com.netx.bl.R1.spi.DatabaseDriver;


public class FieldChar extends Field {

	private final boolean _ignoreCase;
	
	public FieldChar(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, boolean ignoreCase) {
		super(Field.TYPE.CHAR, owner, name, columnName, defaultValue, mandatory, readOnly);
		_ignoreCase = ignoreCase;
	}

	public boolean ignoreCase() {
		return _ignoreCase;
	}
	
	protected void checkType(Object value) {
		value = (Character)value;
	}

	public Comparable<?> toObject(String value, DatabaseDriver driver) throws SizeExceededException {
		if(value.length() > 1) {
			throw new SizeExceededException(this, 1, value);
		}
		return new Character(value.charAt(0));
	}
}
