package com.netx.bl.R1.core;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.spi.DatabaseDriver;


public abstract class FieldNumber extends FieldValidated {

	protected FieldNumber(TYPE type, MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, ValidationExpr vExpr, Validator validator) {
		super(type, owner, name, columnName, defaultValue, mandatory, readOnly, vExpr, validator);
	}

	public Comparable<?> toObject(String value, DatabaseDriver driver) throws WrongFormatException {
		try {
			if(getType().equals(TYPE.BYTE)) {
				return new Byte(value);
			}
			else if(getType().equals(TYPE.SHORT)) {
				return new Short(value);
			}
			else if(getType().equals(TYPE.INT)) {
				return new Integer(value);
			}
			else if(getType().equals(TYPE.LONG)) {
				return new Long(value);
			}
			else if(getType().equals(TYPE.FLOAT)) {
				return new Float(value);
			}
			else if(getType().equals(TYPE.DOUBLE)) {
				return new Double(value);
			}
			else {
				throw new IntegrityException(getType());
			}
		}
		catch(NumberFormatException nfe) {
			// TODO this may also correspond to a value truncated exception
			throw new WrongFormatException(this, value);
		}
	}
}
