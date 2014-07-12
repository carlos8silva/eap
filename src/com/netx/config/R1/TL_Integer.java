package com.netx.config.R1;


public class TL_Integer extends TL_Number<Integer> {

	public Integer parse(String value, PropertyDef pDef) throws TypeLoadException {
		Double d = parseAndCheckSize(value, pDef, Integer.MAX_VALUE);
		return new Integer(d.intValue());
	}
}
