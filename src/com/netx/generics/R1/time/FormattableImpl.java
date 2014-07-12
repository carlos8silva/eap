package com.netx.generics.R1.time;
import java.text.DateFormat;


public abstract class FormattableImpl implements Formattable, Cloneable {

	private DateFormat _format;
	
	public DateFormat getFormat() {
		return _format;
	}
	
	public Formattable setFormat(DateFormat df) {
		_format = df;
		return this;
	}
	
	public final String format() {
		if(getFormat() == null) {
			return format(getDefaultFormat());
		}
		else {
			return format(getFormat());
		}
	}
	
	// for Moment, Date and Time:
	abstract DateFormat getDefaultFormat();
}
