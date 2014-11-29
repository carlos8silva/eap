package com.netx.bl.R1.core;



public final class CacheLockTimeoutException extends BLException {

	// For LockUtils:
	CacheLockTimeoutException() {
		super(L10n.BL_MSG_CACHE_LOCK_TIMEOUT);
	}
}
