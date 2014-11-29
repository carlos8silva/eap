package com.netx.bl.R1.core;

public class MandatoryFieldException extends ValidationException {

	// For EntityInstance.setValue:
	MandatoryFieldException(Field f) {
		super(L10n.BL_MSG_VAL_NULL_MANDATORY_VALUE, f.getName());
	}
}
