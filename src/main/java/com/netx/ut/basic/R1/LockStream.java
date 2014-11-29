package com.netx.ut.basic.R1;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.io.*;


public class LockStream extends UnitTester {

	// TYPE:
	public static void main(String[] args) throws FileSystemException, ReadWriteException {
		LockStream program = new LockStream();
		Directory dir = program.getTestResourceLocation().getDirectory(UTInputOutput.IO_TEST_DIR);
		File f = dir.getFile("shopping-list.csv");
		ExtendedOutputStream out = f.getOutputStreamAndLock();
		Tools.sleep(10000);
		out.close();
	}
	
	// INSTANCE:
	
}
