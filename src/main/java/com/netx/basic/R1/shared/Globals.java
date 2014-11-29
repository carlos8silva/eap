package com.netx.basic.R1.shared;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.Locale;
import com.netx.basic.R1.logging.Logger;
import com.netx.basic.R1.eh.Checker;
import com.netx.generics.R1.util.Tools;


public class Globals {

	private static Logger _logger;
	private static int _tabSize;
	private static RUN_MODE _runMode;
	private static OPTIMIZATION _optimization;
	private static DATE_ORDER _dateOrder;
	private static Locale _applicationLocale;
	private static Locale _systemLocale;
	private static final Timer _timer;
	private static final List<Disposable> _disposables;
	// This will prevent changes to the disposable
	// list from happening while iterating it.
	private static boolean _disposing;
	
	static {
		_disposables = new ArrayList<Disposable>();
		_disposing = false;
		_logger = new Logger(System.out);
		_logger.setLevel(Logger.LEVEL.INFO);
		_tabSize = 4;
		_runMode = RUN_MODE.PROD;
		_optimization = OPTIMIZATION.PROCESSOR;
		_dateOrder = DATE_ORDER.EU;
		_timer = new Timer(true);
		_applicationLocale = Locale.getDefault();
		// TODO the system properties we chose are not appropriate to find out what the OS language is
		//_systemLocale = Tools.getLocale(Machine.getSystemLanguage(), Machine.getSystemCountry());
		_systemLocale = Tools.getLocale("en", "GB");
		// Add shutdown hook to dispose of disposable objects
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				_disposing = true;
				// Dispose of all disposables:
				Iterator<Disposable> it = _disposables.iterator();
				while(it.hasNext()) {
					Disposable d = it.next(); 
					try {
						d.onDispose();
					}
					catch(Throwable t) {
						Tools.handleCriticalError("found error while disposing of element '"+(d == null ? "null" : d.toString())+"'", t);
					}
				}
				// Stop timer:
				_timer.cancel();
			}
		});
	}
	
	public static boolean registerDisposable(Disposable d) {
		Checker.checkNull(d, "d");
		if(_disposing) {
			return false;
		}
		return _disposables.add(d);
	}

	public static boolean unregisterDisposable(Disposable d) {
		Checker.checkNull(d, "d");
		if(_disposing) {
			return false;
		}
		return _disposables.remove(d);
	}

	public static Logger getLogger() {
		return _logger;
	}
	
	public static int getTabSize() {
		return _tabSize;
	}
	
	public static void setTabSize(int value) {
		Checker.checkNull(value, "value");
		_tabSize = value;
	}

	public static RUN_MODE getRunMode() {
		return _runMode;
	}
	
	public static void setRunMode(RUN_MODE value) {
		Checker.checkNull(value, "value");
		_runMode = value;
	}

	public static OPTIMIZATION getOptimization() {
		return _optimization;
	}
	
	public static void setOptimization(OPTIMIZATION value) {
		Checker.checkNull(value, "value");
		_optimization = value;
	}

	public static DATE_ORDER getDateOrder() {
		return _dateOrder;
	}
	
	public static void setDateOrder(DATE_ORDER value) {
		Checker.checkNull(value, "value");
		_dateOrder = value;
	}

	public static Timer getTimer() {
		return _timer;
	}
	
	public static Locale getApplicationLocale() {
		return _applicationLocale;
	}

	public static void setApplicationLocale(Locale locale) {
		Checker.checkNull(locale, "locale");
		_applicationLocale = locale;
		Locale.setDefault(locale);
	}

	public static Locale getSystemLocale() {
		return _systemLocale;
	}

	public static void setSystemLocale(Locale locale) {
		Checker.checkNull(locale, "locale");
		_systemLocale = locale;
	}
}
