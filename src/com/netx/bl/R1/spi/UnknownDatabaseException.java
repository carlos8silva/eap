package com.netx.bl.R1.spi;
import java.sql.SQLException;


public final class UnknownDatabaseException extends DatabaseException {

	UnknownDatabaseException(SQLException cause, String query) {
		super(cause, query, L10n.BL_MSG_UNKNOWN_DB_EXCEPTION);
	}
}
