package com.netx.basic.R1.io;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.eh.Checker;


public class ExtendedInputStream extends InputStream {

	private final String _path;
	private final BufferedInputStream _in;
	private boolean _isOpen;

	public ExtendedInputStream(InputStream in, String path) {
		Checker.checkNull(in, "in");
		Checker.checkEmpty(path, "path");
		_path = path;
		_in = new BufferedInputStream(in, Streams.getDefaultBufferSize());
		_isOpen = true;
	}

	// For ProtocolImplFile:
	ExtendedInputStream(File file) throws FileLockedException {
		Checker.checkNull(file, "file");
		_path = file.getAbsolutePath();
		// Existence and permissions are checked in ProtocolImpl.
		// Check whether this file has been locked:
		if(Locks.isLocked(_path)) {
			// Opening a FileInputStream does not throw an exception if
			// the file has bee locked by another thread, so we need
			// to use the Locks class to ensure this. However, this
			// does not detect a lock that has been open by another
			// JVM, so in this case getting the input stream will go
			// through (and it will fail on any read() invocation).
			throw new FileLockedException(_path, null);
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
		}
		catch(FileNotFoundException fnfe) {
			// Note: no need to attempt closing the stream; if we get an exception
			// during initialisation, we never get a handle to the stream.
			// Translate the exception:
			throw Translator.translateFNFE(fnfe, _path);
		}
		_in = new BufferedInputStream(in, Streams.getDefaultBufferSize());
		_isOpen = true;
	}

	public String getPath() {
		return _path;
	}

	public int available() throws ReadWriteException {
		_checkState();
		try {
			return _in.available();
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _path);
		}
	}

	public void close() throws ReadWriteException {
		if(_isOpen) {
			try {
				_isOpen = false;
				_in.close();
			}
			catch(IOException io) {
				throw Translator.translateIOE(io, _path);
			}
		}
	}

	public void mark(int readLimit) {
		Checker.checkMinValue(readLimit, 1, "readLimit");
		if(!markSupported()) {
			throw new UnsupportedOperationException("mark is not supported");
		}
		_checkState();
		_in.mark(readLimit);
	}

	public boolean markSupported() {
		_checkState();
		return _in.markSupported();
	}

	public int read() throws FileLockedException, ReadWriteException {
		_checkState();
		try {
			return _in.read();
		}
		catch(IOException io) {
			_checkFLE(io);
			throw Translator.translateIOE(io, _path);
		}
	}

	public int read(byte[] b) throws FileLockedException, ReadWriteException {
		_checkState();
		Checker.checkNull(b, "b");
		try {
			return _in.read(b);
		}
		catch(IOException io) {
			_checkFLE(io);
			throw Translator.translateIOE(io, _path);
		}
	}

	public int read(byte[] b, int off, int len) throws FileLockedException, ReadWriteException {
		_checkState();
		Checker.checkNull(b, "b");
		try {
			return _in.read(b, off, len);
		}
		catch(IOException io) {
			_checkFLE(io);
			throw Translator.translateIOE(io, _path);
		}
	}

	public void reset() {
		_checkState();
		if(!markSupported()) {
			throw new UnsupportedOperationException("mark is not supported");
		}
		try {
			_in.reset();
		}
		catch(IOException io) {
			Translator.throwResetException(io, _path);
		}
	}

	public long skip(long n) throws FileLockedException, ReadWriteException {
		_checkState();
		try {
			return _in.skip(n);
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
		FileLockedException fle = Translator.translateFLE(io, _path);
		if(fle != null) {
			throw fle;
		}
	}
}
