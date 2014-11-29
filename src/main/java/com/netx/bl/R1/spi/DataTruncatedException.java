package com.netx.bl.R1.spi;
import java.sql.SQLException;


public final class DataTruncatedException extends DatabaseException {

	DataTruncatedException(SQLException cause, String query) {
		super(cause, query, L10n.BL_MSG_DATA_TRUNCATED);
	}
}
