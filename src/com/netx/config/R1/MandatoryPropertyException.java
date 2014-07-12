package com.netx.config.R1;

public class MandatoryPropertyException extends ValidationException {

	public MandatoryPropertyException(String propertyName) {
		super(L10n.CONFIG_MSG_CFG_NULL_MANDATORY_VALUE, propertyName);
	}
}
