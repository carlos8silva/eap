package com.netx.bl.R1.spi;
import java.sql.SQLException;


public final class DatabaseNotFoundException extends DatabaseException {

	DatabaseNotFoundException(SQLException sqle) {
		super(sqle, null, L10n.BL_MSG_DB_NOT_FOUND);
	}
}
