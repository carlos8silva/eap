package com.netx.config.R1;
import com.netx.basic.R1.eh.L10nException;
import com.netx.basic.R1.l10n.ContentID;


public class TypeLoadException extends L10nException {

	public TypeLoadException(ContentID id, Object ... parameters) {
		super(id, parameters);
	}

	public TypeLoadException(Exception cause) {
		super(cause);
	}
}
