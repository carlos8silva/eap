package com.netx.basic.R1.logging;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.eh.IllegalUsageException;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.ExtendedInputStream;
import com.netx.basic.R1.io.ExtendedWriter;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.basic.R1.io.FileNotFoundException;
import com.netx.basic.R1.io.AccessDeniedException;
import com.netx.basic.R1.io.FileLockedException;
import com.netx.basic.R1.io.ReadWriteException;
import com.netx.basic.R1.logging.LogFile.FORMAT;


abstract class AbstractLogger {

	// TYPE:
	protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	protected static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
	private static final String _EX_SUFFIX = "\tat ";

	// INSTANCE:
	private Entry _logEntry;
	private Entry _exEntry;
	private boolean _enabled;
	
	protected AbstractLogger(PrintStream out) {
		setOutput(out);
		_enabled = true;
	}

	protected AbstractLogger(LogFile logFile) throws BasicIOException {
		Checker.checkNull(logFile, "logFile");
		setOutput(logFile);
		_enabled = true;
	}

	public synchronized void setOutput(PrintStream out) {
		Checker.checkNull(out, "out");
		// Close previous log log entries:
		_closeEntries();
		// Set new entries:
		_logEntry = new Entry(null);
		if(out == System.out) {
			_logEntry.tw = new TextWriter(new ExtendedWriter(out, "System.out"));
		}
		else if(out == System.err) {
			_logEntry.tw = new TextWriter(new ExtendedWriter(out, "System.err"));
		}
		else {
			throw new IllegalArgumentException("unknown stream: "+out);
		}
		_exEntry = writesStackTraces() ? _logEntry : null;
	}

	public synchronized void setOutput(LogFile logFile) throws BasicIOException {
		Checker.checkNull(logFile, "logFile");
		// Reset the log file:
		logFile.reset();
		// Close previous log log entries:
		_closeEntries();
		// Load the log file:
		_logEntry = _loadLogFile(logFile);
		if(!writesStackTraces()) {
			_exEntry = null;
		}
		else {
			if(logFile.getFormat().equals(FORMAT.TXT)) {
				_exEntry = _logEntry;
			}
			else {
				// Get same log filename, with .txt extension:
				String filename = logFile.getOriginalFileName();
				filename = filename.substring(0, filename.lastIndexOf(".")) + ".txt";
				LogFile exLogFile = new LogFile(logFile.getDirectory(), filename, logFile.getNumDays());
				_exEntry = _loadLogFile(exLogFile);
			}
		}
	}

	public boolean getEnabled() {
		return _enabled;
	}

	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

	public LogFile getLogFile() {
		return _logEntry.logFile;
	}

	public String toString() {
		String output = null;
		if(getLogFile() != null) {
			output = getLogFile().getCurrentLogFile();
		}
		else {
			output = _logEntry.tw.getWriter().getPath();
		}
		return getClass().getSimpleName()+": "+output;
	}

	public void finalize() {
		try {
			_closeEntries();
			super.finalize();
		}
		catch(Throwable t) {
			Tools.handleCriticalError(t);
		}
	}
	
	// Methods to be implemented by specific Loggers:
	protected abstract ExtendedInputStream getTemplate();
	protected abstract boolean writesStackTraces();
	protected abstract String[] getHeader();
	protected abstract String[] formatLine(Object ... line);
	protected abstract void printLine(ExtendedWriter writer, Object ... line) throws ReadWriteException;
	
	protected StackTraceElement getTrace() {
		Exception e = new Exception();
		StackTraceElement[] elems = e.getStackTrace();
		for(int i=elems.length-1; true; i--) {
			if(elems[i].toString().contains(getClass().getName())) {
				return elems[i+1];
			}
		}
	}

	// This must be synchronized to allow multiple threads
	// to write to this logger without having mixed output.
	protected synchronized void printStackTrace(Timestamp ts, Throwable t) {
		if(!_enabled) {
			return;
		}
		if(_exEntry == null) {
			throw new IllegalUsageException("cannot print stack traces");
		}
		try {
			if(_exEntry.logFile != null && _exEntry.logFile.needsToUpdateFilename()) {
				_resetLogFile(_exEntry);
			}
			ExtendedWriter out = _exEntry.tw.getWriter();
			// We only write the timestamp associated with the stack trace
			// if we are writing to a CSV or XLS log file:
			if(_logEntry.logFile != null && _logEntry.logFile.getFormat() != FORMAT.TXT) {
				out.write('[');
				out.write(new Long(ts.getTimeInMilliseconds()).toString());
				out.write(']');
				out.newLine();
			}
			out.write(t.toString());
			out.newLine();
			for(StackTraceElement ste : t.getStackTrace()) {
				out.write(_EX_SUFFIX);
				out.write(ste.toString());
				out.newLine();
			}
			out.flush();
		}
		catch(BasicIOException fse) {
			_changeToStderr(_exEntry, fse);
		}
	}

