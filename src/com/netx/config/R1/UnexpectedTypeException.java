package com.netx.config.R1;

public class UnexpectedTypeException extends ValidationException {

	public UnexpectedTypeException(String propertyName, Class<?> expected, Class<?> found) {
		super(L10n.CONFIG_MSG_CFG_UNEXPECTED_TYPE, propertyName, expected.getSimpleName(), found.getSimpleName());
	}
}
