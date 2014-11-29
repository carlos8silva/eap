package com.netx.generics.R1.translation;


// TODO these should have msgs from the L10n store
public class RecognizeException extends Exception {

	public final int errorPosition;
	public final int resumePosition;
	
	public RecognizeException(String message, int errorPosition, int resumePosition) {
		super(message);
		this.errorPosition = errorPosition;
		this.resumePosition = resumePosition;
	}
}
