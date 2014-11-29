package com.netx.ut.basic.R1;
import java.io.IOException;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.io.*;


public class NTInputOutput extends UnitTester {

	public static void main(String[] args) throws Throwable {
		NTInputOutput nt = new NTInputOutput();
		nt.init();
		//nt.t01_TestXlsRead();
		//nt.t02_TestXlsWrite();
		//nt.t03_TestDeleteOpenStream();
		//nt.t04_TestPrintToStdout();
		//nt.t05_TestFileLocked();
		nt.t06_TestSecondJVM();
	}

	private String _testLocation;
	
	public void init() throws FileSystemException {
		_testLocation = getTestResourceLocation().getDirectory(UTInputOutput.IO_TEST_DIR).getAbsolutePath();
	}

	public void t01_TestXlsRead() throws FileSystemException, ReadWriteException {
		FileSystem fs = new FileSystem(_testLocation);
		File xls = fs.getFile("test-excel-read.xls");
		ExtendedInputStream in = xls.getInputStream();
		// Wait to write:
		for(int i=0; i<100; i++) {
			Tools.sleep(1000, null);
			println(in.read());
		}
		in.close();
	}

	// Note: if we try to open the file with Excel while writing to it,
	// Excel can't actually open it and asks the user to open as read-only.
	public void t02_TestXlsWrite() throws FileSystemException, ReadWriteException {
		FileSystem fs = new FileSystem(_testLocation);
		File xls = fs.getFile("test-excel-write.xls");
		ExtendedOutputStream out = xls.getOutputStream(true);
		// Wait to write:
		for(int i=0; i<100; i++) {
			Tools.sleep(1000, null);
			int number = (int)Math.round(Math.random()*1000);
			println(number);
			out.write(number);
		}
		out.close();
	}
	
	// This throws an OperationFailedException:
	public void t03_TestDeleteOpenStream() throws BasicIOException {
		FileSystem fs = new FileSystem(_testLocation);
		File txt = fs.getFile("new-file.txt", true);
		txt.getInputStream();
		txt.delete();
	}
	
	public void t04_TestPrintToStdout() throws ReadWriteException {
		ExtendedWriter out = new ExtendedWriter(System.out, "System.out");
		out.write("whatever");
		out.newLine();
		out.close();
	}

	// TODO this throws an error that needs to be fixed
	public void t05_TestFileLocked() throws BasicIOException {
		FileSystem fs = new FileSystem(_testLocation);
		File locked = fs.getFile("locked.txt", true);
		ExtendedInputStream in = locked.getInputStream();
		ExtendedOutputStream out = locked.getOutputStreamAndLock();
		in.read();
		out.close();
		in.close();
	}

	// Usage: run once to block the file; run a second time to 
	public void t06_TestSecondJVM() throws FileSystemException, ReadWriteException {
		FileSystem fs = new FileSystem(_testLocation);
		File f = fs.getFile("shopping-list.csv");
		println(f.getAbsolutePath());
		ExtendedOutputStream out = f.getOutputStreamAndLock();
		try {
			System.in.read();
			System.in.read();
		}
		catch(IOException io) {
			throw new IntegrityException(io);
		}
		out.close();
	}
}
