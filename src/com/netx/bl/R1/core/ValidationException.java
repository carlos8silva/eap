package com.netx.bl.R1.core;
import com.netx.basic.R1.eh.L10nRuntimeException;
import com.netx.basic.R1.l10n.ContentID;


public abstract class ValidationException extends L10nRuntimeException {

	protected ValidationException(ContentID id, Object ... parameters) {
		super(id, parameters);
	}

	// For WrongFormatException:
	ValidationException(String message) {
		super(message);
	}
}
