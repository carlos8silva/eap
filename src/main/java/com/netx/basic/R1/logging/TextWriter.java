package com.netx.basic.R1.logging;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.ExtendedWriter;
import com.netx.basic.R1.io.FileNotFoundException;
import com.netx.basic.R1.io.AccessDeniedException;
import com.netx.basic.R1.io.ReadWriteException;


class TextWriter extends FileWriter {

	private final ExtendedWriter _out;
	
	public TextWriter(ExtendedWriter out) {
		super(null);
		_out = out;
	}

	public TextWriter(File file) throws FileNotFoundException, AccessDeniedException, ReadWriteException {
		super(file);
		_out = new ExtendedWriter(file.getOutputStreamAndLock(true));
	}

	public ExtendedWriter getWriter() {
		return _out;
	}

	public void close() throws ReadWriteException {
		// No need to call flush, the underlying
		// stream does that for us:
		_out.close();
		deleteIfEmpty();
	}
	
	public boolean isBlank() throws FileNotFoundException {
		return getFile().isBlank();
	}
}
