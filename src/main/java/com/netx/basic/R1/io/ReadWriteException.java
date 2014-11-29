package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.ContentID;
import com.netx.basic.R1.l10n.L10n;


public abstract class ReadWriteException extends BasicIOException {

	// TODO use i18n
	ReadWriteException(String message, Exception cause) {
		super(message, cause);
	}

	// TODO use i18n
	ReadWriteException(Exception cause) {
		super(cause);
	}
	
	// For subclasses:
	ReadWriteException(Exception cause, ContentID id, Object ... parameters) {
		super(L10n.getContent(id, parameters), cause);
	}
}
