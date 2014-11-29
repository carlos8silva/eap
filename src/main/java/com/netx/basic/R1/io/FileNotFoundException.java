package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.ContentID;


public class FileNotFoundException extends FileSystemException {

	public FileNotFoundException(String path, ContentID id) {
		super(id, path);
	}
}
