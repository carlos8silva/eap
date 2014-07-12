package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.ContentID;


public class FileSystemException extends BasicIOException {

	// For subclasses:
	FileSystemException(ContentID id, Object ... parameters) {
		super(null, id, parameters);
	}

	// For subclasses:
	FileSystemException(Exception cause, ContentID id, Object ... parameters) {
		super(cause, id, parameters);
	}
}
