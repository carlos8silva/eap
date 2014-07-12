package com.netx.basic.R1.logging;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.ReadWriteException;


abstract class ColumnWriter extends FileWriter {

	protected ColumnWriter(File file) {
		super(file);
	}

	// This should return the number of lines that can
	// be written to a file, or 0 if unlimited.
	public abstract int getMaxNumLines();
	// This should return the number of lines already written to a file, unless
	// getMaxNumLines returns 0. In this case, this method should return -1.
	public abstract int getTotalNumLines();
	// This method is responsible for flushing data when necessary.
	public abstract void write(String[] line) throws ReadWriteException;

	public boolean isBlank() {
		return getTotalNumLines() <= 1;
	}
}
