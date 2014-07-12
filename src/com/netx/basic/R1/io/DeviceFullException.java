package com.netx.basic.R1.io;


public abstract class DeviceFullException extends ReadWriteException {

	// TODO i18n
	DeviceFullException(String message, Exception cause) {
		super(message, cause);
	}
}
