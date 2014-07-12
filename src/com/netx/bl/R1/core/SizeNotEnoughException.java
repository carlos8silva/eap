package com.netx.bl.R1.core;

public class SizeNotEnoughException extends ValidationException {

	// For Field.parse:
	SizeNotEnoughException(Field f, long expectedLength, String value) {
		super(L10n.BL_MSG_VAL_SIZE_NOT_ENOUGH, f.getName(), expectedLength, value.length(), value);
	}
}
