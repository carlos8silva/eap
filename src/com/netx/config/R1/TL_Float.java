package com.netx.config.R1;


public class TL_Float extends TL_Number<Float> {

	public Float parse(String value, PropertyDef pDef) throws TypeLoadException {
		Double d = parseAndCheckSize(value, pDef, Float.MAX_VALUE);
		return new Float(d.floatValue());
	}
}
