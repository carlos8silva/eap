package com.netx.basic.R1.eh;
import com.netx.basic.R1.l10n.ContentID;
import com.netx.generics.R1.translation.ErrorList;


public abstract class ErrorListException extends L10nException {

	private final ErrorList _el;
	
	protected ErrorListException(ErrorList el, ContentID id, Object ... parameters) {
		super(id, parameters);
		Checker.checkEmpty(el, "el");
		_el = el;
	}

	public ErrorList getErrorList() {
		return _el;
	}
}
