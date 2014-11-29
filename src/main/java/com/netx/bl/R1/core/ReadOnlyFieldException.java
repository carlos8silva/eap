package com.netx.bl.R1.core;


public class ReadOnlyFieldException extends ValidationException {

	// For DAFacade.update:
	ReadOnlyFieldException(Field f) {
		super(L10n.BL_MSG_VAL_READ_ONLY_FIELD, f.getName());
	}
}
