package com.netx.generics.R1.translation;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.shared.Constants;
import com.netx.generics.R1.util.Strings;


public class Recognizers {

	public static class StringRecognizer implements Recognizer {

		private final String _delimiters;
		private final String _escapes;
		
		public StringRecognizer(String delimiters, String escapes) {
			Checker.checkEmpty(delimiters, "delimiters");
			_delimiters = delimiters;
			// Add 'delimiters' to 'escapes':
			escapes = escapes==null ? "" : escapes;
			for(int i=0; i<delimiters.length(); i++) {
				if(escapes.indexOf(delimiters.charAt(i)) == -1) {
					escapes = escapes + delimiters.charAt(i);
				}
			}
			_escapes = escapes;
		}

		public StringRecognizer(String delimiters) {
			this(delimiters, Constants.ESCAPE_CHARS);
		}

		public StringRecognizer() {
			this("\"");
		}
		
		public Token recognize(String line, int lineNum, int currentPos, ErrorList el) throws RecognizeException {
			if(_delimiters.indexOf(line.charAt(currentPos)) == -1) {
				return null;
			}
			char delimiter = line.charAt(currentPos);
			int initialPos = currentPos++;
			int illegalEscape = -1;
			while(currentPos < line.length()) {
				// End of string:
				if(line.charAt(currentPos) == delimiter) {
					currentPos++;
					if(illegalEscape != -1) {
						el.addError(1, illegalEscape, "illegal escape sequence: '\\"+line.charAt(illegalEscape)+"'");
					}
					return new Token(TYPE.STRING_CONSTANT, lineNum, initialPos, currentPos, line);
				}
				// Escape:
				else if(line.charAt(currentPos) == '\\') {
					currentPos++;
					if(_escapes.indexOf(line.charAt(currentPos)) == -1) {
						// Don't throw exception, let the recognizer finish reading
						// the string, and position itself at the end of it.
						illegalEscape = currentPos;
					}
					currentPos++;
				}
				else {
					currentPos++;
				}
			}
			// If we got here, the end of String hasn't been found:
			throw new RecognizeException("unterminated String constant", initialPos, currentPos);
		}
	}

	public static class NumberRecognizer implements Recognizer {
		private final boolean _recognizeFloats;
	
		public NumberRecognizer(boolean recognizeFloats) {
			_recognizeFloats = recognizeFloats;
		}

		public NumberRecognizer() {
			this(false);
		}

		public Token recognize(String line, int lineNum, int currentPos, ErrorList el) throws RecognizeException {
			// Careful with negative numbers:
			int beginPos = currentPos;
			if(line.charAt(currentPos) == '-') {
				currentPos++;
			}
			boolean foundDigits = false;
			while(currentPos<line.length()) {
				if(Character.isDigit(line.charAt(currentPos))) {
					foundDigits = true;
					currentPos++;
				}
				else {
					break;
				}
			}
			if(!foundDigits) {
				return null;
			}
			boolean isFloat = false;
			if(currentPos<line.length() && _recognizeFloats) {
				if(line.charAt(currentPos) == '.') {
					isFloat = true;
					currentPos++;
					if(Character.isDigit(line.charAt(currentPos))) {
						while(currentPos<line.length()) {
							if(Character.isDigit(line.charAt(currentPos))) {
								foundDigits = true;
								currentPos++;
							}
							else {
								break;
							}
						}
					}
					else {
						throw new RecognizeException("expected number, found: '"+line.charAt(currentPos)+"'", currentPos, currentPos);
					}
				}
			}
			return new Token(isFloat ? TYPE.FLOAT_CONSTANT : TYPE.INTEGER_CONSTANT, lineNum, beginPos, currentPos, line);
		}
	}

	public static class SeparatorRecognizer implements Recognizer {
		private final String _separators;
		
		public SeparatorRecognizer(String separators) {
			Checker.checkEmpty(separators, "separators");
			_separators = separators;
		}

		public Token recognize(String line, int lineNum, int currentPos, ErrorList el) throws RecognizeException {
			if(_separators.indexOf(line.charAt(currentPos)) != -1) {
				return new Token(TYPE.SEPARATOR, lineNum, currentPos, currentPos+1, line);
			}
			else {
				return null;
			}
		}
	}

	public static class OperatorRecognizer implements Recognizer {
		private final String[] _operators;
		
		public OperatorRecognizer(String[] operators) {
			Checker.checkEmptyElements(operators, "operators");
			_operators = operators;
		}

		public Token recognize(String line, int lineNum, int currentPos, ErrorList el) throws RecognizeException {
			for(int i=0; i<_operators.length; i++) {
				if(line.charAt(currentPos) == _operators[i].charAt(0)) {
					if(_operators[i].length() == 1) {
						return new Token(TYPE.OPERATOR, lineNum, currentPos, currentPos+1, line);
					}
					else {
						if(line.substring(currentPos).startsWith(_operators[i])) {
							return new Token(TYPE.OPERATOR, lineNum, currentPos, currentPos+_operators[i].length(), line);
						}
					}
				}
			}
			return null;
		}
	}

	public static class IdentifierRecognizer implements Recognizer {
		private final boolean _acceptDigits;
		private final String _acceptableStartChars;
		private final String _acceptableChars;
		private final String[] _keywords;
		private final boolean _ignoreCase;
		
		public IdentifierRecognizer(boolean acceptDigits, String allowedChars, String allowedStartChars, String[] keywords, boolean ignoreCase) {
			if(keywords != null) {
				Checker.checkNullElements(keywords, "keywords");
			}
			_acceptDigits = acceptDigits;
			_acceptableChars = allowedChars==null ? "" : allowedChars;
			_acceptableStartChars = allowedStartChars==null ? "" : allowedStartChars;
			_keywords = keywords;
			_ignoreCase = ignoreCase;
		}

		public IdentifierRecognizer(boolean acceptDigits, String allowedChars, String allowedStartChars) {
			this(acceptDigits, allowedChars, allowedStartChars, null, false);
		}

		public IdentifierRecognizer(boolean acceptDigits, String allowedChars, String[] keywords, boolean ignoreCase) {
			this(acceptDigits, allowedChars, allowedChars, keywords, ignoreCase);
		}

		public IdentifierRecognizer(boolean acceptDigits, String allowedChars) {
			this(acceptDigits, allowedChars, allowedChars, null, false);
		}

		public Token recognize(String line, int lineNum, int currentPos, ErrorList el) throws RecognizeException {
			if(!Character.isLetter(line.charAt(currentPos)) && _acceptableStartChars.indexOf(line.charAt(currentPos))==-1) {
				return null;
			}
			int beginPos = currentPos;
			for(currentPos++; currentPos < line.length(); currentPos++) {
				if(Character.isLetter(line.charAt(currentPos))) {
					continue;
				}
				if(_acceptDigits && Character.isDigit(line.charAt(currentPos))) {
					continue;
				}
				if(_acceptableChars.indexOf(line.charAt(currentPos)) != -1) {
					continue;
				}
				break;
			}
			if(_keywords != null) {
				String value = line.substring(beginPos, currentPos);
				if(Strings.find(value, _keywords, _ignoreCase) != -1) {
					return new Token(TYPE.KEYWORD, lineNum, beginPos, currentPos, line);
				}
			}
			return new Token(TYPE.IDENTIFIER, lineNum, beginPos, currentPos, line);
		}
	}
}
