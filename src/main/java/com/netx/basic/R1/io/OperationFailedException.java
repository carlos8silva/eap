package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.ContentID;
import com.netx.basic.R1.l10n.L10n;


// TODO identify the operation
public class OperationFailedException extends FileSystemException {

	OperationFailedException(String path) {
		super(L10n.BASIC_MSG_OPERATION_FAILED, path);
	}

	OperationFailedException(String path, ContentID id) {
		super(id, path);
	}
}
