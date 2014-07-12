package com.netx.basic.R1.logging;
import java.io.PrintStream;
import com.netx.basic.R1.io.ExtendedInputStream;
import com.netx.basic.R1.io.ExtendedWriter;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.basic.R1.io.ReadWriteException;


public class PerformanceLogger extends AbstractLogger {

	// TYPE:
	private static final String[] _HEADER = {"Operation", "Start Time", "End Time", "Time Elapsed", "Trace"};
	private static final String _TEMPLATE = "template-pl.xls";
	
	// INSTANCE:
	public PerformanceLogger(LogFile logFile) throws BasicIOException {
		super(logFile);
	}

	public PerformanceLogger(PrintStream out) {
		super(out);
	}

	protected ExtendedInputStream getTemplate() {
		return new ExtendedInputStream(getClass().getResourceAsStream(_TEMPLATE), _TEMPLATE);
	}

	protected boolean writesStackTraces() {
		return false;
	}

	protected String[] getHeader() {
		return _HEADER;
	}

	public void log(PerformanceMetric metric) {
		write(metric);
	}

	protected void printLine(ExtendedWriter out, Object ... line) throws ReadWriteException {
		PerformanceMetric metric = (PerformanceMetric)line[0];
		out.write(metric.getOperationName());
		out.write(": ");
		out.write(metric.getTimeElapsed().getTimeInMilliseconds()+"");
		out.write("ms");
		out.newLine();
	}

	protected String[] formatLine(Object ... line) {
		PerformanceMetric metric = (PerformanceMetric)line[0];
		String op = metric.getOperationName();
		String start = metric.getStart().format(TIME_FORMAT);
		String end = metric.getEnd().format(TIME_FORMAT);
		String elapsed = new Long(metric.getTimeElapsed().getTimeInMilliseconds()).toString();
		return new String[] {op, start, end, elapsed, getTrace().toString()};
	}
}
