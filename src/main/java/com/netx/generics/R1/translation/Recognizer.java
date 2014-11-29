package com.netx.generics.R1.translation;

public interface Recognizer {
	
	public Token recognize(String line, int lineNum, int currentPos, ErrorList el) throws RecognizeException;

}
