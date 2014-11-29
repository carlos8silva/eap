package com.netx.bl.R1.core;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.netx.bl.R1.spi.DatabaseDriver;
import com.netx.generics.R1.time.DateFormatException;
import com.netx.generics.R1.time.Time;
import com.netx.generics.R1.util.Expr;


public class FieldTime extends Field {

	// TYPE:
	private final static DateFormat _FORMAT = new SimpleDateFormat("HH:mm:ss");
	
	// INSTANCE:
	public FieldTime(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly) {
		super(Field.TYPE.TIME, owner, name, columnName, defaultValue, mandatory, readOnly);
	}
	
	public Time toObject(String value, DatabaseDriver driver) throws WrongFormatException {
		try {
			DateFormat df = null;
			if(driver != null) {
				// If the driver is provided, we are creating the EI by reading from the database,
				// and as such we need to use the driver's date format to parse the string value.
				df = driver.getTimeFormat();
			}
			else {
				// If the driver is not provided, the EI's field is being set by the application,
				// and we need to parse the string value according to BL's date format rules.
				df = _FORMAT;
			}
			Object o = Expr.evaluate(value.toString());
			if(o instanceof Time) {
				return (Time)o;
			}
			else {
				return new Time(o.toString(), df);
			}
		}
		catch(DateFormatException dfe) {
			throw new WrongFormatException(this, value);
		}
	}

	protected void checkType(Object value) {
		value = (Time)value;
	}
}
