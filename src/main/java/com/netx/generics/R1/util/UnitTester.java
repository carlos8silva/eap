package com.netx.generics.R1.util;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import com.netx.generics.R1.time.Timestamp;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.io.FileSystem;
import com.netx.basic.R1.io.FileNotFoundException;
import com.netx.basic.R1.io.AccessDeniedException;
import org.testng.Assert;


public abstract class UnitTester {

	// TYPE:
	private static final String _PROPS_FILE = "util.properties";
	private static final String _PROP_TEST_LOC = "test.location";
	private static final String _testLocation;
	
	static {
		// Load test location from a properties file:
		try {
			Properties props = new Properties();
			props.load(UnitTester.class.getResourceAsStream(_PROPS_FILE));
			// Test location:
			_testLocation = props.getProperty(_PROP_TEST_LOC);
			if(Strings.isEmpty(_testLocation)) {
				throw new IntegrityException();
			}
		}
		catch(IOException io) {
			throw new IntegrityException(io);
		}
	}

	// INSTANCE:
	protected UnitTester() {
	}

	protected void fail() {
		Assert.fail("test case failed");
	}

	protected void fail(String message) {
		Assert.fail(message);
	}

	protected void fail(String msg, Throwable t) {
		Assert.fail(msg, t);
	}

	protected void assertTrue(boolean expr) {
		Assert.assertTrue(expr);
	}

	protected void assertFalse(boolean expr) {
		Assert.assertFalse(expr);
	}
	
	protected void assertEquals(Object a, Object b) {
		Assert.assertEquals(a, b);
	}

	protected void assertNotEquals(Object a, Object b) {
		if(a == null) {
			Assert.assertNotNull(b);
		}
		else {
			Assert.assertFalse(a.equals(b));
		}
	}

	protected void assertNull(Object o) {
		Assert.assertNull(o);
	}

	protected void assertNotNull(Object o) {
		Assert.assertNotNull(o);
	}
	
	protected Directory getTestResourceLocation() {
		try {
			return new FileSystem(_testLocation);
		}
		catch(AccessDeniedException ade) {
			throw new IntegrityException(ade);
		}
		catch(FileNotFoundException ade) {
			throw new IntegrityException(ade);
		}
	}

	public enum FORMAT {TIMESTAMP, DATE, TIME}

	protected void showObjectProperty(Object obj, String methodName) {
		showObjectProperty(obj, methodName, null);
	}
	
	protected void showObjectProperty(Object obj, String methodName, FORMAT format) {
		Checker.checkNull(obj, "obj");
		Checker.checkEmpty(methodName, "methodName");
		try {
			Class<?> c = obj.getClass();
			Method m = c.getMethod(methodName);
			if(m.getReturnType() == Void.class) {
				throw new IllegalArgumentException("method '"+methodName+"' has void return type");
			}
			if(format != null && m.getReturnType() != Long.class && m.getReturnType() != long.class) {
				throw new IllegalArgumentException("illegal return type '"+m.getReturnType()+"' when format is specified");
			}
			Object result = m.invoke(obj);
			print(methodName);
			print("(): ");
			if(format != null) {
				Timestamp moment = new Timestamp((Long)result);
				// Dont't format if millis==0:
				if(moment.getTimeInMilliseconds() != 0) {
					if(format == FORMAT.TIMESTAMP) {
						result = moment;
					}
					else if(format == FORMAT.DATE) {
						result = moment.getDate();
					}
					else if(format == FORMAT.TIME) {
						result = moment.getTime();
					}
				}
			}
			println(result);
		}
		catch(NoSuchMethodException nsme) {
			throw new IllegalArgumentException(methodName+"(): method does not exist");
		}
		catch(IllegalAccessException iae) {
			throw new IllegalArgumentException(methodName+"(): method is not accessible");
		}
		catch(InvocationTargetException ite) {
			throw new RuntimeException(methodName+"(): invoke threw exception: "+ite, ite);
		}
	}
	
	public Exception getException(int numIterations) {
		if(numIterations == 0) {
			return new Exception();
		}
		else {
			return getException(numIterations - 1);
		}
	}

	protected void print(Object o) {
		System.out.print(o);
	}

	protected void println(Object o) {
		System.out.println(o);
	}
	
	protected void println() {
		System.out.println();
	}

	protected void printArray(Object[] array) {
		Checker.checkNull(array, "array");
		for(int i=0; i<array.length; i++) {
			println("["+i+"]: "+array[i]);
		}
	}

	protected void printArray(boolean[] array) {
		printArray(Arrays.toObjectArray(array));
	}

	protected void printArray(char[] array) {
		printArray(Arrays.toObjectArray(array));
	}

	protected void printArray(short[] array) {
		printArray(Arrays.toObjectArray(array));
	}

	protected void printArray(int[] array) {
		printArray(Arrays.toObjectArray(array));
	}

	protected void printArray(long[] array) {
		printArray(Arrays.toObjectArray(array));
	}

	protected void printArray(float[] array) {
		printArray(Arrays.toObjectArray(array));
	}

	protected void printArray(double[] array) {
		printArray(Arrays.toObjectArray(array));
	}

	protected void printArrayInLine(Object[] array) {
		Checker.checkNull(array, "array");
		for(int i=0; i<array.length - 1; i++) {
			print(array[i]);
			print(", ");
		}
		println(array[array.length-1]);
	}

	protected void printArrayInLine(boolean[] array) {
		printArrayInLine(Arrays.toObjectArray(array));
	}

	protected void printArrayInLine(char[] array) {
		printArrayInLine(Arrays.toObjectArray(array));
	}

	protected void printArrayInLine(short[] array) {
		printArrayInLine(Arrays.toObjectArray(array));
	}

	protected void printArrayInLine(int[] array) {
		printArrayInLine(Arrays.toObjectArray(array));
	}

	protected void printArrayInLine(long[] array) {
		printArrayInLine(Arrays.toObjectArray(array));
	}

	protected void printArrayInLine(float[] array) {
		printArrayInLine(Arrays.toObjectArray(array));
	}

	protected void printArrayInLine(double[] array) {
		printArrayInLine(Arrays.toObjectArray(array));
	}

	protected void printIterator(Iterator<?> it) {
		Checker.checkNull(it, "it");
		for(int i=0; it.hasNext(); i++) {
			println("["+i+"]: "+it.next());
		}
	}

	protected void printIteratorInLine(Iterator<?> it) {
		Checker.checkNull(it, "it");
		while(it.hasNext()) {
			print(it.next());
			if(it.hasNext()) {
				print(", ");
			}
		}
		println();
	}
}
