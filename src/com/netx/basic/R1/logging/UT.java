package com.netx.basic.R1.logging;
import com.netx.basic.R1.io.FileSystemException;


public class UT {

	public static String getNextLogFile(LogFile logFile) throws FileSystemException {
		return logFile.getNextLogFile();
	}
}
