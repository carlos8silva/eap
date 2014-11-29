package com.netx.basic.R1.eh;
import com.netx.basic.R1.l10n.ContentID;
import com.netx.basic.R1.l10n.L10n;


public abstract class L10nException extends Exception implements Translated {

	// For more restrictive constructors:
	protected L10nException(ContentID id, Object ... parameters) {
		super(L10n.getContent(id, parameters));
	}

	// For more restrictive constructors:
	// TODO document
	protected L10nException(Throwable cause, ContentID id, Object ... parameters) {
		super(L10n.getContent(id, parameters), cause);
	}

	// For open constructors (where information is retrieved from
	// the L10n store outside of the constructor):
	protected L10nException(String message) {
		super(message);
	}

	protected L10nException(String message, Throwable cause) {
		super(message, cause);
	}
	
	protected L10nException(Throwable cause) {
		super(cause);
	}
}
