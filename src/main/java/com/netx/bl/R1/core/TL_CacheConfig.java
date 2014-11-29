package com.netx.bl.R1.core;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.shared.Constants;
import com.netx.config.R1.PropertyDef;
import com.netx.config.R1.SimpleTypeLoader;
import com.netx.config.R1.TypeLoadException;


// Format:
// full
// none
// limited(<num-rows>)
public class TL_CacheConfig extends SimpleTypeLoader<CacheConfig> {

	// TYPE:
	private static final String _FULL = "full";
	private static final String _NONE = "none";
	private static final String _LIMITED = "limited";
	
	// INSTANCE:
	public CacheConfig parse(String value, PropertyDef pDef) throws TypeLoadException {
		if(value.equals(_FULL)) {
			return new CacheConfig(CacheConfig.CACHE_POLICY.FULL, -1);
		}
		if(value.equals(_NONE)) {
			return new CacheConfig(CacheConfig.CACHE_POLICY.NONE, 0);
		}
		if(value.startsWith(_LIMITED)) {
			String s = Strings.replaceAll(value, _LIMITED, Constants.EMPTY);
			if(!s.startsWith("(") || !s.endsWith(")")) {
				throw new TypeLoadException(L10n.BL_MSG_CFG_CACHE_CONFIG_MISSING_ARG, value);
			}
			s = s.substring(1, s.length()-1);
			try {
				return new CacheConfig(CacheConfig.CACHE_POLICY.LIMITED, new Integer(s));
			}
			catch(NumberFormatException nfe) {
				throw new TypeLoadException(L10n.BL_MSG_CFG_CACHE_CONFIG_WRONG_ARG, s);
			}
		}
		throw new TypeLoadException(L10n.BL_MSG_CFG_WRONG_CACHE_CONFIG_FORMAT, value);
	}
}
