package com.netx.bl.R1.core;
import com.netx.bl.R1.spi.DatabaseDriver;


public class FieldBinary extends Field {

	private final long _length;

	public FieldBinary(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, long length) {
		super(Field.TYPE.BINARY, owner, name, columnName, defaultValue, mandatory, readOnly);
		_length = length;
	}

	public long getLength() {
		return _length;
	}

	public Comparable<?> toObject(String value, DatabaseDriver driver) throws SizeExceededException {
		if(value.length() > getLength()) {
			throw new SizeExceededException(this, getLength(), value);
		}
		return new Blob(value.getBytes());
	}

	protected void checkType(Object value) {
		value = (Blob)value;
	}
}
