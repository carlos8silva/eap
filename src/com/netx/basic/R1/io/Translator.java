package com.netx.basic.R1.io;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.Machine;
import com.netx.basic.R1.shared.Globals;


public class Translator {

	private static final String _MSG_RESET_MARK_NOT_CALLED = "Resetting to invalid mark";
	
	// Translates IOException to the equivalent ReadWriteException:
	public static ReadWriteException translateIOE(IOException io, String streamName) {
		Checker.checkNull(io, "io");
		if(io instanceof ReadWriteException) {
			return (ReadWriteException)io;
		}
		// Not supposed to happen:
		if(io instanceof java.nio.channels.ClosedChannelException) {
			throw new IntegrityException(io);
		}
		Globals.getLogger().warn("unknown IO exception found - is the system locale set correctly? (currently "+Globals.getSystemLocale()+")");
		// TODO all other exceptions
		return new UnknownIOException(io, streamName);
	}

	// Handles FileNotFoundExceptions when attempting to create input / output streams:
	static FileLockedException translateFNFE(FileNotFoundException io, String path) {
		final String message = io.getMessage();
		if(Machine.runningOnWindows()) {
			if(message.contains(OSMsg.cs.getContent(OSMsg.IO_WINDOWS_ACCESS_DENIED))) {
				// Access denied should be managed one level above,
				// so we should never get this exception:
				throw new IntegrityException(io);
			}
			if(message.contains(OSMsg.cs.getContent(OSMsg.IO_WINDOWS_FILE_LOCKED_1)) || message.contains(OSMsg.cs.getContent(OSMsg.IO_WINDOWS_FILE_LOCKED_2))) {
				return new FileLockedException(path, io);
			}
			// If we get to this point, its either because the system locale does not match the OS language,
			// or because the error messages in the OSMessages L10n store are not correct:
			throw new IntegrityException("could not find appropriate error message for file locked exception, is the system locale set correctly? (currently "+Globals.getSystemLocale()+")", io);
		}
		else {
			// TODO support UNIX
			// TODO do NOT fail when running in UNIX. Only impact should be that we do not detect FileLockedException
			throw new IntegrityException();
		}
	}

	// Handles the specific case when an input stream throws an IOException
	// because the file has been locked for writing by a different JVM:
	static FileLockedException translateFLE(IOException io, String path) {
		final String message = io.getMessage();
		if(Machine.runningOnWindows()) {
			if(message.contains(OSMsg.cs.getContent(OSMsg.IO_WINDOWS_FILE_LOCKED_3))) {
				return new FileLockedException(path, io);
			}
		}
		return null;
	}
	
	// Handles exceptions thrown by InputStream.reset().
	static void throwResetException(IOException io, String path) {
		final String message = io.getMessage();
		if(message.equals(_MSG_RESET_MARK_NOT_CALLED)) {
			throw new IllegalStateException("mark() has not been called");
		}
		throw new IntegrityException(io);
	}
}
