package com.netx.ut.lib.java;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Timer;
import com.netx.generics.R1.util.UnitTester;
import com.netx.ut.lib.java.TestResources.CloneExample1;
import com.netx.ut.lib.java.TestResources.CloneExample2;
import com.netx.ut.lib.java.TestResources.ModifiableString;
import com.netx.ut.lib.java.TestResources.TerminationReason;
import com.netx.ut.lib.java.TestResources.ForeverThread;
import com.netx.ut.lib.java.TestResources.SimpleThread;
import com.netx.ut.lib.java.TestResources.SimpleTask;


public class NTLang extends UnitTester {

	public static void main(String[] args) throws Throwable {
		NTLang nt = new NTLang();
		//nt.showArgs(args);
		//nt.testCharacter();
		//nt.testStringReplace();
		//nt.printLogInfo();
		//nt.testStaticClasses();
		nt.loadClassResource();
		nt.loadClassLoaderResource();
		//nt.testNullCast();
		//nt.showEnvVariables();
		//nt.errorLoadingClass();
		//nt.testEnums();
		//nt.threads_testTimerThread();
		nt.println("done.");
	}
	
	public void showArgs(String[] args) {
		for(int i=0; i<args.length; i++) {
			println("'"+args[i]+"'");
		}
	}

	public void testCharacter() {
		println("isLetter('ç'): "+Character.isLetter('ç'));
	}

	public void testStringReplace() {
		println("a b c d e f".replaceAll(" ", ""));
	}

	// Demonstrates how static variables are global
	public void testStaticClasses() {
		TestResources.StaticClass1.setString("Hello!");
		println(TestResources.StaticClass1.getString());
		TestResources.StaticClass2.setString("Goodbye!");
		println(TestResources.StaticClass1.getString());
	}

	public void printLogInfo() {
		_printLogInfo("this is a log message");
	}
	
	private void _printLogInfo(String message) {
		Exception e = new Exception();
		StackTraceElement[] elems = e.getStackTrace();
		println("message: "+message);
		println("place: "+elems[1].toString());
	}

	// Note: getClassLoader does not retrieve the file, but getClass().getResource does
	public void loadClassResource() {
		println(getClass().getClassLoader().getResource("file-in-classpath"));
	}

	public void loadClassLoaderResource() throws IOException {
		String resource = "file-in-classpath";
		InputStream in = getClass().getResourceAsStream(resource);
		if(in == null) {
			println("Resource "+resource+" not found.");
		}
		else {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while(true) {
				String s = reader.readLine();
				if(s == null) {
					break;
				}
				else {
					println(s);
				}
			}
		}
	}
	
	public void ex_showStackTrace() {
		Exception e = getException(10);
		e.printStackTrace();
	}

	public void ex_showFinally() throws Exception {
		try {
			throw new RuntimeException("blow up!");
		}
		catch(Exception e) {
			println(e);
			throw e;
		}
		finally {
			println("finally blocked reached");
		}
	}

	public void ex_showNormalExceptionFormat() {
		println("Normal Exception:");
		try {
			throw new RuntimeException("I am a runtime exception!");
		}
		catch(Throwable t) {
			_printException(t);
		}
	}

	@SuppressWarnings("null")
	public void ex_showNullPointerExceptionFormat() {
		println("Null pointer expection:");
		try {
			String s = null;
			s.toString();
		}
		catch(Throwable t) {
			_printException(t);
		}
	}

