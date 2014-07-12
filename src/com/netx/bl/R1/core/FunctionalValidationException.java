package com.netx.bl.R1.core;
import com.netx.basic.R1.l10n.ContentID;


public class FunctionalValidationException extends ValidationException {

	public FunctionalValidationException(ContentID id, Object ... parameters) {
		super(id, parameters);
	}
}
