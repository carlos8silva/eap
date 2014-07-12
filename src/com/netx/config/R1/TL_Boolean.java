package com.netx.config.R1;
import com.netx.generics.R1.util.Strings;


public class TL_Boolean extends SimpleTypeLoader<Boolean> {

	public Boolean parse(String value, PropertyDef pDef) throws TypeLoadException {
		Boolean b = Strings.toBoolean(value);
		if(b == null) {
			throw new TypeLoadException(L10n.CONFIG_MSG_CFG_WRONG_FORMAT, pDef.name, pDef.type.id, value);
		}
		return b;
	}
}
