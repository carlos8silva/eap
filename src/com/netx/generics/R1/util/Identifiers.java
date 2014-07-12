package com.netx.generics.R1.util;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.generics.R1.translation.ParseException;


public class Identifiers {

	// TYPE:
	public static boolean isTextIdentifier(String s, boolean acceptDot) {
		Checker.checkEmpty(s, "s");
		try {
			Checker.checkTextIdentifier(s, "s", acceptDot);
			return true;
		}
		catch(IllegalArgumentException iae) {
			return false;
		}
	}

	public static boolean isTextIdentifier(String s) {
		return isTextIdentifier(s, false);
	}

	public static boolean isCodeIdentifier(String s, boolean acceptDot) {
		Checker.checkEmpty(s, "s");
		try {
			Checker.checkCodeIdentifier(s, "s", acceptDot);
			return true;
		}
		catch(IllegalArgumentException iae) {
			return false;
		}
	}
	
	public static boolean isCodeIdentifier(String s) {
		return isCodeIdentifier(s, false);
	}

	public static String toTextIdentifier(String s, boolean acceptDot) {
		Checker.checkEmpty(s, "s");
		// Check format:
		try {
			Checker.checkCodeIdentifier(s, "s", acceptDot);
		}
		catch(IllegalArgumentException iae) {
			throw new ParseException(iae.getMessage());
		}
		// Convert:
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if(Character.isDigit(c)) {
				sb.append(c);
			}
			else if(c == '_' || Character.isLetter(c)) {
				if(c == '_' || Character.isUpperCase(c)) {
					if(sb.length() > 0 && sb.charAt(sb.length()-1) != '-') {
						sb.append('-');
					}
					c = Character.toLowerCase(c);
				}
				if(c != '_') {
					sb.append(c);
				}
			}
			else {
				throw new IntegrityException(c);
			}
		}
		return sb.toString();
	}

	public static String toTextIdentifier(String s) {
		return toTextIdentifier(s, false);
	}

	public static String toCodeIdentifier(String s, boolean acceptDot) {
		Checker.checkEmpty(s, "s");
		// Check format:
		try {
			Checker.checkTextIdentifier(s, "s", acceptDot);
		}
		catch(IllegalArgumentException iae) {
			throw new ParseException(iae.getMessage());
		}
		// Convert:
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if(Character.isDigit(c)) {
				sb.append(c);
			}
			else if(c == '_' || Character.isLetter(c)) {
				sb.append(c);
			}
			else if(c == '-') {
				sb.append(Character.toUpperCase(s.charAt(++i)));
			}
			else {
				throw new IntegrityException(c);
			}
		}
		return sb.toString();
	}

	public static String toCodeIdentifier(String s) {
		return toCodeIdentifier(s, false);
	}
}
