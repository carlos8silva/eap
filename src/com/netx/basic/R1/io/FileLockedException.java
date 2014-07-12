package com.netx.basic.R1.io;
import com.netx.basic.R1.l10n.L10n;


public class FileLockedException extends AccessDeniedException {

	FileLockedException(String path, Exception cause) {
		super(cause, L10n.BASIC_MSG_FILE_LOCKED, path);
	}
}
