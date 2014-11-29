package com.netx.basic.R1.io;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.CharBuffer;
import com.netx.basic.R1.eh.Checker;
import com.netx.generics.R1.util.Tools;


public class ExtendedReader extends BufferedReader {

	private final String _path;
	private boolean _isOpen;
	
	public ExtendedReader(InputStream in, String path) {
		super(new InputStreamReader((InputStream)Checker.checkNull(in, "in")), Streams.getDefaultBufferSize());
		Checker.checkEmpty(path, "path");
		_path = path;
		_isOpen = true;
	}

	public ExtendedReader(Reader in, String path) {
		super((Reader)Checker.checkNull(in, "in"), Streams.getDefaultBufferSize());
		Checker.checkEmpty(path, "path");
		_path = path;
		_isOpen = true;
	}

	public ExtendedReader(File file) throws FileNotFoundException, FileLockedException {
		this(((File)Checker.checkNull(file, "file")).getInputStream(), file.getAbsolutePath());
	}

	public ExtendedReader(ExtendedInputStream in) {
		super(new InputStreamReader((InputStream)Checker.checkNull(in, "in")), Streams.getDefaultBufferSize());
		_path = in.getPath();
		_isOpen = true;
	}

	public String getPath() {
		return _path;
	}

	public void close() throws ReadWriteException {
		if(_isOpen) {
			try {
				_isOpen = false;
				super.close();
			}
			catch(IOException io) {
				throw Translator.translateIOE(io, _path);
			}
		}
	}

	public void mark(int readAheadLimit) throws ReadWriteException {
		_checkState();
		try {
			super.mark(readAheadLimit);
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}
	
	// No need to override markSupported():boolean.
	
	public int read() throws FileLockedException, ReadWriteException {
		_checkState();
		try {
			return super.read();
		}
		catch(IOException io) {
			_checkFLE(io);
			throw Translator.translateIOE(io, _path);
		}
	}

	public int read(char[] cbuf, int off, int len) throws FileLockedException, ReadWriteException {
		_checkState();
		try {
			return super.read(cbuf, off, len);
		}
		catch(IOException io) {
			_checkFLE(io);
			throw Translator.translateIOE(io, _path);
		}
	}

	public int read(char[] cbuf) throws FileLockedException, ReadWriteException {
		_checkState();
		try {
			return super.read(cbuf);
		}
		catch(IOException io) {
			_checkFLE(io);
			throw Translator.translateIOE(io, _path);
		}
	}
	
	public int read(CharBuffer target) throws FileLockedException, ReadWriteException {
		_checkState();
		try {
			return super.read(target);
		}
		catch(IOException io) {
			_checkFLE(io);
			throw Translator.translateIOE(io, _path);
		}
	}

	public String readLine() throws FileLockedException, ReadWriteException {
		_checkState();
		try {
			return super.readLine();
		}
		catch(IOException io) {
			_checkFLE(io);
			throw Translator.translateIOE(io, _path);
		}
	}

	public boolean ready() throws ReadWriteException {
		_checkState();
		try {
			return super.ready();
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void reset() {
		_checkState();
		try {
			super.reset();
		}
		catch(IOException io) {
			Translator.throwResetException(io, _path);
		}
	}

	public long skip(long n) throws FileLockedException, ReadWriteException {
		_checkState();
		try {
			return super.skip(n);
		}
		catch(IOException io) {
			_checkFLE(io);
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
	
	private void _checkFLE(IOException io) throws FileLockedException {
		if(io instanceof FileLockedException) {
			throw (FileLockedException)io;
		}
	}
}
