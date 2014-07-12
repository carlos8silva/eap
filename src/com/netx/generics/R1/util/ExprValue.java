package com.netx.generics.R1.util;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.TYPE;
import com.netx.generics.R1.translation.Token;


class ExprValue extends Expression {

	public final Token value;
	
	public ExprValue(Token v) {
		value = v;
	}

	public Object evaluate(ErrorList el) {
		if(value.getType() == TYPE.STRING_CONSTANT) {
			return value.toString();
		}
		try {
			if(value.getType() == TYPE.FLOAT_CONSTANT) {
				return new Double(value.toString());
			}
			if(value.getType() == TYPE.INTEGER_CONSTANT) {
				return new Integer(value.toString());
			}
			throw new IntegrityException(value.getType());
		}
		catch(NumberFormatException nfe) {
			el.addError(value, "expected number, found: '"+value.toString()+"'");
			return null;
		}

	}
}
