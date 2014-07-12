package com.netx.eap.R1.core;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.io.ExtendedReader;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.Constants;


class Segment {

	private final String _name;
	private String _varValue;
	private ExtendedReader _stream;
	private final String _text;
	private final ValueList _list;
	private final Values _ifTrue;
	private final Values _ifFalse;
	private final int _line;
	
	public Segment(String name, String text, ValueList list, Values ifTrue, Values ifFalse, int line) {
		_name = name;
		_varValue = null;
		_stream = null;
		_text = text;
		_list = list;
		_ifTrue = ifTrue;
		_ifFalse = ifFalse;
		_line = line;
	}

	public Segment(Segment another) {
		_name = another._name;
		_varValue = null;
		_text = another._text;
		_stream = another._stream;
		_list = another._list == null ? null : new ValueList(another._list);
		_ifTrue = another._ifTrue == null ? null : new Values(another._ifTrue);
		_ifFalse = another._ifFalse == null ? null : new Values(another._ifFalse);
		_line = another._line;
	}
	
	public boolean isVariable() {
		return _name != null && _list == null && _ifTrue == null;
	}

	public boolean isText() {
		return _text != null;
	}

	public boolean hasStream() {
		return _stream != null;
	}

	public boolean isList() {
		return _list != null;
	}

	public boolean isIf() {
		return _ifTrue != null;
	}
	
	public String getName() {
		return _name;
	}

	public void setValue(Object value) {
		if(isIf()) {
			Checker.checkNull(value, "value");
			if(value.toString().equals(Constants.FALSE)) {
				_varValue = Constants.FALSE;
			}
			else if(value.toString().equals(Constants.TRUE)) {
				_varValue = Constants.TRUE;
			}
			else {
				throw new IllegalArgumentException("expected boolean value, found ["+value+"]");
			}
		}
		else {
			if(value == null) {
				_varValue = Constants.EMPTY;
			}
			else if(value instanceof ExtendedReader) {
				_stream = (ExtendedReader)value;
			}
			else {
				_varValue = value.toString();
			}
		}
	}

	public ValueList getList() {
		return _list;
	}

	public Values getIf() {
		if(_varValue == null || _varValue.equals(Constants.FALSE)) {
			return _ifFalse;
		}
		else if(_varValue.equals(Constants.TRUE)) {
			return _ifTrue;
		}
		else {
			throw new IntegrityException(_varValue);
		}
	}
	
	public String getText() {
		if(isText()) {
			return _text;
		}
		if(_stream != null) {
			throw new UnsupportedOperationException("cannot call getText: segment has a character stream");
		}
		if(isVariable()) {
			if(_varValue == null) {
				throw new IllegalStateException("in line "+_line+": variable '"+_name+"' has not been set yet");
			}
			return _varValue;
		}
		throw new UnsupportedOperationException("cannot call getText on a <"+(isList()?"list":"if")+"> segment");
	}

	public ExtendedReader getStream() {
		if(hasStream()) {
			return _stream;
		}
		throw new UnsupportedOperationException("no character stream set");
	}
	
	// For Values:
	String getRawValue() {
		return _varValue;
	}
}
