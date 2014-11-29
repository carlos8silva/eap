package com.netx.ut.basic.R1;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.time.Date;
import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.ExtendedInputStream;
import com.netx.basic.R1.io.ExtendedReader;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.basic.R1.io.FileSystemException;
import com.netx.basic.R1.io.ReadWriteException;
import com.netx.basic.R1.io.FileLockedException;
import com.netx.basic.R1.logging.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;


public class UTLogging extends UnitTester {

	// TYPE:
	public static final String LOGGING_TEST_DIR = "basic.R1.logging";
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	public static final Date today = new Date();
	
	// INSTANCE:
	private Directory _testLocation;

	@BeforeClass
	public void setUp() throws FileSystemException {
		_testLocation = getTestResourceLocation().getDirectory(UTLogging.LOGGING_TEST_DIR);
		_testLocation.deleteContents();
	}

	@AfterClass
	public void exit() {}
	
	@Test
	// Check the next log file name when <date> is not specified:
	public void testLogFileWithoutDate() throws BasicIOException {
		// 1) First getNextLogFile() simple returns the file name:
		String logFileName = "log_t01.xls";
		LogFile logFile = new LogFile(_testLocation, logFileName, 0);
		assertEquals(UT.getNextLogFile(logFile), logFileName);
		
		// 2) getNextLogFile() returns sequencial file names:
		for(int i=1; i<99; i++) {
			UT.getNextLogFile(logFile);
		}
		assertEquals(UT.getNextLogFile(logFile), "log_t01_100.xls");
		assertEquals(UT.getNextLogFile(logFile), "log_t01_101.xls");
		
		// 3) If there is an existing log_t01_xx.xls file, getNextLogFile() returns it:
		logFileName = "log_t01_67.xls";
		_testLocation.createFile(logFileName);
		logFile = new LogFile(_testLocation, logFileName, 0);
		assertEquals(UT.getNextLogFile(logFile), logFileName);
		
		// 4) Check for a different extension:
		logFileName = "another_file_01.xls";
		String txtFileName = "another_file.txt";
		_testLocation.createFile(logFileName);
		logFile = new LogFile(_testLocation, txtFileName, 0);
		assertEquals(UT.getNextLogFile(logFile), txtFileName);
		assertEquals(UT.getNextLogFile(logFile), "another_file_02.txt");
	}
	
	@Test
	// Check the next log file name when <date> is specified:
	public void testLogFileWithDate() throws BasicIOException {
		// 1) When the directory is empty, the log file name has today's date:
		String logFileName = "log_t02_<date>.xls";
		String realFileName = "log_t02_"+today.format(dateFormat)+".xls";
		LogFile logFile = new LogFile(_testLocation, logFileName, 1);
		assertEquals(UT.getNextLogFile(logFile), realFileName);

		// 2) getNextLogFile() returns sequencial file names:
		for(int i=1; i<99; i++) {
			UT.getNextLogFile(logFile);
		}
		realFileName = "log_t02_"+today.format(dateFormat)+"_100.xls";
		assertEquals(UT.getNextLogFile(logFile), realFileName);
		realFileName = "log_t02_"+today.format(dateFormat)+"_101.xls";
		assertEquals(UT.getNextLogFile(logFile), realFileName);
		
		// 3) If a file exists within the range of days, the existing file is used:
		today.setLenient(true);
		Date past = today.setDay(today.getDay()-15);
		String pastFileName = "log_t02_"+past.format(dateFormat)+".xls";
		File file = _testLocation.createFile(pastFileName);
		logFile = new LogFile(_testLocation, logFileName, 15);
		assertEquals(UT.getNextLogFile(logFile), pastFileName);

		// 4) If a file exists but is already outside the range of days, a new file is created:
		file.delete();
		past = today.setDay(today.getDay()-16);
		logFile = new LogFile(_testLocation, logFileName, 15);
		realFileName = "log_t02_"+today.format(dateFormat)+".xls";
		assertEquals(UT.getNextLogFile(logFile), realFileName);
		
		// 5) If there is an existing log_t01_xx.xls file, getNextLogFile() returns it:
		realFileName = "log_t02_"+today.format(dateFormat)+"_49.xls";
		_testLocation.createFile(realFileName);
		logFile = new LogFile(_testLocation, logFileName, 15);
		assertEquals(UT.getNextLogFile(logFile), realFileName);
	}

	@Test
	// TODO can we add assertions here?
	public void t03_TestStandardLogger() throws FileSystemException, ReadWriteException {
		Logger logger = new Logger(System.out);
		logger.info("this is a trace message");
		logger.warn("this is a warning");
		logger.warn("this is a warning", getException(1));
		logger.error("this is an error", getException(1));
		logger.error(getException(1));
	}

	@Test
	public void t04_TestTxtLogger() throws BasicIOException {
		final String filename1 = "test01.log";
		final String filename2 = "test01.txt";
		final LogFile logFile = new LogFile(_testLocation, filename1);
		Logger logger = new Logger(logFile);
		// The file should be created immediately:
		File file = _testLocation.getFile(filename1);
		if(file == null) {
			fail();
		}
		// It should be locked:
		try {
			file.getOutputStream();
			fail();
		}
		catch(FileLockedException fle) {
			println(fle);
		}
		// The .txt file should NOT have been created:
		if(_testLocation.exists(filename2)) {
			fail();
		}
		// Change output, should delete the file:
		logger.setOutput(System.out);
		if(_testLocation.exists(filename1)) {
			fail();
		}
		// Again open the same file:
		logger.setOutput(logFile);
		assertEquals(file.getSize(), 0L);
		logger.info("first message");
		long size = file.getSize();
		assertTrue(size > 0);
		// Change to stdout and check the file is still there:
		logger.setOutput(System.out);
		if(!_testLocation.exists(filename1)) {
			fail();
		}
		// We can now open it:
		ExtendedReader in = new ExtendedReader(file.getInputStream());
		assertTrue(in.readLine().startsWith("INFO"));
		in.close();
		// Ensure that if we disable the logger it does not write to the file:
		logger.setOutput(logFile);
		logger.setEnabled(false);
		logger.info("second message");
		logger.setOutput(System.out);
		assertEquals(file.getSize(), size);
		file.delete();
		// Print an exception to stdout:
		logger.setEnabled(true);
		logger.error("Oops!", new Exception());
		// Print an exception to TXT:
		logger.setOutput(logFile);
		logger.error("Oops!", new Exception());
		logger.setOutput(System.out);
		file.delete();
	}