	@SuppressWarnings("null")
	public void ex_showExceptionWithRootCause() {
		println("Exception with root cause:");
		try {
			try {
				String s = null;
				s.toString();
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		catch(Throwable t) {
			_printException(t);
		}
	}
	
	private void _printException(Throwable t) {
		println("getMessage():");
		println(t.getMessage());
		println("");
		println("getClass().getName()+\": \"+getMessage():");
		println(t.getClass().getName()+": "+t.getMessage());
		println("");
		println("direct exception:");
		println(t);
		println("");
		println("stack trace:");
		t.printStackTrace(System.out);
		println("");
	}
	
	public void showNumberSizes() {
		println("Short.MAX_VALUE: "+Short.MAX_VALUE);
		println("Short.MIN_VALUE: "+Short.MIN_VALUE);
		println("Integer.MAX_VALUE: "+Integer.MAX_VALUE);
		println("Integer.MIN_VALUE: "+Integer.MIN_VALUE);
		println("Long.MAX_VALUE: "+Long.MAX_VALUE);
		println("Long.MIN_VALUE: "+Long.MIN_VALUE);
		println("Float.MAX_VALUE: "+Float.MAX_VALUE);
		println("Float.MIN_VALUE: "+Float.MIN_VALUE);
		println("Double.MAX_VALUE: "+Double.MAX_VALUE);
		println("Double.MIN_VALUE: "+Double.MIN_VALUE);
	}

	public void testClone() {
		// 1) Clone that doesn't work:
		CloneExample1 ce1 = new CloneExample1(100);
		ce1.getList().add(new ModifiableString("String one"));
		println("ce1: " + ce1);
		CloneExample1 ce2 = ce1.clone();
		ce2.setNumber(200);
		ce2.getList().add(new ModifiableString("String two"));
		// This will print the same list for both objects:
		println("ce1: " + ce1);
		println("ce2: " + ce2);
		
		// 2) Now, with an improved clone method:
		CloneExample2 ce3 = new CloneExample2(300);
		ce3.getList().add(new ModifiableString("String three"));
		println("ce3: " + ce3);
		CloneExample2 ce4 = ce3.clone();
		ce4.setNumber(400);
		ce4.getList().add(new ModifiableString("String four"));
		// Different lists:
		println("ce3: " + ce3);
		println("ce4: " + ce4);
		
		// However, if we change an internal object of the list,
		// both containers will be affected:
		ce3.getList().get(0).setString("i've changed");
		println("ce3: " + ce3);
		println("ce4: " + ce4);
	}
	
	public void instantiateArray() {
		Object array = Array.newInstance(String.class, 10);
		Array.set(array, 0, 100);
	}
	
	public void testStringHashCode() {
		String s1 = "ABC";
		String s2 = "ABC";
		String s3 = "AAA";
		println("Hashcode of String '"+s1+"': "+s1.hashCode());
		println("Hashcode of String '"+s2+"': "+s2.hashCode());
		println("Hashcode of String '"+s3+"': "+s3.hashCode());
	}
	
	public void testNullCast() {
		String a = "abc";
		String b = null;
		Object c = (String)b;
		println((a+c).toString());
	}

	public void showSystemProperties() {
		Properties props = System.getProperties();
		Enumeration<?> names = props.propertyNames();
		List<String> list = new ArrayList<String>();
		while(names.hasMoreElements()) {
			String name = (String)names.nextElement();
			String value = props.getProperty(name);
			list.add(name+"="+value);
		}
		Collections.sort(list);
		for(String s : list) {
			println(s);
		}
	}

	public void showEnvVariables() {
		println(System.getenv("PATH"));
		println(System.getenv("EAP_CUBIGRAF_HOME"));
	}
	
	public void errorLoadingClass() throws Throwable {
		Class.forName("com.netx.ut.lib.java.TestResources$ClassWithError");
	}
	
	public void testNumberSizeExceededException() {
		String bigNumber = "717987349012222142324576234868726372875971245781578682677458283502384717987349012222142324576234868726372875971245781578682677458283502384717987349012222142324576234868726372875971245781578682677458283502384762348687263728759712457815786826774582835023847623486872637287597124578157868267745828350238476234868726372875971245781578682677458283502384";
		Double d = new Double(bigNumber);
		println(d);
		println(Double.MAX_VALUE);
		println(d > Double.MAX_VALUE);
	}
	
	public void testEnums() {
		for(TerminationReason r : TerminationReason.values()) {
			println(r+" = '"+r.getCode()+"'");
		}
		try {
			println("get value 'LOGGED_OUT': "+Enum.valueOf(TerminationReason.class, "LOGGED_OUT"));
			println("get value 'LO': "+Enum.valueOf(TerminationReason.class, "LO"));
		}
		catch(Exception e) {
			println(e);
		}
	}

	public void threads_testTimerThread() {
		Timer timer = new Timer();
		timer.schedule(new SimpleTask(), 3*1000);
		System.out.println("Task scheduled to start within 3 seconds.");
		// the timer thread keeps running until we kill it:
		try {
			Thread.sleep(5*1000);
			System.out.println("cancelling timer thread...");
			timer.cancel();
		}
		catch(InterruptedException ie) {
			System.out.println(ie);
		}
	}

	public void threads_testTwoThreads() {
		new SimpleThread("Jamaica").start();
		new SimpleThread("Fiji").start();
	}
	
	public void threads_testThreadGroup() {
		ThreadGroup group = new ThreadGroup("TaskManager");
		new ForeverThread(group).start();
		new ForeverThread(group).start();
		new ForeverThread(group).start();
		System.out.println("Self active threads:\t"+group.activeCount());
		System.out.println("Parent active threads:\t"+group.getParent().activeCount());
	}
}
