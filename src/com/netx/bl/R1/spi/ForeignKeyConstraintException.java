package com.netx.bl.R1.spi;
import java.sql.SQLException;


public final class ForeignKeyConstraintException extends ConstraintException {

	ForeignKeyConstraintException(SQLException cause, String query) {
		super(cause, query, L10n.BL_MSG_CONSTRAINT_FRGNK);
	}
}
