package com.netx.generics.R1.translation;
import com.netx.basic.R1.eh.Checker;


public class Position {

	private final int _line;
	private final int _index;
	private final String _location;
	
	public Position(int line, int index) {
		Checker.checkIndex(line, "line");
		Checker.checkIndex(index, "index");
		_line = line;
		_index = index;
		_location = null;
	}

	public Position(String location) {
		Checker.checkEmpty(location, "location");
		_location = location;
		_line = _index = -1;
	}

	public int getLine() {
		return _line;
	}

	public int getIndex() {
		return _index;
	}
	
	public String getLocation() {
		return _location;
	}

	// Format:
	// at 'elem1'
	// line 1, index 15
	public String toString() {
		if(_location != null) {
			return "at '"+_location+"'";
		}
		else {
			String s = "line "+_line;
			if(_index != -1) {
				s = s+", index "+_index;
			}
			return s;
		}
	}
}
