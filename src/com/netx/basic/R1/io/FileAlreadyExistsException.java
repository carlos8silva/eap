package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.ContentID;
import com.netx.basic.R1.l10n.L10n;


public class FileAlreadyExistsException extends FileSystemException {

	public FileAlreadyExistsException(ContentID objectType, String path) {
		super(L10n.BASIC_MSG_ALREADY_EXISTS, L10n.getContent(objectType), path);
	}
}
