package com.netx.generics.R1.util;
import com.netx.basic.R1.eh.L10nException;
import com.netx.basic.R1.l10n.ContentID;


public abstract class ConstructionException extends L10nException {

	protected ConstructionException(ContentID id) {
		super(id);
	}

	// For ConstructorInvocationException:
	protected ConstructionException(String message, Throwable cause) {
		super(message, cause);
	}
}
