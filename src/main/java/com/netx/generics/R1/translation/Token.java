package com.netx.generics.R1.translation;
import com.netx.basic.R1.eh.Checker;
import com.netx.generics.R1.translation.Position;


public class Token extends Position {

	private final TYPE _type;
	private final int _length;
	private final String _source;
	private String _value;
	
	public Token(TYPE type, int line, int beginIndex, int endIndex, String source) {
		super(line, beginIndex);
		Checker.checkNull(type, "type");
		Checker.checkIndex(endIndex, "line");
		Checker.checkEmpty(source, "source");
		if(endIndex <= beginIndex) {
			throw new IllegalArgumentException("invalid beginIndex ("+beginIndex+") and endIndex ("+endIndex+") values");
		}
		_type = type;
		_length = endIndex - beginIndex;
		_source = source;
		_value = null;
	}

	public TYPE getType() {
		return _type;
	}

	public int getLength() {
		return _length;
	}
	
	public String getSource() {
		return _source;
	}

	public String getValue() {
		return getValue(false);
	}

	public String getValue(boolean lowerCase) {
		if(_value == null) {
			if(_type == TYPE.CHAR_CONSTANT || _type == TYPE.STRING_CONSTANT) {
				// Take the delimiters out:
				_value = _source.substring(getIndex()+1, getIndex()+getLength()-1);
			}
			else {
				_value = _source.substring(getIndex(), getIndex()+_length);
			}
		}
		return lowerCase ? _value.toLowerCase() : _value;
	}

	public String getRawValue() {
		// No need to cache this value; it's not supposed to be used often.
		return _source.substring(getIndex(), getIndex()+_length);
	}

	public String toString() {
		return getValue();
	}
}
