package com.netx.bl.R1.core;

public class ValueTruncatedException extends ValidationException {

	// For Field.parse:
	ValueTruncatedException(String dataType, String value) {
		super(L10n.BL_MSG_VAL_VALUE_TRUNCATED, dataType, value);
	}
}
