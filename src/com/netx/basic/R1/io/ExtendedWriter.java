package com.netx.basic.R1.io;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.eh.Checker;


public class ExtendedWriter extends BufferedWriter {

	private final String _path;
	private final boolean _std;
	private boolean _isOpen;

	public ExtendedWriter(OutputStream out, String path) {
		super(new OutputStreamWriter((OutputStream)Checker.checkNull(out, "out")), Streams.getDefaultBufferSize());
		_path = path;
		if(out == System.out || out == System.err) {
			_std = true;
		}
		else {
			_std = false;
		}
		_isOpen = true;
	}
	
	public ExtendedWriter(Writer out, String path) {
		super((Writer)Checker.checkNull(out, "out"), Streams.getDefaultBufferSize());
		_path = path;
		_std = false;
		_isOpen = true;
	}

	public ExtendedWriter(File file, boolean append) throws FileNotFoundException, AccessDeniedException {
		this(((File)Checker.checkNull(file, "file")).getOutputStream(append), file.getAbsolutePath());
	}

	public ExtendedWriter(File file) throws FileNotFoundException, AccessDeniedException {
		this(file, false);
	}
	
	public ExtendedWriter(ExtendedOutputStream out) {
		super(new OutputStreamWriter((OutputStream)Checker.checkNull(out, "out")), Streams.getDefaultBufferSize());
		_path = out.getPath();
		_std = false;
		_isOpen = true;
	}

	public String getPath() {
		return _path;
	}

	public boolean isStandardStream() {
		return _std;
	}

	public Writer append(char c) throws ReadWriteException {
		_checkState();
		try {
			return super.append(c);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public Writer append(CharSequence cs) throws ReadWriteException {
		_checkState();
		try {
			return super.append(cs);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public Writer append(CharSequence cs, int start, int end) throws ReadWriteException {
		_checkState();
		try {
			return super.append(cs, start, end);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void close() throws ReadWriteException {
		if(_isOpen) {
			try {
				_isOpen = false;
				super.flush();
				if(!_std) {
					// We do not close the standard 
					// output and error streams:
					super.close();
				}
			}
			catch(IOException io) {
				throw Translator.translateIOE(io, _path);
			}
		}
	}

	public void flush() throws ReadWriteException {
		_checkState();
		try {
			super.flush();
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void newLine() throws ReadWriteException {
		_checkState();
		try {
			super.newLine();
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void write(char[] cbuf, int off, int len) throws ReadWriteException {
		_checkState();
		try {
			super.write(cbuf, off, len);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void write(char[] cbuf) throws ReadWriteException {
		_checkState();
		try {
			super.write(cbuf);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void write(char c) throws ReadWriteException {
		_checkState();
		try {
			super.write(c);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void write(String s, int off, int len) throws ReadWriteException {
		_checkState();
		try {
			super.write(s, off, len);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void write(String s) throws ReadWriteException {
		_checkState();
		try {
			super.write(s);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void finalize() throws Throwable {
		try {
			close();
		}
		catch(ReadWriteException ioe) {
			Tools.handleCriticalError(ioe);
		}
	}

	private void _checkState() {
		if(!_isOpen) {
			throw new IllegalStateException("this stream has been closed");
		}
	}
}
