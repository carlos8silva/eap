package com.netx.bl.R1.spi;
import java.sql.SQLException;


public final class DatabaseUnavailableException extends DatabaseException {

	DatabaseUnavailableException(SQLException sqle) {
		super(sqle, null, L10n.BL_MSG_DB_UNAVAILABLE);
	}
}
