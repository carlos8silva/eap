package com.netx.basic.R1.io;
import com.netx.basic.R1.eh.Checker;


public class Streams {

	// TODO get the default buffer size according to the underlying platform.
	// The value below has been obtained from x86 running Windows XP.
	private static final int _DEFAULT_BUFFER_SIZE = 8192;
	
	private Streams() {
	}

	public static int getDefaultBufferSize() {
		return _DEFAULT_BUFFER_SIZE;
	}

	public static void copy(ExtendedInputStream in, ExtendedOutputStream out) throws FileLockedException, ReadWriteException {
		copy(in, out, null);
	}

	public static void copy(ExtendedInputStream in, ExtendedOutputStream out, ProgressObserver observer) throws FileLockedException, ReadWriteException {
		Checker.checkNull(in, "in");
		Checker.checkNull(out, "out");
		final int bufferSize = getDefaultBufferSize();
		byte[] buffer = new byte[bufferSize];
		int bytesRead = in.read(buffer, 0, bufferSize);
		while(bytesRead > 0) {
			out.write(buffer, 0, bytesRead);
			if(observer != null) {
				observer.increment(bytesRead);
			}
			bytesRead = in.read(buffer, 0, bufferSize);
		}
		out.flush();
	}
	
	public static void copy(ExtendedReader in, ExtendedWriter out) throws FileLockedException, ReadWriteException {
		Checker.checkNull(in, "in");
		Checker.checkNull(out, "out");
		final int bufferSize = getDefaultBufferSize();
		char[] buffer = new char[bufferSize];
		int bytesRead = in.read(buffer, 0, bufferSize);
		while(bytesRead > 0) {
			out.write(buffer, 0, bytesRead);
			bytesRead = in.read(buffer, 0, bufferSize);
		}
		out.flush();
	}
}
