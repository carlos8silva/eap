package com.netx.ut.basic.R1;
import com.netx.generics.R1.util.UnitTester;
import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.logging.*;


public class NTLogging extends UnitTester {

	public static void main(String[] args) throws Throwable {
		NTLogging nt = new NTLogging();
		nt.init();
		//nt.t01_StandardLogger();
		nt.t02_TextLogger();
		//nt.t04_XlsLogger();
		//nt.t05_PerformanceLogger();
	}

	private Directory _testLocation;
	
	public void init() throws Exception {
		_testLocation = getTestResourceLocation().getDirectory(UTLogging.LOGGING_TEST_DIR);
	}

	public void t01_StandardLogger() {
		Logger l = new Logger(System.out);
		l.info("this is a log message");
		l = null;
		System.gc();
	}

	public void t02_TextLogger() throws Exception {
		Logger l = new Logger(new LogFile(_testLocation, "test.log"));
		l.info("this is a log message");
		Exception e = new Exception();
		l.error(e);
	}

	public void t04_XlsLogger() throws Exception {
		Logger l = new Logger(new LogFile(_testLocation, "test.csv"));
		l.info("this is a log message");
		l = null;
		System.gc();
		//l.finalize();
	}

	public void t05_PerformanceLogger() throws Exception {
		PerformanceLogger pl = new PerformanceLogger(new LogFile(_testLocation, "pl_<date>.xls", 1));
		PerformanceMetric pm = new PerformanceMetric("t03_PerformanceLogger");
		pm.start();
		Process p = Runtime.getRuntime().exec("freecell");
		p.waitFor();
		pm.end();
		pl.log(pm);
	}
}
