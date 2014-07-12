package com.netx.config.R1;
import com.netx.basic.R1.logging.Logger;
import com.netx.basic.R1.shared.DATE_ORDER;
import com.netx.basic.R1.shared.OPTIMIZATION;
import com.netx.basic.R1.shared.RUN_MODE;


public class GlobalsConfig extends ContextWrapper {

	// TYPE:
	public static final String TAB_SIZE = "tab-size";
	public static final String RUN_MODE = "run-mode";
	public static final String LOCALE = "locale";
	public static final String LOGGER = "logger";
	public static final String OPTIMIZATION = "optimization";
	public static final String DATE_FORMAT = "date-format";
	
	// INSTANCE:
	// For Context:
	GlobalsConfig(Context globals, String id) {
		super(globals, id);
	}
	
	public Integer getTabSize() {
		return getContext().getInteger(TAB_SIZE);
	}
	
	public void setTabSize(Integer value) {
		getContext().setProperty(TAB_SIZE, value);
	}

	public RUN_MODE getRunMode() {
		return (RUN_MODE)getContext().getObject(RUN_MODE);
	}

	public String getLocale() {
		return getContext().getString(LOCALE);
	}
	
	public void setLocale(String value) {
		getContext().setProperty(LOCALE, value);
	}

	public Logger getLogger() {
		return (Logger)getContext().getObject(LOGGER);
	}

	public OPTIMIZATION getOptimization() {
		return (OPTIMIZATION)getContext().getObject(OPTIMIZATION);
	}

	public DATE_ORDER getDateFormat() {
		return (DATE_ORDER)getContext().getObject(DATE_FORMAT);
	}

	public void setDateFormat(DATE_ORDER value) {
		getContext().setProperty(DATE_FORMAT, value);
	}
}
