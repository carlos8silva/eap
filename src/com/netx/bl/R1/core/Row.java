package com.netx.bl.R1.core;
import java.util.Iterator;
import java.util.NoSuchElementException;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.DateFormatException;
import com.netx.generics.R1.time.Time;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Strings;


public class Row {

	private final Results _r;
	private final String[] _values;
	private String _toString;

	// For Results:
	Row(Results t, String[] values) {
		_r = t;
		_values = values;
		_toString = null;
	}

	public Results getResults() {
		return _r;
	}

	public String getString(int column) {
		String v = _get(column);
		return Strings.isEmpty(v) ? null : v;
	}
	
	public String getString(String column) {
		return getString(_getColumnIndex(column));
	}

	// TODO must conform to the way that the driver translates boolean values
	public boolean getBoolean(int column) throws ValueTruncatedException {
		return getInt(column) != 0;
	}

	public boolean getBoolean(String column) throws ValueTruncatedException {
		return getBoolean(_getColumnIndex(column));
	}

	public char getChar(int column) {
		String v = _get(column);
		return Strings.isEmpty(v) ? '\0' : v.charAt(0);
	}

	public char getChar(String column) {
		return getChar(_getColumnIndex(column));
	}

	public byte getByte(int column) throws ValueTruncatedException {
		String v = _get(column);
		if(Strings.isEmpty(v)) {
			return 0;
		}
		return _checkMaxValue(v, Byte.MAX_VALUE, "byte").byteValue();
	}

	public byte getByte(String column) throws ValueTruncatedException {
		return getByte(_getColumnIndex(column));
	}

	public short getShort(int column) throws ValueTruncatedException {
		String v = _get(column);
		if(Strings.isEmpty(v)) {
			return 0;
		}
		return _checkMaxValue(v, Short.MAX_VALUE, "short").shortValue();
	}

	public short getShort(String column) throws ValueTruncatedException {
		return getShort(_getColumnIndex(column));
	}

	public int getInt(int column) throws ValueTruncatedException {
		String v = _get(column);
		if(Strings.isEmpty(v)) {
			return 0;
		}
		return _checkMaxValue(v, Integer.MAX_VALUE, "int").intValue();
	}

	public int getInt(String column) throws ValueTruncatedException {
		return getInt(_getColumnIndex(column));
	}

	public long getLong(int column) throws ValueTruncatedException {
		String v = _get(column);
		if(Strings.isEmpty(v)) {
			return 0;
		}
		return _checkMaxValue(v, Long.MAX_VALUE, "long").longValue();
	}

	public long getLong(String column) throws ValueTruncatedException {
		return getLong(_getColumnIndex(column));
	}

	public float getFloat(int column) throws ValueTruncatedException {
		String v = _get(column);
		if(Strings.isEmpty(v)) {
			return 0;
		}
		return _checkMaxValue(v, Float.MAX_VALUE, "float").floatValue();
	}

	public float getFloat(String column) throws ValueTruncatedException {
		return getFloat(_getColumnIndex(column));
	}

	public double getDouble(int column) {
		String v = _get(column);
		return Strings.isEmpty(v) ? 0.0F : new Double(v).doubleValue();
	}

	public double getDouble(String column) {
		return getDouble(_getColumnIndex(column));
	}

	public Timestamp getTimestamp(int column) {
		String v = _get(column);
		return Strings.isEmpty(v) ? null : new Timestamp(v, _r.getDateTimeFormat());
	}

	public Timestamp getTimestamp(String column) {
		return getTimestamp(_getColumnIndex(column));
	}

	public Date getDate(int column) {
		String v = _get(column);
		return Strings.isEmpty(v) ? null : new Date(v, _r.getDateFormat());
	}

	public Date getDate(String column) {
		return getDate(_getColumnIndex(column));
	}

	public Time getTime(int column) throws DateFormatException {
		String v = _get(column);
		return Strings.isEmpty(v) ? null : new Time(v, _r.getTimeFormat());
	}

	public Time getTime(String column) {
		return getTime(_getColumnIndex(column));
	}
	
	public String toString() {
		if(_toString == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Row: ");
			Iterator<String> it = _r.getColumns().iterator();
			for(int i=0; it.hasNext(); i++) {
				sb.append("[");
				sb.append(it.next());
				sb.append("=");
				sb.append(_values[i]);
				sb.append("]");
				if(it.hasNext()) {
					sb.append(" ");
				}
			}
			_toString = sb.toString();
		}
		return _toString;
	}
	
	private void _checkColumnIndex(int column) {
		if(column<1 || column>_r.getColumns().size()) {
			throw new IndexOutOfBoundsException(column+"");
		}
	}

	private int _getColumnIndex(String colname) {
		int index = _r.getColumnIndex(colname);
		if(index == -1) {
			throw new NoSuchElementException(colname);
		}
		else {
			return index;
		}
	}

	private String _get(int column) {
		_checkColumnIndex(column);
		return _values[column-1];
	}

	private Double _checkMaxValue(String v, double maxValue, String dataType) throws ValueTruncatedException {
		Double d = new Double(v);
		// Note: the behavior of this 'if' statement is also correct for Double.
		// If a String that exceeds the value of Double.MAX_VALUE is passed as
		// input, the resulting Double object corresponds to 'Infinity', which 
		// always evaluates true in comparison to Double.MAX_VALUE.
		if(d.doubleValue() > maxValue) {
			throw new ValueTruncatedException(dataType, v);
		}
		return d;
	}
}
