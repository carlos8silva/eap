package com.netx.bl.R1.spi;
import java.sql.SQLException;


public class MalformedSQLException extends DatabaseException {

	MalformedSQLException(SQLException sqle, String query) {
		super(sqle, query, L10n.BL_MSG_MALFORMED_QUERY);
	}
}
