package com.netx.ut.lib.java;
import java.util.List;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.locks.*;
import com.netx.basic.R1.eh.IntegrityException;


public class TestResources {

	public final static String JAVA_TEST_DIR = "lib.java";

	// For NTLang:
	public enum TerminationReason {

		LOGGED_OUT("LO"),
		FORCED_OUT("FO");
		
		private final String _code;
		
		private TerminationReason(String code) {
			_code = code;
		}
		
		public String getCode() {
			return _code;
		}
	}
	
	// For NTLang:
	public static class ClassWithError {

		static {
			if(true) {
				throw new RuntimeException("oops");
			}
		}
	}

	// For NTLang:
	public static class StaticClass1 {
		
		private static String str = null;
		
		public static String getString() {
			return str;
		}
		
		public static void setString(String s) {
			str = s;
		}
	}

	// For NTLang:
	public static class StaticClass2 extends StaticClass1 {
	}

	// For NTLang:
	public static class CloneExample1 implements Cloneable {

	    private int _aNumber;
	    protected List<ModifiableString> _aList;

	    public CloneExample1(int aNumber) {
	        _aNumber = aNumber;
	        _aList = new ArrayList<ModifiableString>();
	    }

	    public void setNumber(int aNumber) {
	    	_aNumber = aNumber;
	    }

	    public List<ModifiableString> getList() {
	        return _aList;
	    }

	    public CloneExample1 clone() {
	    	try {
		        // Byte copy of this object (newObj has the same reference to _aList)
		        CloneExample1 newObj = (CloneExample1)super.clone();
		        return newObj;
	    	}
	    	catch(CloneNotSupportedException cnse) {
	    		throw new IntegrityException();
	    	}
	    }

	    public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(_aNumber+" ");
			sb.append(_aList);
			return sb.toString();
		}
	}

	// For NTLang:
	public static class CloneExample2 extends CloneExample1 {
		
		public CloneExample2(int aNumber) {
			super(aNumber);
		}
		
		public CloneExample2 clone() {
			CloneExample2 newObj = (CloneExample2)super.clone();
	        newObj._aList = new ArrayList<ModifiableString>();
	        newObj._aList.addAll(_aList);
			return newObj;
		}
	}
	
	// For NTLang:
	public static class ModifiableString {
		
		private String _s;
		
		public ModifiableString(String s) {
			_s = s;
		}
		
		public String getString() {
			return _s;
		}
		
		public void setString(String s) {
			_s = s;
		}
		
		public String toString() {
			return "str("+_s+")";
		}
	}
	
	// For NTLang:
	public static class SimpleTask extends TimerTask {
		
		private int _counter;
		
		public SimpleTask() {
			_counter = 0;
		}
		
		public void run() {
			_counter++;
			System.out.println(_counter);
		}
	}
	
	// For NTLang:
	public static class SimpleThread extends Thread {
	    public SimpleThread(String str) {
	        super(str);
	    }
	    public void run() {
	        for (int i=1; i<=10; i++) {
	            System.out.println(i + " " + getName());
	            try {
	                sleep((long)(Math.random() * 1000));
	            } catch (InterruptedException e) {}
	        }
	        System.out.println("DONE! " + getName());
	    }
	}

	// For NTLang:
	public static class ForeverThread extends Thread {
		public ForeverThread(ThreadGroup group) {
			super(group, "ForeverThread");
			setDaemon(true);
		}
		public void run() {
			while(true) {
				try {
					Thread.sleep(1000);
				}
				catch(InterruptedException ie) {
					System.err.println("caught exception while waiting: "+ie);
				}
			}
		}
	}

	// For NTUtil:
	public static class Beeper {

		private int _numCalls;
		//private final Object _beepLock = new Object();
		private final ReentrantLock _lock = new ReentrantLock();
		
		public Beeper() {
			_numCalls = 0;
		}

		// Synchronizing the method or the internal lock has the same result.
		public void beep(String name) {
			_lock.lock();
			//synchronized(_beepLock) {
				_numCalls++;
				System.out.println("I'm beeping for "+name+"! (total calls so far: "+_numCalls+")");
				// Note: cannot use wait, otherwise the lock is released:
				_sleep(5000);
				System.out.println(name+": stopped waiting");
			//}
			_lock.unlock();
		}

		// If you dont synchronize this method / lock, HONKER will call it while BEEPER is sleeping.
		// If you synchronize it, HONKER will wait until BEEPER finishes calling beep.
		// Using locks instead of synchronized has the same effect.
		// Locks are not compatible with synchronized.
		public void honk(String name) {
			_lock.lock();
			//synchronized(_beepLock) {
				_numCalls++;
				System.out.println("I'm honking for "+name+"! (total calls so far: "+_numCalls+")");
			//}
			_lock.unlock();
		}
	}
	
	// For NTUtil:
	public static class Thread1 extends Thread {

		protected final Beeper beeper;
		protected final String name;

		public Thread1(Beeper b, String name) {
			beeper = b;
			this.name = name;
		}
		
		public void run() {
			System.out.println(name+": started");
			beeper.beep(name);
			beeper.honk(name);
			System.out.println(name+": finished");
		}
	}

	// For NTUtil:
	public static class Thread2 extends Thread1 {

		public Thread2(Beeper b, String name) {
			super(b, name);
		}
		
		public void run() {
			// Force Thread1 to start first:
			_sleep(500);
			System.out.println(name+": started");
			beeper.honk(name);
			beeper.honk(name);
			beeper.beep(name);
			System.out.println(name+": finished");
		}
	}

	// Conclusion on ReadWriteLocks:
	// If you hold the read lock, the write lock cannot be obtained.
	// If you hold the read lock, the read lock can still be obtained.
	public static class RWLock1 extends Thread {
		
		public final String name;
		public final ReadWriteLock lock;
		
		public RWLock1(String name, ReadWriteLock lock) {
			this.name = name;
			this.lock = lock;
		}

		public void run() {
			lock.readLock().lock();
			System.out.println(name+" - just got the read lock and will wait");
			_sleep(3000);
			System.out.println(name+" - unlocking read lock...");
			lock.readLock().unlock();
			System.out.println(name+" - attempting write lock:");
			lock.writeLock().lock();
			System.out.println(name+" - made it, releasing now:");
			lock.writeLock().unlock();
		}
	}

	public static class RWLock2 extends RWLock1 {
		public RWLock2(String name, ReadWriteLock lock) {
			super(name, lock);
		}
		
		public void run() {
			// Force RWLock1 to start first:
			_sleep(500);
			System.out.println(name+" - attempting write lock...");
			lock.writeLock().lock();
			System.out.println(name+" - done! waiting now");
			_sleep(3000);
			lock.writeLock().unlock();
			System.out.println(name+" - unlocked");
		}
	}

	private static void _sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		}
		catch(InterruptedException ie) {
			System.err.println(ie);
		}
	}
}
