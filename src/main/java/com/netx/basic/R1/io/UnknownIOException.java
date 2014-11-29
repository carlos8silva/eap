package com.netx.basic.R1.io;
import java.io.IOException;


public class UnknownIOException extends ReadWriteException {

	UnknownIOException(IOException cause, String streamName) {
		// TODO use the stream name in the description
		super(cause);
	}
}
