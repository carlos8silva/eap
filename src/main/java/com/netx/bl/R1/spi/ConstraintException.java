package com.netx.bl.R1.spi;
import java.sql.SQLException;
import com.netx.basic.R1.l10n.ContentID;


public abstract class ConstraintException extends DatabaseException {

	ConstraintException(SQLException cause, String query, ContentID id, Object ... parameters) {
		super(cause, query, id, parameters);
	}
}
