package com.netx.generics.R1.util;
import com.netx.basic.R1.eh.ErrorHandler;
import com.netx.basic.R1.l10n.L10n;


public class ConstructorInvocationException extends ConstructionException {

	ConstructorInvocationException(Throwable t) {
		super(L10n.getContent(L10n.GENERICS_MSG_CONSTRUCTION_INVOCATION, ErrorHandler.getMessage(t)), t);
	}
}