	@Test
	public void t05_TestCsvLogger() throws BasicIOException {
		final String filename1 = "test01.csv";
		final String filename2 = "test01.txt";
		final LogFile logFile = new LogFile(_testLocation, filename1);
		Logger logger = new Logger(logFile);
		// The file should be created immediately:
		File file = _testLocation.getFile(filename1);
		if(file == null) {
			fail();
		}
		// It should be locked:
		try {
			file.getOutputStream();
			fail();
		}
		catch(FileLockedException fle) {
			println(fle);
		}
		// Get the initial file size (it has the header):
		long initialSize = file.getSize();
		// The .txt file should also have been created:
		if(!_testLocation.exists(filename2)) {
			fail();
		}
		// Change output, should delete both files:
		logger.setOutput(System.out);
		if(_testLocation.exists(filename1)) {
			fail();
		}
		if(_testLocation.exists(filename2)) {
			fail();
		}
		// Again open the same file:
		logger.setOutput(logFile);
		assertEquals(file.getSize(), initialSize);
		logger.info("first message");
		long size = file.getSize();
		assertTrue(size > initialSize);
		// Change to stdout and check the file is still there:
		logger.setOutput(System.out);
		if(!_testLocation.exists(filename1)) {
			fail();
		}
		// ... and the TXT file is not there:
		if(_testLocation.exists(filename2)) {
			fail();
		}
		// We can now open it:
		ExtendedReader in = new ExtendedReader(file.getInputStream());
		assertTrue(in.readLine().startsWith("Timestamp"));
		String s = in.readLine();
		s = s.substring(s.indexOf(",")+1);
		assertTrue(s.startsWith("INFO"));
		assertTrue(s.endsWith(","));
		in.close();
		// Ensure that if we disable the logger it does not write to the file:
		logger.setOutput(new LogFile(_testLocation, filename1));
		logger.setEnabled(false);
		logger.info("second message");
		logger.setOutput(System.out);
		assertEquals(file.getSize(), size);
		
		// Write an error and ensure the ex file remains there:
		logger.setOutput(logFile);
		logger.setEnabled(true);
		logger.error("an error", getException(5));
		logger.setOutput(System.out);
		if(!_testLocation.exists(filename1)) {
			fail();
		}
		if(!_testLocation.exists(filename2)) {
			fail();
		}
		_testLocation.getFile(filename1).delete();
		_testLocation.getFile(filename2).delete();
	}

	@Test
	public void t06_TestXlsLogger() throws BasicIOException {
		final String filename1 = "test01.xls";
		final String filename2 = "test01.txt";
		final LogFile logFile = new LogFile(_testLocation, filename1);
		Logger logger = new Logger(logFile);
		// The file should be created immediately:
		File file = _testLocation.getFile(filename1);
		if(file == null) {
			fail();
		}
		// It should be locked:
		try {
			file.getOutputStream();
			fail();
		}
		catch(FileLockedException fle) {
			println(fle);
		}
		// Get the initial file size (it has the header):
		long initialSize = file.getSize();
		// The .txt file should also have been created:
		if(!_testLocation.exists(filename2)) {
			fail();
		}
		// Change output, should delete both files:
		logger.setOutput(System.out);
		if(_testLocation.exists(filename1)) {
			fail();
		}
		if(_testLocation.exists(filename2)) {
			fail();
		}
		// Again open the same file:
		logger.setOutput(logFile);
		assertEquals(file.getSize(), initialSize);
		logger.info("first message");
		// On XLS, this doesn't write to disk straight away:
		assertEquals(file.getSize(), initialSize);
		// So lets force it to write:
		LogFile.setXlsBufferSize(10);
		for(int i=0; i<9; i++) {
			logger.info("message "+(i+2));
		}
		assertEquals(file.getSize(), initialSize);
		logger.info("11th message");
		long size = file.getSize();
		assertTrue(size > initialSize);
		// Change to stdout and check the file is still there:
		logger.setOutput(System.out);
		if(!_testLocation.exists(filename1)) {
			fail();
		}
		// ... and the TXT file is not there:
		if(_testLocation.exists(filename2)) {
			fail();
		}
		// We can now open it:
		ExtendedInputStream in = file.getInputStream();
		in.close();
		// Ensure that if we disable the logger it does not write to the file:
		logger.setOutput(new LogFile(_testLocation, filename1));
		logger.setEnabled(false);
		logger.info("12th message");
		// We can only check the file's size is the same once we close the XLS logger:
		logger.setOutput(System.out);
		assertTrue(file.getSize() == size);

		// Write an error and ensure the ex file remains there:
		logger.setOutput(logFile);
		logger.setEnabled(true);
		logger.error("an error", getException(5));
		logger.setOutput(System.out);
		if(!_testLocation.exists(filename1)) {
			fail();
		}
		if(!_testLocation.exists(filename2)) {
			fail();
		}
		_testLocation.getFile(filename1).delete();
		_testLocation.getFile(filename2).delete();
	}
	
	// TODO exceed lines on CSV / XLS to force new file
}
