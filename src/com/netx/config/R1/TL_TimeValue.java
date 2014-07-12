package com.netx.config.R1;
import com.netx.generics.R1.time.TimeValue;


public class TL_TimeValue extends SimpleTypeLoader<TimeValue> {

	public TimeValue parse(String value, PropertyDef pDef) throws TypeLoadException {
		try {
			return new TimeValue(value);
		}
		catch(IllegalArgumentException iae) {
			throw new TypeLoadException(L10n.CONFIG_MSG_CFG_WRONG_FORMAT, pDef.name, pDef.type.id, value);
		}
	}
}
