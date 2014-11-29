package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.ContentID;


public abstract class AccessDeniedException extends FileSystemException {

	// For subclasses:
	AccessDeniedException(ContentID id, Object ... parameters) {
		super(id, parameters);
	}

	// For subclasses:
	AccessDeniedException(Exception cause, ContentID id, Object ... parameters) {
		super(cause, id, parameters);
	}
}
