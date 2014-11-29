package com.netx.bl.R1.core;

public abstract class FieldValidated extends Field {

	private final ValidationExpr _vExpr;
	private final Validator _validator;
	
	protected FieldValidated(Field.TYPE type, MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, ValidationExpr vExpr, Validator validator) {
		super(type, owner, name, columnName, defaultValue, mandatory, readOnly);
		_vExpr = vExpr;
		_validator = validator;
	}

	protected ValidationExpr getValidationExpr() {
		return _vExpr;
	}

	protected Validator getValidator() {
		return _validator;
	}
}
