package com.netx.basic.R1.io;
import java.io.IOException;


// TODO throw when corresponding IOException found
public class InterruptedIOException extends ReadWriteException {

	InterruptedIOException(String message, IOException cause) {
		super(message, cause);
	}

	InterruptedIOException(IOException cause) {
		super(cause);
	}
}
