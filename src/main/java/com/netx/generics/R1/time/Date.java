package com.netx.generics.R1.time;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.DATE_ORDER;


public class Date extends FormattableImpl implements Comparable<Date> {

	// TYPE:
	private static final DateFormat _DATE_BASIC = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat _DATE_YLEFT_SLASH = new SimpleDateFormat("yyyy/MM/dd");
	private static final DateFormat _DATE_YLEFT_HIFEN = new SimpleDateFormat("yyyy-MM-dd"); 
	private static final DateFormat _DATE_EU_SLASH = new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat _DATE_EU_HIFEN = new SimpleDateFormat("dd-MM-yyyy"); 
	private static final DateFormat _DATE_US_SLASH = new SimpleDateFormat("MM/dd/yyyy");
	private static final DateFormat _DATE_US_HIFEN = new SimpleDateFormat("MM-dd-yyyy"); 

	static {
		_DATE_BASIC.setLenient(false);
		_DATE_YLEFT_SLASH.setLenient(false);
		_DATE_YLEFT_HIFEN.setLenient(false);
		_DATE_EU_SLASH.setLenient(false);
		_DATE_EU_HIFEN.setLenient(false);
		_DATE_US_SLASH.setLenient(false);
		_DATE_US_HIFEN.setLenient(false);
	}

	public static boolean isLeapYear(int year) {
		Checker.checkMinValue(year, 1, "year");
		return ((year%400==0)||((year%4==0)&&(year%100!=0)));
	}
	
	public static Date parse(String s) {
		Checker.checkEmpty(s, "s");
		DATE_ORDER df = Globals.getDateOrder();
		int index = s.indexOf('/');
		if(index != -1) {
			_checkLength(s, 10);
			if(index == 4) {
				return new Date(s, _DATE_YLEFT_SLASH);
			}
			else {
				if(df == DATE_ORDER.EU) {
					return new Date(s, _DATE_EU_SLASH);
				}
				else if(df == DATE_ORDER.US) {
					return new Date(s, _DATE_US_SLASH);
				}
				else {
					throw new IntegrityException(Globals.getDateOrder());
				}
			}
		}
		index = s.indexOf('-');
		if(index != -1) {
			_checkLength(s, 10);
			if(index == 4) {
				return new Date(s, _DATE_YLEFT_HIFEN);
			}
			else {
				if(df == DATE_ORDER.EU) {
					return new Date(s, _DATE_EU_HIFEN);
				}
				else if(df == DATE_ORDER.US) {
					return new Date(s, _DATE_US_HIFEN);
				}
				else {
					throw new IntegrityException(df);
				}
			}
		}
		else {
			_checkLength(s, 8);
			return new Date(s, _DATE_BASIC);
		}
	}

	private static void _checkLength(String s, int length) {
		if(s.length() > length) {
			throw new DateFormatException("unparseable date: '"+s+"'");
		}
	}

	private static final int[] _days = {
		31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
	};

	// INSTANCE:
	private int _year;
	private int _month;
	private int _day;
	private boolean _lenient;

	public Date() {
		this(Calendar.getInstance());
	}

	public Date(int year, int month, int day) {
		_setYear(year);
		_setMonth(month);
		_setDay(day);
		_lenient = false;
	}
	
	public Date(String s, DateFormat df) {
		Checker.checkEmpty(s, "s");
		Checker.checkNull(df, "df");
		try {
			Date d = new Timestamp(df.parse(s).getTime()).getDate();
			_setYear(d.getYear());
			_setMonth(d.getMonth());
			_setDay(d.getDay());
			_lenient = false;
		}
		catch(ParseException pe) {
			throw new DateFormatException("unparseable date: '"+s+"'");
		}
	}

	public Date(String s) {
		this(s, DateFormat.getDateInstance());
	}

	// for Moment.getDate
 	Date(Calendar c) {
		_year = c.get(Calendar.YEAR);
		_month = c.get(Calendar.MONTH)+1;
		_day = c.get(Calendar.DAY_OF_MONTH);
		_lenient = false;
	}

 	public int getYear() {
 		return _year;
 	}
 	
 	public int getMonth() {
 		return _month;
 	}
 	
 	public int getDay() {
 		return _day;
 	}

 	public Date setYear(int year) {
 		if(!_lenient) {
 	 		return new Date(year, getMonth(), getDay());
 		}
 		else {
 			Calendar c = new Timestamp(this).getCalendar();
 			c.set(Calendar.YEAR, year);
 			Date d = new Date(c);
 			d.setLenient(true);
 			return d;
 		}
 	}

 	public Date setMonth(int month) {
 		if(!_lenient) {
 	 		return new Date(getYear(), month, getDay());
 		}
 		else {
 			Calendar c = new Timestamp(this).getCalendar();
 			c.set(Calendar.MONTH, month-1);
 			Date d = new Date(c);
 			d.setLenient(true);
 			return d;
 		}
 	}
 	
 	public Date setDay(int day) {
 		if(!_lenient) {
 	 		return new Date(getYear(), getMonth(), day);
 		}
 		else {
 			Calendar c = new Timestamp(this).getCalendar();
 			c.set(Calendar.DAY_OF_MONTH, day);
 			Date d = new Date(c);
 			d.setLenient(true);
 			return d;
 		}
 	}

 	public boolean getLenient() {
 		return _lenient;
 	}
 	
 	public void setLenient(boolean value) {
 		_lenient = value;
 	}

 	public int compareTo(Date d) {
 		Checker.checkNull(d, "d");
 		return _getDate().compareTo(d._getDate());
 	}
 	
 	public boolean equals(Object o) {
 		if(this == o) {
 			return true;
 		}
 		if(o == null) {
 			return false;
 		}
 		if(!(o instanceof Date)) {
 			return false;
 		}
 		return compareTo((Date)o)==0;
 	}

 	public int hashCode() {
 		int hash = 7;
 		hash = 31 * hash + _year;
 		hash = 31 * hash + _month;
 		hash = 31 * hash + _day;
 		return hash;
 	}

 	public boolean after(Date d) {
		return compareTo(d) > 0;
	}

	public boolean before(Date d) {
		return compareTo(d) < 0;
	}

	public String format(DateFormat df) {
		Checker.checkNull(df, "df");
 		return df.format(_getDate());
 	}
 	
	public String toString() {
		return format();
	}
	
	public Date clone() {
		try {
			return (Date)super.clone();
		}
		catch(CloneNotSupportedException cnse) {
			throw new IntegrityException(cnse);
		}
	}

	protected DateFormat getDefaultFormat() {
		return DateFormat.getDateInstance();
	}
	
 	private void _setYear(int year) {
		if(year < 1) {
			throw new IllegalArgumentException("illegal year: "+year);
		}
		_year = year;
	}

	private void _setMonth(int month) {
		if(month<1 || month>12 || _day>_days[month-1] || (month == 2 && !isLeapYear(_year) && _day>28)) {
			throw new IllegalArgumentException("illegal month: "+Strings.valueOf(month, 2));
		}
		_month = month;
	}

 	private void _setDay(int day) {
		if(day<1 || day>_days[_month-1] || (_month == 2 && !isLeapYear(_year) && day>28)) {
			throw new IllegalArgumentException("illegal day: "+Strings.valueOf(day, 2));
		}
		_day = day;
 	}

 	private java.util.Date _getDate() {
		return new Timestamp(this).getCalendar().getTime();
	}
}
