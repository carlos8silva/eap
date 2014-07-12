package com.netx.config.R1;
import java.util.Map;
import java.util.HashMap;
import com.netx.generics.R1.sql.JdbcDriver;
import com.netx.generics.R1.sql.Database;
import com.netx.generics.R1.time.TimeValue;
import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.logging.Logger;
import com.netx.basic.R1.shared.DATE_ORDER;
import com.netx.basic.R1.shared.OPTIMIZATION;
import com.netx.basic.R1.shared.RUN_MODE;
import com.netx.bl.R1.core.CacheConfig;
import com.netx.bl.R1.core.TL_CacheConfig;


class Dictionary {

	// Internal variables:
	private static final Map<Class<?>,TypeDef> _types;
	private static final Map<String,ContextDef> _contexts;
	private static final ContextDef _root;

	// Initiate built-in types and contexts:
	static {
		// Types:
		_types = new HashMap<Class<?>,TypeDef>();
		_types.put(Boolean.class, new TypeDef(Boolean.class, new TL_Boolean()));
		_types.put(Character.class, new TypeDef(Character.class, new TL_Character()));
		_types.put(Byte.class, new TypeDef(Byte.class, new TL_Byte()));
		_types.put(Short.class, new TypeDef(String.class, new TL_String()));
		_types.put(Integer.class, new TypeDef(Integer.class, new TL_Integer()));
		_types.put(Long.class, new TypeDef(Long.class, new TL_Long()));
		_types.put(Float.class, new TypeDef(Float.class, new TL_Float()));
		_types.put(Double.class, new TypeDef(Double.class, new TL_Double()));
		_types.put(String.class, new TypeDef(String.class, new TL_String()));
		_types.put(JdbcDriver.class, new TypeDef(JdbcDriver.class, new TL_JdbcDriver()));
		_types.put(Directory.class, new TypeDef(Directory.class, new TL_Directory()));
		Map<String,PropertyDef> loggerProps = new HashMap<String,PropertyDef>();
		loggerProps.put("level", new PropertyDef("level", false, getTypeDef(Logger.LEVEL.class), false, false, "ERROR"));
		loggerProps.put("location", new PropertyDef("location", false, getTypeDef(Directory.class), true, true, null));
		loggerProps.put("filename", new PropertyDef("filename", false, getTypeDef(String.class), true, true, "System.out"));
		loggerProps.put("num-days", new PropertyDef("num-days", false, getTypeDef(Integer.class), true, true, null));
		_types.put(Logger.class, new TypeDef(Logger.class, new TL_Logger(), loggerProps));
		Map<String,PropertyDef> cdProps = new HashMap<String,PropertyDef>();
		cdProps.put("driver", new PropertyDef("driver", false, getTypeDef(JdbcDriver.class), true, true, null));
		cdProps.put("server", new PropertyDef("server", false, getTypeDef(String.class), true, true, null));
		cdProps.put("port", new PropertyDef("port", false, getTypeDef(Integer.class), false, true, null));
		cdProps.put("schema", new PropertyDef("schema", false, getTypeDef(String.class), true, true, null));
		cdProps.put("username", new PropertyDef("username", false, getTypeDef(String.class), true, true, null));
		cdProps.put("password", new PropertyDef("password", false, getTypeDef(String.class), true, true, null));
		_types.put(Database.class, new TypeDef(Database.class, new TL_ConnectionDetails(), cdProps));
		_types.put(TimeValue.class, new TypeDef(TimeValue.class, new TL_TimeValue()));
		_types.put(CacheConfig.class, new TypeDef(CacheConfig.class, new TL_CacheConfig()));
		// Contexts:
		_contexts = new HashMap<String,ContextDef>();
		// CTX GLOBALS:
		ContextDef globals = new ContextDef("globals");
		globals.properties.put("tab-size", new PropertyDef("tab-size", false, getTypeDef(Integer.class), true, false, "4"));
		globals.properties.put("run-mode", new PropertyDef("run-mode", false, getTypeDef(RUN_MODE.class), true, false, "PROD"));
		// TODO this should be of type Locale
		globals.properties.put("locale", new PropertyDef("locale", false, getTypeDef(String.class), true, false, "gb_GB"));
		Map<String,String> defaultLogger = new HashMap<String,String>();
		defaultLogger.put("level", "INFO");
		defaultLogger.put("filename", "System.out");
		globals.properties.put("logger", new PropertyDef("logger", false, getTypeDef(Logger.class), true, false, defaultLogger));
		globals.properties.put("optimization", new PropertyDef("optimization", false, getTypeDef(OPTIMIZATION.class), true, true, "PROCESSOR"));
		globals.properties.put("date-format", new PropertyDef("date-format", false, getTypeDef(DATE_ORDER.class), true, true, "EU"));
		_contexts.put("globals", globals);
		// CTX BL:
		ContextDef bl = new ContextDef("bl");
		bl.properties.put("use-prepared-statements", new PropertyDef("use-prepared-statements", false, getTypeDef(Boolean.class), true, false, "false"));
		bl.properties.put("cache-enabled", new PropertyDef("cache-enabled", false, getTypeDef(Boolean.class), true, false, "true"));
		bl.properties.put("cache-policies", new PropertyDef("cache-policies", true, getTypeDef(CacheConfig.class), true, false, null));
		bl.properties.put("disable-cache-daemon-delay", new PropertyDef("disable-cache-daemon-delay", false, getTypeDef(TimeValue.class), true, false, "2s"));
		bl.properties.put("number-of-lock-attempts-before-failure", new PropertyDef("number-of-lock-attempts-before-failure", false, getTypeDef(Integer.class), true, false, "2"));
		bl.properties.put("number-of-lock-attempts-before-release", new PropertyDef("number-of-lock-attempts-before-release", false, getTypeDef(Integer.class), true, false, "3"));
		bl.properties.put("sleep-time-before-lock-retry", new PropertyDef("sleep-time-before-lock-retry", false, getTypeDef(TimeValue.class), true, false, "1s"));
		bl.properties.put("sleep-time-after-lock-release", new PropertyDef("sleep-time-after-lock-release", false, getTypeDef(TimeValue.class), true, false, "1s"));
		bl.properties.put("logger", new PropertyDef("logger", false, getTypeDef(Logger.class), false, false, null));
		_contexts.put("bl", bl);
		// CTX EAP:
		ContextDef eap = new ContextDef("eap");
		ContextDef local = new ContextDef("local");
		local.properties.put("session-check-daemon-period", new PropertyDef("session-check-daemon-period", false, getTypeDef(TimeValue.class), true, false, "5m"));
		local.properties.put("notification-check-daemon-period", new PropertyDef("notification-check-daemon-period", false, getTypeDef(TimeValue.class), true, false, "1h"));
		local.properties.put("notification-keep-time", new PropertyDef("notification-keep-time", false, getTypeDef(TimeValue.class), true, false, "30m"));
		eap.contexts.put("local", local);
		ContextDef central = new ContextDef("central");
		central.properties.put("authenticator-name", new PropertyDef("authenticator-name", false, getTypeDef(String.class), true, true, null));
		central.properties.put("error-handler-name", new PropertyDef("error-handler-name", false, getTypeDef(String.class), true, true, null));
		central.properties.put("charset", new PropertyDef("charset", false, getTypeDef(String.class), true, true, "UTF-8"));
		central.properties.put("send-error-codes", new PropertyDef("send-error-codes", false, getTypeDef(Boolean.class), false, true, "true"));
		central.properties.put("session-mode", new PropertyDef("session-mode", false, getTypeDef(String.class), false, true, "normal"));
		central.properties.put("allow-multiple-sessions", new PropertyDef("allow-multiple-sessions", false, getTypeDef(Boolean.class), false, true, "false"));
		central.properties.put("keep-sessions-on-reload", new PropertyDef("keep-sessions-on-reload", false, getTypeDef(Boolean.class), false, false, "false"));
		central.properties.put("save-errors-in-database", new PropertyDef("save-errors-in-database", false, getTypeDef(Boolean.class), false, false, "true"));
		central.properties.put("show-stack-trace", new PropertyDef("show-stack-trace", false, getTypeDef(Boolean.class), false, false, "true"));
		eap.contexts.put("central", central);
		_contexts.put("eap", eap);
		// CTX CUBIGRAF:
		ContextDef cubigraf = new ContextDef("cubigraf");
		// CTX MAP-DEFAULTS:
		ContextDef mapDefaults = new ContextDef("map-defaults");
		Map<String,Object> defaults = new HashMap<String,Object>();
		defaults.put("one", "1");
		defaults.put("two", "2");
		defaults.put("three", "3");
		defaults.put("four", "4");
		defaults.put("five", "5");
		mapDefaults.properties.put("integers", new PropertyDef("integers", true, getTypeDef(Integer.class), true, false, defaults));
		_contexts.put("map-defaults", mapDefaults);
		// The root context definition, which will be tailored by applications.
		// Applications are free to add any context at the first level apart from 'globals'.
		_root = new ContextDef(null);
		_root.contexts.put("globals", globals);
		_root.contexts.put("eap", eap);
		_root.contexts.put("cubigraf", cubigraf);
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static TypeDef getTypeDef(Class<?> id) {
		if(id.isEnum()) {
			// Create an Enum type loader if it does not yet exist:
			TypeDef td = _types.get(id);
			if(td == null) {
				td = new TypeDef(id, new TL_Enum((Class<? extends Enum>)id));
				_types.put(id, td);
			}
			return td;
		}
		return _types.get(id);
	}

	public static ContextDef getContextDef(String id) {
		return _contexts.get(id);
	}
	
	public static ContextDef getRootDef() {
		return _root;
	}
}
