package com.netx.basic.R1.io;
import java.io.IOException;

import com.netx.basic.R1.l10n.L10n;


public class UnknownHostException extends AccessDeniedException {

	UnknownHostException(String hostName, IOException cause) {
		super(cause, L10n.BASIC_MSG_UNKNOWN_HOST, hostName);
	}
}
