package com.netx.generics.R1.util;
import com.netx.basic.R1.l10n.L10n;


public class IllegalConstructionTargetException extends ConstructionException {

	IllegalConstructionTargetException(boolean isAbstract) {
		super(isAbstract ? L10n.GENERICS_MSG_CONSTRUCTION_TARGET_ABSTRACT : L10n.GENERICS_MSG_CONSTRUCTION_TARGET_INTERFACE);
	}
}
