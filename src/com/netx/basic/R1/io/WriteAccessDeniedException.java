package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.ContentID;


public class WriteAccessDeniedException extends AccessDeniedException {

	WriteAccessDeniedException(ContentID id, String path, String objectType) {
		super(id, path, objectType);
	}
}
