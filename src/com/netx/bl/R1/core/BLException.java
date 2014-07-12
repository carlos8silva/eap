package com.netx.bl.R1.core;
import java.sql.SQLException;
import com.netx.basic.R1.eh.L10nException;
import com.netx.basic.R1.l10n.ContentID;


public abstract class BLException extends L10nException {

	// For DatabaseExceptions not caused by an SQLException, only to be used in this package:
	BLException(ContentID id, Object ... parameters) {
		super(L10n.getContent(id, parameters));
	}

	// For DatabaseException:
	protected BLException(SQLException cause, ContentID id, Object ... parameters) {
		super(L10n.getContent(id, parameters), cause);
	}
}
