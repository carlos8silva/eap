package com.netx.generics.R1.util;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;


public class Version implements Comparable<Version> {

	private final int[] _versionNumbers;

	// TODO constructor for String version.
	
	public Version(int ... versionNumbers) {
		Checker.checkNull(versionNumbers, "versionNumbers");
		Checker.checkEmpty(Arrays.toObjectArray(versionNumbers), "versionNumbers");
		for(int i=0; i<versionNumbers.length; i++) {
			Checker.checkMinValue(versionNumbers[i], 0, "versionNumbers["+i+"]");
		}
		_versionNumbers = versionNumbers;
	}

	public int[] getVersionNumbers() {
		return _versionNumbers;
	}

	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null) {
			return false;
		}
		if(o instanceof Version) {
			return compareTo((Version)o) == 0;
		}
		else {
			return false;
		}
	}

	public int compareTo(Version v) {
		Checker.checkNull(v, "v");
		if(v == this) {
			return 0;
		}
		int smallerIndex = _versionNumbers.length;
		if(v._versionNumbers.length < smallerIndex) {
			smallerIndex = v._versionNumbers.length;
		}
		for(int i=0; i<smallerIndex; i++) {
			if(_versionNumbers[i] > v._versionNumbers[i]) {
				return 1;
			}
			if(_versionNumbers[i] < v._versionNumbers[i]) {
				return -1;
			}
		}
		// If we got here, versions are equal so far.
		if(_versionNumbers.length == v._versionNumbers.length) {
			return 0;
		}
		else if(_versionNumbers.length > v._versionNumbers.length) {
			for(int i=smallerIndex; i<_versionNumbers.length; i++) {
				if(_versionNumbers[i] > 0) {
					return 1;
				}
			}
			return 0;
		}
		else if(_versionNumbers.length < v._versionNumbers.length) {
			for(int i=smallerIndex; i<v._versionNumbers.length; i++) {
				if(v._versionNumbers[i] > 0) {
					return -1;
				}
			}
			return 0;
		}
		else {
			throw new IntegrityException();
		}
	}
	
	public boolean after(Version v) {
		return compareTo(v) == 1;
	}

	public boolean before(Version v) {
		return compareTo(v) == -1;
	}

	public String toString() {
		// TODO cache
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<_versionNumbers.length; i++) {
			sb.append(_versionNumbers[i]);
			if(i < _versionNumbers.length-1) {
				sb.append(".");
			}
		}
		return sb.toString();
	}
}
