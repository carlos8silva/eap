package com.netx.config.R1;

public class ReadOnlyPropertyException extends ValidationException {

	public ReadOnlyPropertyException(String propertyName) {
		super(L10n.CONFIG_MSG_CFG_READ_ONLY_PROPERTY, propertyName);
	}
}
