package com.netx.generics.R1.time;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.ParseException;

import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.generics.R1.time.TimeValue.MEASURE;


public class Timestamp extends FormattableImpl implements Comparable<Timestamp> {

	// TYPE:
	public static Timestamp parse(String s) {
		Checker.checkEmpty(s, "s");
		String[] array = s.split("[ ]");
		if(array.length != 2) {
			throw new DateFormatException("unparseable moment: '"+s+"'");
		}
		return new Timestamp(Date.parse(array[0]), Time.parse(array[1]));
	}

	// INSTANCE:
	private final Calendar _cal;
	
	public Timestamp() {
		_cal = Calendar.getInstance();
	}

	public Timestamp(long milliseconds) {
		this();
		_cal.setTimeInMillis(milliseconds);
	}

	public Timestamp(Date d, Time t) {
		this();
		Checker.checkNull(d, "d");
		Checker.checkNull(t, "t");
		_cal.set(Calendar.YEAR, d.getYear());
		_cal.set(Calendar.MONTH, d.getMonth()-1);
		_cal.set(Calendar.DAY_OF_MONTH, d.getDay());
		_cal.set(Calendar.HOUR_OF_DAY, t.getHours());
		_cal.set(Calendar.MINUTE, t.getMinutes());
		_cal.set(Calendar.SECOND, t.getSeconds());
		_cal.set(Calendar.MILLISECOND, t.getMilliseconds());
	}

	public Timestamp(Date d) {
		this(d, new Time(0, 0, 0, 0));
	}

	public Timestamp(String s, DateFormat df) {
		this();
		Checker.checkEmpty(s, "s");
		Checker.checkNull(df, "df");
		try {
			_cal.setTimeInMillis(df.parse(s).getTime());
		}
		catch(ParseException pe) {
			throw new DateFormatException("unparseable moment: '"+s+"'");
		}
		
	}

	public Timestamp(String s) {
		this(s, DateFormat.getDateTimeInstance());
	}

	public String toString() {
		return format();
	}

 	public boolean equals(Object o) {
 		if(this == o) {
 			return true;
 		}
 		if(o == null) {
 			return false;
 		}
 		if(!(o instanceof Timestamp)) {
 			return false;
 		}
 		return compareTo((Timestamp)o)==0;
 	}

 	public int hashCode() {
 		return _cal.hashCode();
 	}

 	public int compareTo(Timestamp m) {
 		Checker.checkNull(m, "m");
 		return _cal.compareTo(m.getCalendar());
 	}

 	public Calendar getCalendar() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(_cal.getTimeInMillis());
		return c;
	}
	
	public long getTimeInMilliseconds() {
		return _cal.getTimeInMillis();
	}
	
	public Date getDate() {
		return new Date(_cal);
	}

	public Time getTime() {
		return new Time(_cal);
	}

	public String format(DateFormat df) {
		Checker.checkNull(df, "df");
 		return df.format(_cal.getTime());
 	}

 	public boolean after(Timestamp m) {
		Checker.checkNull(m, "m");
		return compareTo(m) > 0;
	}

	public boolean before(Timestamp m) {
		Checker.checkNull(m, "m");
		return compareTo(m) < 0;
	}

	public Timestamp add(Timestamp m) {
		Checker.checkNull(m, "m");
		return new Timestamp(getTimeInMilliseconds() + m.getTimeInMilliseconds());
	}

	public Timestamp add(Time time) {
		Checker.checkNull(time, "time");
		return add(new TimeValue(time));
	}

	public Timestamp add(TimeValue value) {
		Checker.checkNull(value, "value");
		return new Timestamp(getTimeInMilliseconds() + value.getAs(MEASURE.MILLISECONDS));
	}

	public Timestamp add(long milliseconds) {
		return new Timestamp(getTimeInMilliseconds() + milliseconds);
	}

	public Timestamp subtract(Timestamp m) {
		Checker.checkNull(m, "m");
		return subtract(m.getTimeInMilliseconds());
	}

	public Timestamp subtract(Time time) {
		Checker.checkNull(time, "time");
		return subtract(new TimeValue(time));
	}

	public Timestamp subtract(TimeValue value) {
		Checker.checkNull(value, "value");
		return subtract(value.getAs(MEASURE.MILLISECONDS));
	}

	public Timestamp subtract(long milliseconds) {
		return new Timestamp(getTimeInMilliseconds() - milliseconds);
	}

	public TimeValue timeElapsed(Timestamp m) {
		Checker.checkNull(m, "m");
		if(m.before(this)) {
			throw new IllegalArgumentException(m+": before this Moment: "+this);
		}
		else {
			return new TimeValue(m.getTimeInMilliseconds() - getTimeInMilliseconds(), MEASURE.MILLISECONDS);
		}
	}

	public TimeValue timeElapsed() {
		return timeElapsed(new Timestamp());
	}

	public Timestamp clone() {
		try {
			return (Timestamp)super.clone();
		}
		catch(CloneNotSupportedException cnse) {
			throw new IntegrityException(cnse);
		}
	}
	
	protected DateFormat getDefaultFormat() {
		return DateFormat.getDateTimeInstance();
	}
}
