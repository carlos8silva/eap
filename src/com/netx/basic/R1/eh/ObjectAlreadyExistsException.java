package com.netx.basic.R1.eh;
import com.netx.basic.R1.l10n.ContentID;
import com.netx.basic.R1.l10n.L10n;


public class ObjectAlreadyExistsException extends L10nRuntimeException {

	// For subclasses:
	// TODO document protected
	protected ObjectAlreadyExistsException(ContentID id, Object ... parameters) {
		super(id, parameters);
	}

	
	// Used when the object type IS on the i18n store.
	// TODO document
	public ObjectAlreadyExistsException(String detail, ContentID objectType) {
		super(L10n.BASIC_MSG_ALREADY_EXISTS, L10n.getContent(objectType), detail);
	}

	// Used when the object type is NOT on the i18n store.
	// TODO document
	public ObjectAlreadyExistsException(String objectType, String detail) {
		super(L10n.BASIC_MSG_ALREADY_EXISTS, objectType, detail);
	}
}
