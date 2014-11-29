package com.netx.generics.R1.util;
import java.io.StringReader;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.FirstStep;
import com.netx.generics.R1.translation.GenericScanner;
import com.netx.generics.R1.translation.RecognizeException;
import com.netx.generics.R1.translation.Recognizer;
import com.netx.generics.R1.translation.Recognizers;
import com.netx.generics.R1.translation.Token;
import com.netx.basic.R1.io.ExtendedReader;


class ExprScanner extends FirstStep {

	public Object performWork(Object o, ErrorList el) {
		return new InternalScanner().performWork(new ExtendedReader(new StringReader((String)o), "<string>"), el);
	}
	
	private class InternalScanner extends GenericScanner {
		public InternalScanner() {
			super(new Recognizer[] {
					new TextRecognizer(),
					new Recognizers.StringRecognizer("'\""),
					new Recognizers.NumberRecognizer(true),
					new Recognizers.SeparatorRecognizer("(,)[]"),
					new Recognizers.OperatorRecognizer(ExprConstants.OPERATORS),
					new Recognizers.IdentifierRecognizer(true, "_"),
				}, true, null
			);
		}
	}

	public static class TextRecognizer implements Recognizer {
		
		private boolean _isInsideBuiltin = false;
		
		public Token recognize(String line, int lineNum, int currentPos, ErrorList el) throws RecognizeException {
			if(line.charAt(currentPos) == ']') {
				_isInsideBuiltin = false;
				return null;
			}
			if(_isInsideBuiltin) {
				return null;
			}
			int start = currentPos;
			for( ; currentPos < line.length(); currentPos++) {
				// Skip escaped '[':
				if(line.charAt(currentPos) == '\\') {
					if(currentPos + 1 < line.length() && line.charAt(currentPos+1) == '[') {
						currentPos++;
					}
				}
				else if(line.charAt(currentPos) == '[') {
					_isInsideBuiltin = true;
					break;
				}
			}
			if(currentPos == 0) {
				// This happens when there is no text in the beginning:
				return null;
			}
			return new Token(EXPR_TYPE.TEXT, lineNum, start, currentPos, line);
		}
	}
}