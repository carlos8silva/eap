package com.netx.generics.R1.util;
import com.netx.basic.R1.l10n.L10n;


public class ConstructorNotFoundException extends ConstructionException {

	ConstructorNotFoundException() {
		super(L10n.GENERICS_MSG_CONSTRUCTOR_NOT_FOUND);
	}
}
