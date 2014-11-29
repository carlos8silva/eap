package com.netx.config.R1;


public class TL_Double extends TL_Number<Double> {

	public Double parse(String value, PropertyDef pDef) throws TypeLoadException {
		return parseAndCheckSize(value, pDef, Double.MAX_VALUE);
	}
}
