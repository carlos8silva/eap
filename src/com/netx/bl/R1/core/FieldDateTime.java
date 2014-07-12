package com.netx.bl.R1.core;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.netx.bl.R1.spi.DatabaseDriver;
import com.netx.generics.R1.time.DateFormatException;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Expr;


public class FieldDateTime extends Field {

	// TYPE:
	private final static DateFormat _FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// INSTANCE:
	public FieldDateTime(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly) {
		super(Field.TYPE.DATETIME, owner, name, columnName, defaultValue, mandatory, readOnly);
	}

	public Timestamp toObject(String value, DatabaseDriver driver) throws WrongFormatException {
		try {
			DateFormat df = null;
			if(driver != null) {
				// If the driver is provided, we are creating the EI by reading from the database,
				// and as such we need to use the driver's date format to parse the string value.
				df = driver.getDateTimeFormat();
			}
			else {
				// If the driver is not provided, the EI's field is being set by the application,
				// and we need to parse the string value according to BL's date format rules.
				df = _FORMAT;
			}
			Object o = Expr.evaluate(value.toString());
			if(o instanceof Timestamp) {
				return (Timestamp)o;
			}
			else {
				return new Timestamp(o.toString(), df);
			}
		}
		catch(DateFormatException dfe) {
			throw new WrongFormatException(this, value);
		}
	}

	protected void checkType(Object value) {
		value = (Timestamp)value;
	}
}
