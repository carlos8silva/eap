package com.netx.eap.R1.core;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.netx.generics.R1.sql.Database;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.logging.Logger;


public class Config {

	// Currently static properties:
	public static Locale SYSTEM_LOCALE = new Locale("en", "GB");
	public static Locale APP_LOCALE = new Locale("en", "GB");
	public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd HH:mm");
	public static final String APP_NAME = "RIT";
	public static final String CHARSET = "UTF-8";
	public static final int MAX_FAILED_LOGIN_TRIES = 5;
	public static final boolean CACHE_TEMPLATES = false;
	public static final boolean SEND_STATUS_CODES = false;
	public static final Logger LOGGER = Globals.getLogger();
	public static final Logger.LEVEL LOG_LEVEL = Logger.LEVEL.INFO;
	public static final String SUPPORT_PHONE = "3655";
	public static final String SUPPORT_EMAIL = "helpdesk@rit.com";
	public static final String DEFAULT_UI = "rit";
	// Properties that can be configured via the config.properties file:
	public static Database DB_CONNECTION = null;
	public static String LOG_DIR = null;
}
