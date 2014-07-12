package com.netx.basic.R1.logging;
import com.netx.basic.R1.eh.Checker;
import com.netx.generics.R1.time.Timestamp;


public class PerformanceMetric {

	private final String _op;
	private Timestamp _start;
	private Timestamp _end;
	private Timestamp _time;
	
	public PerformanceMetric(String operation) {
		Checker.checkEmpty(operation, "operation");
		_op = operation;
	}

	public void start() {
		if(_start != null) {
			throw new IllegalStateException("this object has already been started");
		}
		_start = new Timestamp();
	}

	public void end() {
		if(_start == null) {
			throw new IllegalStateException("this object hasn't been started yet");
		}
		if(_end != null) {
			throw new IllegalStateException("this object has already been ended");
		}
		Timestamp end = new Timestamp();
		_end = end;
		_time = new Timestamp(_end.getTimeInMilliseconds() - _start.getTimeInMilliseconds());
	}

	public String getOperationName() {
		return _op;
	}

	public Timestamp getStart() {
		return _start;
	}

	public Timestamp getEnd() {
		return _end;
	}

	public Timestamp getTimeElapsed() {
		return _time;
	}
}
