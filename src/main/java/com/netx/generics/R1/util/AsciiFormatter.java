package com.netx.generics.R1.util;

import com.netx.basic.R1.eh.Checker;


// used in Tools
class AsciiFormatter {

	private static String _REPLACEMENT_CHAR = "_";
	
	public static String format(String source) {
		Checker.checkNull(source, "source");
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<source.length(); i++) {
			// find special (switchable) character:
			String s = _findSpecial(source.charAt(i));
			if(s != null) {
				sb.append(s);
			}
			else if(_isAcceptable(source.charAt(i))) {
				sb.append(source.charAt(i));
			}
			else {
				sb.append(_REPLACEMENT_CHAR);
			}
		}
		return sb.toString();
	}

	private static String _findSpecial(char c) {
		for(int i=0; i<_chars.length; i++) {
			if(_chars[i][0].charAt(0) == c) {
				return _chars[i][1];
			}
		}
		return null;
	}
	
	private static final boolean _isAcceptable(char c) {
		return Character.isLetterOrDigit(c) || _acceptableChars.indexOf(c)!=-1;
	}

	private static final String[][] _chars = {
		{"�", "A"},
		{"�", "a"},
		{"�", "A"},
		{"�", "a"},
		{"�", "ae"},
		{"�", "AE"},
		{"�", "A"},
		{"�", "a"},
		{"�", "A"},
		{"�", "a"},
		{"�", "A"},
		{"�", "a"},
		{"�", "A"},
		{"�", "a"},
		{"�", "C"},
		{"�", "c"},
		{"�", "E"},
		{"�", "e"},
		{"�", "E"},
		{"�", "e"},
		{"�", "E"},
		{"�", "e"},
		{"�", "E"},
		{"�", "e"},
		{"�", "I"},
		{"�", "i"},
		{"�", "I"},
		{"�", "i"},
		{"�", "I"},
		{"�", "i"},
		{"�", "I"},
		{"�", "i"},
		{"�", "N"},
		{"�", "n"},
		{"�", "O"},
		{"�", "o"},
		{"�", "O"},
		{"�", "o"},
		{"�", "O"},
		{"�", "o"},
		{"�", "OE"},
		{"�", "oe"},
		{"�", "o"},
		{"�", "o"},
		{"�", "O"},
		{"�", "o"},
		{"�", "O"},
		{"�", "o"},
		{"�", "S"},
		{"�", "s"},
		{"�", "U"},
		{"�", "u"},
		{"�", "U"},
		{"�", "u"},
		{"�", "U"},
		{"�", "u"},
		{"�", "U"},
		{"�", "u"},
		{"�", "Y"},
		{"�", "y"},
		{"�", "Y"},
		{"�", "y"},
	};

	private static final String _acceptableChars = "\"\'_-+$!.()[]\\/";
}
