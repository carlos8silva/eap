package com.netx.config.R1;


public class TL_Character extends SimpleTypeLoader<Character> {

	public Character parse(String value, PropertyDef pDef) throws TypeLoadException {
		if(value.length() > 1) {
			throw new TypeLoadException(L10n.CONFIG_MSG_CFG_CHAR_SIZE_EXCEEDED, pDef.name, value);
		}
		return new Character(value.charAt(0));
	}
}
