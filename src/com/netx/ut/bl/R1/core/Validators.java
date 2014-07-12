package com.netx.ut.bl.R1.core;
import com.netx.bl.R1.core.Field;
import com.netx.bl.R1.core.Validator;


public class Validators {

	public static class AlphaText extends _Text {
		public AlphaText() {
			super(false, "- \'");
		}
	}

	public static class ReadableText extends _Text {
		public ReadableText() {
			super(true, "- \'");
		}
	}

	public static class CodeIdentifier extends _Identifier {
		public CodeIdentifier() {
			super("._$");
		}
	}

	public static class TextIdentifier extends _Identifier {
		public TextIdentifier() {
			super("-_");
		}
	}

	public static class Username extends _Identifier {
		public Username() {
			super(".-_");
		}
	}
	
	private static abstract class _Text implements Validator {
		
		private final boolean _allowDigits;
		private final String _allowedChars;
		
		protected _Text(boolean allowDigits, String allowedChars) {
			_allowDigits = allowDigits;
			_allowedChars = allowedChars;
		}

		public String validate(Field field, Comparable<?> value) {
			String s = (String)value;
			String illegal = _checkString(s, _allowDigits, _allowedChars);
			if(illegal != null) {
				try {
					new Integer(illegal);
				}
				catch(NumberFormatException nfe) {
					return "illegal characters found: "+illegal;
				}
				return "no digits allowed";
			}
			return null;
		}
	}

	private static abstract class _Identifier implements Validator {
		
		private final String _allowedChars;
		
		protected _Identifier(String allowedChars) {
			_allowedChars = allowedChars;
		}

		public String validate(Field field, Comparable<?> value) {
			String s = (String)value;
			if(!Character.isLetter(s.charAt(0))) {
				return "the first character must be a letter";
			}
			String illegal = _checkString(s, true, _allowedChars);
			if(illegal != null) {
				return "illegal characters found: "+illegal;
			}
			return null;
		}
	}

	private static String _checkString(String s, boolean allowDigits, String allowedChars) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if(Character.isLetter(c)) {
				continue;
			}
			if(allowDigits) {
				if(Character.isDigit(c)) {
					continue;
				}
			}
			if(allowedChars.indexOf(c) != -1) {
				continue;
			}
			else {
				if(Character.isWhitespace(c)) {
					sb.append("<space>");
				}
				else {
					sb.append(c);
				}
			}
		}
		if(sb.length() > 0) {
			return sb.toString();
		}
		return null;
	}
}
