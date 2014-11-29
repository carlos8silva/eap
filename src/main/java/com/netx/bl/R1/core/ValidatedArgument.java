package com.netx.bl.R1.core;


class ValidatedArgument extends Argument {

	public ValidatedArgument(Field field, Comparable<?> value) throws ValidationException {
		super(field, value);
		// Note: we need to use getValue() to ensure AllowedValues are cast to the appropriate data type
		field.validate(getValue());
		if(field.isReadOnly()) {
			throw new ReadOnlyFieldException(field);
		}
	}
}
