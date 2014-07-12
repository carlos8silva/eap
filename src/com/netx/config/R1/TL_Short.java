package com.netx.config.R1;


public class TL_Short extends TL_Number<Short> {

	public Short parse(String value, PropertyDef pDef) throws TypeLoadException {
		Double d = parseAndCheckSize(value, pDef, Short.MAX_VALUE);
		return new Short(d.shortValue());
	}
}
