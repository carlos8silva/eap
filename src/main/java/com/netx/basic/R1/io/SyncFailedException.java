package com.netx.basic.R1.io;
import java.io.IOException;

// TODO throw when corresponding IOException found
public class SyncFailedException extends ReadWriteException {

	SyncFailedException(String message, IOException cause) {
		super(message, cause);
	}

	SyncFailedException(IOException cause) {
		super(cause);
	}
}
