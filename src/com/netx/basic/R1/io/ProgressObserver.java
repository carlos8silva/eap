package com.netx.basic.R1.io;

public class ProgressObserver {

	private long _progress;
	
	public ProgressObserver() {
		_progress = 0L;
	}
	
	public long getProgress() {
		return _progress;
	}
	
	public void increment(long value) {
		_progress += value;
	}
}
