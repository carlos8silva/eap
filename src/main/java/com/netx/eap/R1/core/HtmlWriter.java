package com.netx.eap.R1.core;
import java.io.Writer;
import java.io.IOException;
import com.netx.basic.R1.eh.Checker;
import com.netx.generics.R1.util.Tools;


// TODO move this to generics.R1.util and substitute current class
// TODO make it a subclass of ExtendedWriter
public class HtmlWriter extends Writer {

	private final Writer _out;
	private boolean _closed;

	public HtmlWriter(Writer out) {
		Checker.checkNull(out, "out");
		_out = out;
		_closed = false;
	}
	
	public void close() {
		_closed = true;
	}

	public void flush() throws IOException {
		_checkClosed();
		_out.flush();
	}

	public void write(char[] cbuf, int off, int len) throws IOException {
		_checkClosed();
		String s = Tools.toHTML(new String(cbuf).substring(off, off+len));
		_out.write(s.toCharArray(), 0, s.length());
	}

	private void _checkClosed() {
		if(_closed) {
			throw new IllegalStateException();
		}
	}
}
