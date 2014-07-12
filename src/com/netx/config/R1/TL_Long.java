package com.netx.config.R1;


public class TL_Long extends TL_Number<Long> {

	public Long parse(String value, PropertyDef pDef) throws TypeLoadException {
		Double d = parseAndCheckSize(value, pDef, Long.MAX_VALUE);
		return new Long(d.longValue());
	}
}
