package com.netx.generics.R1.util;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.MessageFormatter;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.TimeValue;
import com.netx.generics.R1.time.Timestamp;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.ErrorListException;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.logging.Logger;
import com.netx.basic.R1.logging.Logger.LEVEL;


public class Tools {

	public static String toHTML(String source) {
		Checker.checkNull(source, "source");
		return HtmlFormatter.format(source);
	}

	public static String toSafeString(String source) {
		Checker.checkNull(source, "source");
		return AsciiFormatter.format(source);
	}

	@SuppressWarnings("unchecked")
	public static Object createObject(Class<?> c, Class<?>[] params, Object[] args) throws ConstructionException {
		Checker.checkNull(c, "c");
		Checker.checkNullElements(params, "params");
		Checker.checkNull(args, "args");
		// Check if the class is an interface:
		int modifiers = c.getModifiers();
		if(Modifier.isInterface(modifiers)) {
			throw new IllegalConstructionTargetException(false);
		}
		// Check if the class is abstract:
		// NOTE: this needs to be tested _after_ Modifier.isInterface,
		// because Modifier.isAbstract also returns true for interfaces.
		if(Modifier.isAbstract(modifiers)) {
			throw new IllegalConstructionTargetException(true);
		}
		try {
			// Create "regular" (no constructor) enum:
			if(c.isEnum() && params.length==1 && params[0]==String.class && args.length==1 && args[0].getClass()==String.class) {
				@SuppressWarnings("rawtypes")
				Object result = Enum.valueOf((Class<? extends Enum>)c, (String)args[0]); 
				return result;
			}
			else {
				try {
					Constructor<?> cons = c.getDeclaredConstructor(params);
					return cons.newInstance(args);
				}
				catch(NoSuchMethodException nsme) {
					// Did not find a direct match, let's look for a sub-class match:
					Constructor<?>[] constructors = c.getConstructors();
					for(Constructor<?> cons : constructors) {
						Class<?>[] consParams = cons.getParameterTypes();
						if(consParams.length == params.length) {
							boolean matchFound = true;
							for(int i=0; i<params.length; i++) {
								if(!consParams[i].isAssignableFrom(params[i])) {
									matchFound = false;
									break;
								}
							}
							if(matchFound) {
								return cons.newInstance(args);
							}
						}
					}
					// If it got here, no appropriate constructor has been found:
					throw new ConstructorNotFoundException();
				}
			}
		}
		catch(IllegalAccessException iae) {
			throw new ConstructorNotVisibleException();
		}
		catch(InstantiationException ie) {
			// This can't happen, since we tested 
			// whether the class was abstract before:
			throw new IntegrityException(ie);
		}
		catch(InvocationTargetException ite) {
			throw new ConstructorInvocationException(ite.getCause());
		}
	}

	public static Object createObject(Class<?> c, Object ... args) throws ConstructionException {
		Checker.checkNull(c, "c");
		Checker.checkNullElements(args, "args");
		Class<?>[] params = new Class[args.length];
		for(int i=0; i<params.length; i++) {
			params[i] = args[i].getClass();
		}
		return createObject(c, params, args);
	}

	public static Object createObject(Class<?> c) throws ConstructionException {
		return createObject(c, new Class[0], new Object[0]);
	}
	
	public static void handleCriticalError(String message, Throwable t) {
		// TODO this must attempt to log in the error log as well.
		System.err.println("[CRITICAL ERROR]: "+(Strings.isEmpty(message) ? "" : message));
		if(t != null) {
			t.printStackTrace(System.err);
		}
		else {
			System.err.println("(null exception)");
		}
	}

	public static void handleCriticalError(Throwable t) {
		handleCriticalError(null, t);
	}
	
	public static boolean contains(Collection<?> c, Object o, Comparator<Object> comparator) {
		Checker.checkNull(c, "c");
		Checker.checkNull(comparator, "comparator");
		for(Object element : c) {
			if(comparator.compare(element, o) == 0) {
				return true;
			}
		}
		return false;
	}
	
