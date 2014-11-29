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
		{"Á", "A"},
		{"á", "a"},
		{"Â", "A"},
		{"â", "a"},
		{"æ", "ae"},
		{"Æ", "AE"},
		{"À", "A"},
		{"à", "a"},
		{"Å", "A"},
		{"å", "a"},
		{"Ã", "A"},
		{"ã", "a"},
		{"Ä", "A"},
		{"ä", "a"},
		{"Ç", "C"},
		{"ç", "c"},
		{"É", "E"},
		{"é", "e"},
		{"Ê", "E"},
		{"ê", "e"},
		{"È", "E"},
		{"è", "e"},
		{"Ë", "E"},
		{"ë", "e"},
		{"Í", "I"},
		{"í", "i"},
		{"Î", "I"},
		{"î", "i"},
		{"Ì", "I"},
		{"ì", "i"},
		{"Ï", "I"},
		{"ï", "i"},
		{"Ñ", "N"},
		{"ñ", "n"},
		{"Ó", "O"},
		{"ó", "o"},
		{"Ô", "O"},
		{"ô", "o"},
		{"Ò", "O"},
		{"ò", "o"},
		{"Œ", "OE"},
		{"œ", "oe"},
		{"Ø", "o"},
		{"ø", "o"},
		{"Õ", "O"},
		{"õ", "o"},
		{"Ö", "O"},
		{"ö", "o"},
		{"Š", "S"},
		{"š", "s"},
		{"Ú", "U"},
		{"ú", "u"},
		{"Û", "U"},
		{"û", "u"},
		{"Ù", "U"},
		{"ù", "u"},
		{"Ü", "U"},
		{"ü", "u"},
		{"Ý", "Y"},
		{"ý", "y"},
		{"Ÿ", "Y"},
		{"ÿ", "y"},
	};

	private static final String _acceptableChars = "\"\'_-+$!.()[]\\/";
}
