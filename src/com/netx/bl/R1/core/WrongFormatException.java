package com.netx.bl.R1.core;


public class WrongFormatException extends ValidationException {

	// For FieldXxx.parse:
	WrongFormatException(Field f, Object value) {
		super(L10n.BL_MSG_VAL_WRONG_FORMAT, f.getName(), f.getType().toString().toLowerCase(), value);
	}

	// For Field.parse:
	WrongFormatException(String message) {
		super(message);
	}
}
