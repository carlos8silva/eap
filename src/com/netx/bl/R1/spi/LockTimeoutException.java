package com.netx.bl.R1.spi;
import java.sql.SQLException;


public final class LockTimeoutException extends DatabaseException {

	LockTimeoutException(SQLException sqle) {
		super(sqle, null, L10n.BL_MSG_LOCK_TIMEOUT);
	}
}
