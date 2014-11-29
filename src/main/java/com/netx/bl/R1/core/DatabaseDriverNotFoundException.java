package com.netx.bl.R1.core;
import com.netx.generics.R1.util.Version;
import com.netx.basic.R1.eh.L10nRuntimeException;


public class DatabaseDriverNotFoundException extends L10nRuntimeException {

	// When the version number is not known:
	DatabaseDriverNotFoundException(String databaseDriverName) {
		super(L10n.BL_MSG_DB_DRIVER_NOT_FOUND_01, databaseDriverName);
	}

	// When the version number is known:
	DatabaseDriverNotFoundException(String databaseDriverName, Version v) {
		super(L10n.BL_MSG_DB_DRIVER_NOT_FOUND_02, databaseDriverName, v);
	}
}
