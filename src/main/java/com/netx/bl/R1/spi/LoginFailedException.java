package com.netx.bl.R1.spi;
import java.sql.SQLException;


public final class LoginFailedException extends DatabaseException {

	LoginFailedException(SQLException cause, String query) {
		super(cause, query, L10n.BL_MSG_LOGIN_FAILED);
	}
}
