package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.L10n;


public class ReadAccessDeniedException extends AccessDeniedException {

	ReadAccessDeniedException(String path, String objectType) {
		super(L10n.BASIC_MSG_ALREADY_EXISTS, path, objectType);
	}
}
