package com.netx.bl.R1.core;

public class SizeExceededException extends ValidationException {

	// For Field.parse:
	SizeExceededException(Field f, long expectedLength, String value) {
		super(L10n.BL_MSG_VAL_SIZE_EXCEEDED, f.getName(), expectedLength, value.length(), value);
	}
}
