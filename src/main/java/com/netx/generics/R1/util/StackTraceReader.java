package com.netx.generics.R1.util;
import java.io.Reader;
import com.netx.basic.R1.eh.Checker;


public class StackTraceReader extends Reader {

	// TYPE:
	private final static String _PREFIX = "  at ";
	
	// INSTACE:
	private boolean _closed;
	private final StringBuilder _text;
	private int _currChar;
	
	public StackTraceReader(Throwable t) {
		super();
		_closed = false;
		_text = new StringBuilder();
		_text.append(t.toString());
		_text.append('\n');
		for(StackTraceElement ste : t.getStackTrace()) {
			_text.append(_PREFIX);
			_text.append(ste.toString());
			_text.append('\n');
		}
		_currChar = 0;
	}
	
	public void close() {
		_closed = true;
	}

	public void mark(int readAheadLimit) {
		throw new UnsupportedOperationException();
	}
	
	public int read() {
		_checkClosed();
		if(_currChar >= _text.length()) {
			return -1;
		}
		return _text.charAt(_currChar++);
	}
	
	public int read(char[] cbuf, int off, int len) {
		Checker.checkEmpty(cbuf, "cbuf");
		Checker.checkMinValue(off, 0, "off");
		if(off + len > cbuf.length) {
			throw new IllegalArgumentException("cbuf.length: "+cbuf.length+"; off+len: "+(off+len));
		}
		_checkClosed();
		if(_currChar >= _text.length()) {
			return -1;
		}
		int charsCopied = len;
		if(_currChar+len > _text.length()) {
			charsCopied = _text.length()-_currChar;
		}
		_text.getChars(_currChar, charsCopied, cbuf, off);
		_currChar += charsCopied;
		return charsCopied;
	}
	
	public boolean ready() {
		_checkClosed();
		if(_currChar >= _text.length()) {
			return false;
		}
		return true;
	}
	
	public void reset() {
		_checkClosed();
		_currChar = 0;
	}

	public long skip(long n) {
		Checker.checkMinValue(n, 0, "n");
		_checkClosed();
		long charsSkipped = n;
		if(_currChar+n > _text.length()) {
			charsSkipped = _text.length()-_currChar;
		}
		_currChar += charsSkipped;
		return charsSkipped;
	}
	
	private void _checkClosed() {
		if(_closed) {
			throw new IllegalStateException();
		}
	}
}
