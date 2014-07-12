package com.netx.bl.R1.core;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.netx.bl.R1.spi.DatabaseDriver;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.DateFormatException;
import com.netx.generics.R1.util.Expr;


public class FieldDate extends Field {

	// TYPE:
	private final static DateFormat _FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	// INSTANCE:
	public FieldDate(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly) {
		super(Field.TYPE.DATE, owner, name, columnName, defaultValue, mandatory, readOnly);
	}

	public Date toObject(String value, DatabaseDriver driver) throws WrongFormatException {
		try {
			// TODO decide whether we just use the system's default date format
			// obj, or if we can somehow specify a parser like Tools.parseDate.
			// If not, any date formatting must happen at the application level
			// to prevent this method from being called.
			DateFormat df = null;
			if(driver != null) {
				// If the driver is provided, we are creating the EI by reading from the database,
				// and as such we need to use the driver's date format to parse the string value.
				df = driver.getDateFormat();
			}
			else {
				// If the driver is not provided, the EI's field is being set by the application,
				// and we need to parse the string value according to BL's date format rules.
				df = _FORMAT;
			}
			Object o = Expr.evaluate(value.toString());
			if(o instanceof Date) {
				return (Date)o;
			}
			else {
				return new Date(o.toString(), df);
			}
		}
		catch(DateFormatException dfe) {
			throw new WrongFormatException(this, value);
		}
	}

	protected void checkType(Object value) {
		value = (Date)value;
	}
}