	public static void log(Logger logger, ErrorListException e, MessageFormatter mf) {
		Checker.checkNull(logger, "logger");
		Checker.checkNull(e, "e");
		try {
			if(logger.getEnabled()) {
				logger.error(e);
				ErrorList el = e.getErrorList();
				List<String> errors = el.getErrors(mf);
				List<String> warnings = el.getWarnings(mf);
				if(errors != null) {
					for(String msg : errors) {
						logger.error(msg, null);
					}
				}
				if(logger.getLevel() != LEVEL.ERROR && warnings != null) {
					for(String msg : warnings) {
						logger.warn(msg);
					}
				}
			}
		}
		catch(Throwable t) {
			handleCriticalError(t);
		}
	}

	public static Locale getLocale(String locale) {
		Checker.checkEmpty(locale, "locale");
		String[] array = locale.split("[_]");
		if(array.length != 2) {
			throw new IllegalArgumentException("wrong format for locale: "+locale);
		}
		return getLocale(array[0], array[1]);
	}

	public static Locale getLocale(String language, String country) {
		Checker.checkEmpty(language, "language");
		Checker.checkEmpty(country, "country");
		Locale l = new Locale(language, country);
		if(Arrays.find(l, Locale.getAvailableLocales()) == -1) {
			throw new IllegalArgumentException("locale '"+language+"_"+country+"' is not supported");
		}
		return l;
	}
		
	public static void sleep(TimeValue tv, Logger logger) {
		Checker.checkNull(tv, "tv");
		sleep(tv.milliseconds(), logger);
	}

	// TODO document
	public static void sleep(TimeValue tv) {
		sleep(tv, Globals.getLogger());
	}

	public static void sleep(long millis, Logger logger) {
		Checker.checkMinValue(millis, 0, "millis");
		try {
			Thread.sleep(millis);
		}
		catch(InterruptedException ie) {
			if(logger != null) {
				// TODO use global logger
				logger.warn("sleep was interrupted", ie);
			}
		}
	}

	// TODO document
	public static void sleep(long millis) {
		sleep(millis, Globals.getLogger());
	}

	// TODO document
	public static void readInput() {
		try {
			System.in.read();
			System.in.read();
		}
		catch(IOException io) {
			throw new IntegrityException(io);
		}
	}

	// TODO document
	public static byte[] parseInetAddress(String address) {
		Checker.checkEmpty(address, "address");
		String[] array = address.split("\\.");
		if(array.length != 4) {
			throw new IllegalArgumentException("illegal IP address: "+address);
		}
		byte[] result = new byte[array.length];
		for(int i=0; i<array.length; i++) {
			try {
				Short s = new Short(array[i]);
				if(s > 255) {
					throw new IllegalArgumentException("illegal IP address: "+address+" (wrong segment '"+array[i]+"')");
				}
				result[i] = s.byteValue();
			}
			catch(NumberFormatException nfe) {
				throw new IllegalArgumentException("illegal IP address: "+address+" (wrong segment '"+array[i]+"')");
			}
		}
		return result;
	}

	// TODO document
	public static void sendDisableCacheHeaders(HttpServletResponse response) {
		response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT"); // Date in the past
		response.setDateHeader("Last-Modified", new Timestamp().getTimeInMilliseconds()); // always modified
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		// Supposedly, the Pragma header is a request header, not a response header (ie, only HTTP clients
		// should be using it). However, IE seems to use it to avoid caching, so we will keep sending it.
		response.addHeader("Pragma", "no-cache");
	}

	// TODO document
	public static void sendEnableCacheHeaders(HttpServletResponse response) {
		Date shift = new Timestamp().getDate();
		shift = new Date(shift.getYear()+1, shift.getMonth(), shift.getDay());
		Timestamp future = new Timestamp(shift);
		response.setDateHeader("Expires", future.getTimeInMilliseconds()); // Date in the future
		response.setHeader("Last-Modified", "Mon, 26 Jul 1997 05:00:00 GMT"); // Date in the past
	}
}