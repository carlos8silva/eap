package com.netx.bl.R1.core;

public class Blob implements Comparable<Blob> {

	private final byte[] _bytes;
	
	public Blob(byte[] bytes) {
		_bytes = bytes;
	}
	
	public byte[] getBytes() {
		return _bytes;
	}
	
	public int compareTo(Blob blob) {
		for(int i=0; i<_bytes.length; i++) {
			if(i > blob._bytes.length) {
				return 1;
			}
			if(_bytes[i] != blob._bytes[i]) {
				return _bytes[i]-blob._bytes[i];
			}
		}
		if(_bytes.length == blob._bytes.length) {
			return 0;
		}
		else {
			return -1;
		}
	}
}
