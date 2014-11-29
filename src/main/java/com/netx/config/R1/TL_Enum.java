package com.netx.config.R1;


public class TL_Enum extends SimpleTypeLoader<Enum<?>> {

	@SuppressWarnings("rawtypes")
	private final Class<? extends Enum> _enumClass;
	
	@SuppressWarnings("rawtypes")
	public TL_Enum(Class<? extends Enum> enumClass) {
		_enumClass = enumClass;
	}

	@SuppressWarnings("unchecked")
	public Enum<?> parse(String value, PropertyDef pDef) throws TypeLoadException {
		try {
			return Enum.valueOf(_enumClass, value.toUpperCase());
		}
		catch(IllegalArgumentException iae) {
			throw new TypeLoadException(L10n.CONFIG_MSG_CFG_ILLEGAL_ENUM_CONSTANT, _enumClass.getName(), value);
		}
	}
}
