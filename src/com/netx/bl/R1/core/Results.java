package com.netx.bl.R1.core;
import java.text.DateFormat;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import com.netx.generics.R1.collections.IList;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.spi.DatabaseDriver;


public class Results {

	private final DatabaseDriver _driver;
	private final IList<String> _cols;
	private final List<Row> _rows;
	private DateFormat _dateFormat = null;
	private DateFormat _timeFormat = null;
	private DateFormat _dateTimeFormat = null;
	
	// For GlobalQueries:
	Results(DatabaseDriver driver, String[] cols) throws SQLException {
		_driver = driver;
		_cols = new IList<String>(cols);
		_rows = new ArrayList<Row>();
	}
	
	// For GlobalQueries:
	void addRow(String[] values) {
		Checker.checkNull(values, "values");
		if(values.length != _cols.size()) {
			throw new IllegalArgumentException("expected array of values with length "+_cols.size()+" and got "+values.length);
		}
		_rows.add(new Row(this, values));
	}
	
	public int getColumnCount() {
		return _cols.size();
	}

	public int getRowCount() {
		return _rows.size();
	}
	
	public IList<String> getColumns() {
		return _cols;
	}

	public int getColumnIndex(String colName) {
		Checker.checkEmpty(colName, "colName");
		for(int i=0; i<_cols.size(); i++) {
			if(_cols.get(i).equalsIgnoreCase(colName)) {
				return i+1;
			}
		}
		return -1;
	}

	public List<Row> getRows() {
		return _rows;
	}

	public void setDateFormat(DateFormat df) {
		_dateFormat = df;
	}
	
	public void setTimeFormat(DateFormat df) {
		_timeFormat = df;
	}

	public void setDateTimeFormat(DateFormat df) {
		_dateTimeFormat = df;
	}

	public DateFormat getDateFormat() {
		return _dateFormat == null ? _driver.getDateFormat() : _dateFormat;
	}
	
	public DateFormat getTimeFormat() {
		return _timeFormat == null ? _driver.getTimeFormat() : _timeFormat;
	}

	public DateFormat getDateTimeFormat() {
		return _dateTimeFormat == null ? _driver.getDateTimeFormat() : _dateTimeFormat;
	}
}