	// This must be synchronized to allow multiple threads
	// to write to this logger without having mixed output.
	protected synchronized void write(Object ... line) {
		if(!_enabled) {
			return;
		}
		try {
			if(_logEntry.logFile != null && _logEntry.logFile.needsToUpdateFilename()) {
				_resetLogFile(_logEntry);
			}
			_write(line);
			if(_logEntry.cw != null) {
				// getMaxNumLines() depends on the ColumnWriter implementation:
				if(_logEntry.cw.getTotalNumLines() > _logEntry.cw.getMaxNumLines()) {
					_resetLogFile(_logEntry);
				}
			}
		}
		catch(ReadWriteException io) {
			// Need to recover from the exception gracefully.
			// 1st failure: lets try to get another file:
			try {
				Tools.handleCriticalError(io);
				_resetLogFile(_logEntry);
			}
			catch(BasicIOException fse) {
				// 2nd failure: switch over to stderr:
				_changeToStderr(_logEntry, fse);
			}
			finally {
				// Write the line we were attempting previously:
				_writeSafely(line);
			}
		}
		catch(Throwable t) {
			// This can only be caused by runtime exceptions, i.e.
			// unexpected errors. It should never happen, but just
			// to be on the safe side we change to stderr immediately:
			_changeToStderr(_logEntry, t);
			_writeSafely(line);
		}
	}

	private void _write(Object ... line) throws ReadWriteException {
		if(_logEntry.tw != null) {
			ExtendedWriter out = _logEntry.tw.getWriter();
			printLine(out, line);
			out.flush();
		}
		else {
			String[] formatted = formatLine(line);
			_logEntry.cw.write(formatted);
		}
	}
	
	private void _writeSafely(Object ... line) {
		try {
			_write(line);
		}
		catch(ReadWriteException io) {
			// This is not supposed to happen; this method is
			// only called after we have switched over to stderr.
			throw new IntegrityException(io);
		}
	}

	private Entry _loadLogFile(LogFile logFile) throws BasicIOException {
		Entry e = new Entry(logFile);
		String filename = logFile.getNextLogFile();
		// Create the log file, in case it doesn't yet exist:
		File file = logFile.getDirectory().getFile(filename);
		if(file == null) {
			file = logFile.getDirectory().createFile(filename);
		}
		// Open and lock the log file:
		try {
			_openLogWriter(e, file);
			return e;
		}
		catch(FileLockedException fle) {
			// If the file is locked by another application,
			// we need to create a new one:
			file = logFile.getDirectory().createFile(logFile.getNextLogFile());
			_openLogWriter(e, file);
			return e;
		}
	}
	
	private void _openLogWriter(Entry e, File file) throws FileNotFoundException, AccessDeniedException, ReadWriteException {
		if(e.logFile.getFormat() == FORMAT.TXT) {
			e.tw = new TextWriter(file);
		}
		else if(e.logFile.getFormat() == FORMAT.CSV) {
			e.cw = new CsvWriter(file);
			if(file.isBlank()) {
				e.cw.write(getHeader());
			}
		}
		else if(e.logFile.getFormat() == FORMAT.XLS) {
			XlsWriter xlsw = new XlsWriter(file, getTemplate());
			// We need to register XLS writers as disposables,
			// to will prevent the JVM from finalizing this
			// object. XLS writers need to write the file to
			// disk before being finalized, because they do
			// not flush data on every write like TXT writers.
			Globals.registerDisposable(xlsw);
			e.cw = xlsw;
		}
		else {
			throw new IntegrityException(e.logFile.getFormat());
		}
	}
	
	private void _resetLogFile(Entry e) throws BasicIOException {
		e.close();
		File file = e.logFile.getDirectory().createFile(e.logFile.getNextLogFile());
		_openLogWriter(e, file);
	}

	private void _changeToStderr(Entry e, Throwable t) {
		System.err.println("[unexpected error occurred while logging information to a log file:]");
		t.printStackTrace();
		System.err.println("[the system will now change the logging destination to the standard error output]");
		// Dump contents of XLS writer logger to stderr:
		if(e.cw != null && e.logFile.getFormat().equals(FORMAT.XLS)) {
			XlsWriter xlsw = (XlsWriter)e.cw;
			List<String[]> buffer = xlsw.getBuffer();
			if(buffer.isEmpty()) {
				System.err.println("[log buffer empty, no need to dump contents to standard error output]");
			}
			else {
				System.err.println("[dumping current contents of log buffer]");
				buffer.add(0, getHeader());
				for(Object[] line : buffer) {
					for(Object o : line) {
						System.err.print(o);
						System.err.print('\t');
					}
					System.err.println();
				}
				System.err.println("[log buffer dumped]");
				buffer.clear();
			}
		}
		e.close();
		e.logFile = null;
		e.tw = new TextWriter(new ExtendedWriter(System.err, "System.err"));
	}

	private void _closeEntries() {
		if(_logEntry != null) {
			_logEntry.close();
		}
		if(_exEntry != null) {
			_exEntry.close();
		}
	}

	private class Entry {

		public LogFile logFile;
		public TextWriter tw;
		public ColumnWriter cw;

		public Entry(LogFile logFile) {
			this.logFile = logFile;
		}
		
		public void close() {
			// Text writer:
			try {
				if(tw != null) {
					tw.close();
					tw = null;
				}
			}
			catch(ReadWriteException io) {
				// If this happens, it's because the logger has been locked
				// by an external application and we cannot close it now.
				// In the case of XLS writers, this can also happen if there is
				// an error flushing the XLS file to disk. 
				System.err.println("[warning: error when closing text writer:]");
				io.printStackTrace();
			}
			// Column writer:
			try {
				if(cw != null) {
					cw.close();
					// When dealing with XLS writers, if close() is called
					// we need to unregister the writer as a disposable object
					// so that it can be safely garbage collected and finalized.
					if(logFile.getFormat().equals(FORMAT.XLS)) {
						Globals.unregisterDisposable((XlsWriter)cw);
					}
					cw = null;
				}
			}
			catch(ReadWriteException io) {
				// Same as above:
				System.err.println("[warning: error when closing column writer:]");
				io.printStackTrace();
			}
		}
	}
}
