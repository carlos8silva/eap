package com.netx.basic.R1.logging;
import java.io.PrintStream;
import com.netx.generics.R1.time.Timestamp;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.io.ExtendedInputStream;
import com.netx.basic.R1.io.ExtendedWriter;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.basic.R1.io.ReadWriteException;
import com.netx.basic.R1.shared.Constants;


public class Logger extends AbstractLogger {

	// TYPE:
	private static final String[] _HEADER = {"Timestamp", "Type", "Date", "Time", "Message", "Trace", "Error"};
	private static final String _NO_MSG = "(no message)";
	private static final String _TEMPLATE = "template-logger.xls";

	public static enum LEVEL {
		INFO, WARN, ERROR
	}

	// INSTANCE:
	private LEVEL _level;

	public Logger(PrintStream out) {
		super(out);
		_level = LEVEL.INFO;
	}

	public Logger(LogFile logFile) throws BasicIOException {
		super(logFile);
		_level = LEVEL.INFO;
	}

	public LEVEL getLevel() {
		return _level;
	}

	public void setLevel(LEVEL level) {
		Checker.checkNull(level, "level");
		_level = level;
	}

	public void info(String message) {
		if(_level == LEVEL.INFO) {
			_log(LEVEL.INFO, message, null);
		}
	}
	
	public void warn(String message) {
		warn(message, null);
	}

	public void warn(String message, Throwable t) {
		if(_level == LEVEL.INFO || _level == LEVEL.WARN) {
			_log(LEVEL.WARN, message, t);
		}
	}

	public void error(String message, Throwable t) {
		_log(LEVEL.ERROR, message, t);
	}

	public void error(Throwable t) {
		error(null, t);
	}

	protected ExtendedInputStream getTemplate() {
		return new ExtendedInputStream(getClass().getResourceAsStream(_TEMPLATE), _TEMPLATE);
	}

	protected boolean writesStackTraces() {
		return true;
	}

	protected String[] getHeader() {
		return _HEADER;
	}

	protected void printLine(ExtendedWriter out, Object ... line) throws ReadWriteException {
		// Type:
		String sType = line[0].toString();
		out.write(sType);
		if(sType.length() == 4) {
			out.write(' ');
		}
		// Date / time:
		out.write(" [");
		out.write(line[1].toString());
		out.write("]: ");
		// Trace:
		out.write(line[2].toString());
		out.write(": ");
		// Message:
		out.write(line[3] == null ? _NO_MSG : line[3].toString());
		out.newLine();
	}

	// Format of line:
	// <timestamp> <type> <date> <time> <message> <trace> <error title>
	protected String[] formatLine(Object ... line) {
		Timestamp ts = (Timestamp)line[1];
		String id = new Long(ts.getTimeInMilliseconds()).toString();
		String sType = line[0].toString();
		String sDate = ts.getDate().format(DATE_FORMAT);
		String sTime = ts.getTime().format(TIME_FORMAT);
		String message = line[3] == null ? Constants.NO_MESSAGE : line[3].toString();
		String errorId = line[4] == null ? null : line[4].toString();
		return new String[] {id, sType, sDate, sTime, message, line[2].toString(), errorId};
	}

	// Fields in line:
	// <type> <timestamp> <trace> <message> <exception>
	private void _log(LEVEL type, String message, Throwable t) {
		Timestamp ts = new Timestamp();
		write(type, ts, getTrace(), message, t);
		if(t != null) {
			printStackTrace(ts, t);
			if(t.getCause() != null) {
				Throwable t2 = t.getCause();
				error("Caused by: ", t2);
			}
		}
	}
}
