package com.netx.generics.R1.translation;


public class Results {

	private final Object _result;
	private final ErrorList _el;

	// for Translator:
	Results(Object result, ErrorList el) {
		_result = result;
		_el = el;
	}

	public Object getResult() {
		return _result;
	}
	
	public ErrorList getErrorList() {
		return _el;
	}
}
