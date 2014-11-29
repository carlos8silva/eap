package com.netx.basic.R1.io;


public abstract class DeviceUnavailableException extends ReadWriteException {

	// TODO i18n
	DeviceUnavailableException(String message, Exception cause) {
		super(message, cause);
	}
}
