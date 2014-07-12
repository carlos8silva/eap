package com.netx.basic.R1.io;
import java.util.Set;
import java.util.HashSet;
import com.netx.basic.R1.eh.IntegrityException;


class Locks {

	private final static Set<String> _locks;

	static {
		_locks = new HashSet<String>();
	}
	
	public static boolean isLocked(String path) {
		return _locks.contains(path);
	}
	
	public static void lock(String path) {
		if(!_locks.add(path)) {
			throw new IntegrityException(path);
		}
	}

	public static void release(String path) {
		if(!_locks.remove(path)) {
			throw new IntegrityException(path);
		}
	}
}
