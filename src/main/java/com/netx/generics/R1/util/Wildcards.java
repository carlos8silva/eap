package com.netx.generics.R1.util;
import java.util.regex.Pattern;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.eh.Checker;


public class Wildcards {

	// TYPE:
	private static final String _WILDCHAR = "*";
	private static final String _REGEX_EQ = ".*";
	
	public static Wildcards compile(String pattern, boolean ignoreCase) {
		Checker.checkEmpty(pattern, "pattern");
		if(pattern.contains("**")) {
			throw new IllegalArgumentException("parse error in pattern");
		}
		if(pattern.equals(_WILDCHAR)) {
			return new Wildcards(null, ignoreCase);
		}
		else {
			// Escape any regexp special characters:
			pattern = Strings.replaceAll(pattern, ".", "\\.");
			pattern = Strings.replaceAll(pattern, "+", "\\+");
			pattern = Strings.replaceAll(pattern, "$", "\\$");
			// Translate wildchar to equivalent regexp:
			pattern = Strings.replaceAll(pattern, _WILDCHAR, _REGEX_EQ);
			return new Wildcards(pattern, ignoreCase);
		}
	}

	public static Wildcards compile(String pattern) {
		return compile(pattern, false);
	}

	// INSTANCE:
	private final Pattern _pattern;
	
	private Wildcards(String pattern, boolean ignoreCase) {
		if(pattern == null) {
			_pattern = null;
		}
		else {
			_pattern = Pattern.compile(pattern, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
		}
	}
	
	public boolean matches(String toMatch) {
		if(_pattern == null) {
			return true;
		}
		return _pattern.matcher(toMatch).matches();
	}
}
