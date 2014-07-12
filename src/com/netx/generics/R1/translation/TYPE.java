package com.netx.generics.R1.translation;
import com.netx.basic.R1.eh.Checker;

// TODO make enum
public class TYPE {

	// TYPE:
	public static final TYPE KEYWORD = new TYPE("KEYWORD");
	public static final TYPE IDENTIFIER = new TYPE("IDENTIFIER");
	public static final TYPE SEPARATOR = new TYPE("SEPARATOR");
	public static final TYPE OPERATOR = new TYPE("OPERATOR");
	public static final TYPE STRING_CONSTANT = new TYPE("STRING_CONSTANT");
	public static final TYPE CHAR_CONSTANT = new TYPE("CHAR_CONSTANT");
	public static final TYPE INTEGER_CONSTANT = new TYPE("INTEGER_CONSTANT");
	public static final TYPE FLOAT_CONSTANT = new TYPE("FLOAT_CONSTANT");

	// INSTANCE:
	private final String _name;
	
	protected TYPE(String name) {
		Checker.checkEmpty(name, "name");
		_name = name;
	}
	
	public String toString() {
		return _name;
	}
}
