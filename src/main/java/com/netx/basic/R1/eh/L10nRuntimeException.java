package com.netx.basic.R1.eh;
import com.netx.basic.R1.l10n.ContentID;
import com.netx.basic.R1.l10n.L10n;


public abstract class L10nRuntimeException extends RuntimeException implements Translated {

	// For more restrictive constructors:
	protected L10nRuntimeException(ContentID id, Object ... parameters) {
		super(L10n.getContent(id, parameters));
	}

	// For more restrictive constructors:
	// TODO document
	protected L10nRuntimeException(Exception cause, ContentID id, Object ... parameters) {
		super(L10n.getContent(id, parameters), cause);
	}

	// For open constructors (where information is retrieved from
	// the L10n store outside of the constructor):
	protected L10nRuntimeException(String message) {
		super(message);
	}

	protected L10nRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	protected L10nRuntimeException(Throwable cause) {
		super(cause);
	}
}
