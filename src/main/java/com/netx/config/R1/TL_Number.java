package com.netx.config.R1;


public abstract class TL_Number<T> extends SimpleTypeLoader<T> {

	protected Double parseAndCheckSize(String value, PropertyDef pDef, double maxValue) throws TypeLoadException {
		Double d = null;
		try {
			d = new Double(value);
		}
		catch(NumberFormatException nf) {
			throw new TypeLoadException(L10n.CONFIG_MSG_CFG_WRONG_FORMAT, pDef.name, pDef.type.id, value);
		}
		// Note: the behavior of this 'if' statement is also correct for Double.
		// If a String that exceeds the value of Double.MAX_VALUE is passed as
		// input, the resulting Double object corresponds to 'Infinity', which 
		// always evaluates true in comparison to Double.MAX_VALUE.
		if(d.doubleValue() > maxValue) {
			throw new TypeLoadException(L10n.CONFIG_MSG_CFG_VALUE_TRUNCATED, pDef.name, pDef.type.id, value);
		}
		return d;
	}
}
