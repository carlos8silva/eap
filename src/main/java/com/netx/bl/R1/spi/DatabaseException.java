package com.netx.bl.R1.spi;
import java.sql.SQLException;

import com.netx.basic.R1.l10n.ContentID;
import com.netx.bl.R1.core.BLException;


public abstract class DatabaseException extends BLException {

	private final String _query;

	DatabaseException(SQLException sqle, String query, ContentID id, Object ... parameters) {
		super(sqle, id, parameters);
		_query = query;
	}

	public SQLException getSQLCause() {
		return (SQLException)getCause();
	}
	
	public String getQuery() {
		return _query;
	}
}
