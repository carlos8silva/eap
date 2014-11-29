package com.netx.config.R1;
import com.netx.basic.R1.eh.L10nRuntimeException;
import com.netx.basic.R1.l10n.ContentID;


public abstract class ValidationException extends L10nRuntimeException {

	// For subclasses:
	ValidationException(ContentID id, Object ... parameters) {
		super(id, parameters);
	}
}
