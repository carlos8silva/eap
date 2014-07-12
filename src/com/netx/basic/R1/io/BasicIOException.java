package com.netx.basic.R1.io;
import java.io.IOException;
import com.netx.basic.R1.eh.Translated;
import com.netx.basic.R1.l10n.ContentID;
import com.netx.basic.R1.l10n.L10n;


public abstract class BasicIOException extends IOException implements Translated {

	// TODO use i18n
	BasicIOException(String message, Exception cause) {
		super(message, cause);
	}

	// TODO use i18n
	BasicIOException(Exception cause) {
		super(cause);
	}
	
	// For subclasses:
	BasicIOException(Exception cause, ContentID id, Object ... parameters) {
		super(L10n.getContent(id, parameters), cause);
	}
}
