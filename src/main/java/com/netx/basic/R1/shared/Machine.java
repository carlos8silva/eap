package com.netx.basic.R1.shared;
import java.util.Properties;


public class Machine {

	// Property name constants:
	private static final String _PROP_OS_NAME = "os.name";
	private static final String _PROP_COUNTRY = "user.country";
	private static final String _PROP_LANG = "user.language";
	// Property value constants:
	private static final String _PLATF_WIN = "Windows";
	private static final String _OS_WIN_XP = "Windows XP";
	// Other constants:
	private static final byte _WINDOWS = 1;
	private static final byte _UNIX = 2;
	// Internal variables:
	private static final byte _platform;
	private static final OS _os;
	private static final String _country;
	private static final String _language;
	
	static {
		Properties props = System.getProperties();
		// Check the platform:
		String osName = props.getProperty(_PROP_OS_NAME);
		if(osName.startsWith(_PLATF_WIN)) {
			_platform = _WINDOWS;
		}
		else {
			_platform = _UNIX;
		}
		// Check OS:
		if(osName.equals(_OS_WIN_XP)) {
			_os = OS.WINDOWS_XP;
		}
		else {
			_os = null;
		}
		// Load system country:
		_country = props.getProperty(_PROP_COUNTRY);
		// Load system language:
		_language = props.getProperty(_PROP_LANG);
	}
	
	public static boolean runningOnWindows() {
		return _platform == _WINDOWS;
	}

	public static boolean runningOnUnix() {
		return _platform == _UNIX;
	}
	
	public static OS getOS() {
		return _os;
	}
	
	// For Globals:
	static String getSystemCountry() {
		return _country;
	}

	// For Globals:
	static String getSystemLanguage() {
		return _language;
	}
}
