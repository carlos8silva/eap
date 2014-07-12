package com.netx.config.R1;


public class TL_Byte extends TL_Number<Byte> {

	public Byte parse(String value, PropertyDef pDef) throws TypeLoadException {
		Double d = parseAndCheckSize(value, pDef, Byte.MAX_VALUE);
		return new Byte(d.byteValue());
	}
}
