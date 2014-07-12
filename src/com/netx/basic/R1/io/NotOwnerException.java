package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.L10n;


public class NotOwnerException extends AccessDeniedException {

	NotOwnerException(String path, String objectType) {
		super(L10n.BASIC_MSG_NOT_OWNER, path, objectType);
	}
}
