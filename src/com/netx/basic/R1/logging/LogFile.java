package com.netx.basic.R1.logging;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.DateFormatException;
import com.netx.generics.R1.time.TimeValue;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.time.TimeValue.MEASURE;
import com.netx.generics.R1.util.Strings;
import com.netx.generics.R1.collections.IList;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.io.Directory;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.FilenameFilter;
import com.netx.basic.R1.io.SearchOptions;
import com.netx.basic.R1.io.FileSystemException;


public class LogFile {
	
	// TYPE:
	private static final DateFormat _DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat _TIME_FORMAT = new SimpleDateFormat("hh:mm:ss.SSS");
	private static final String _DATE = "<date>";
	private static final String _SUFFIX = "_02";
	// TODO document
	private static int _xlsBufferSize = 20;
	
	static {
		// We need to disable lenient parsing, so that we are stricter 
		// when interpreting dates. Otherwise, a date written as
		// '20071246' will be interpreted as '20080115'.
		_DATE_FORMAT.setLenient(false);
		_TIME_FORMAT.setLenient(false);
	}

	public static enum FORMAT {
		TXT,
		CSV,
		XLS
	}

	public static int getXlsBufferSize() {
		return _xlsBufferSize;
	}

	public static void setXlsBufferSize(int value) {
		_xlsBufferSize = value;
	}

	// INSTANCE:
	private final Directory _dir;
	private final String _filename;
	private final int _filenameLength;
	private final FORMAT _format;
	private final int _numDays;
	private final boolean _hasDateIdentifier;
	private String _lastFilename;
	private Timestamp _lastDate;
	
	public LogFile(Directory dir, String filename, int numDays) {
		Checker.checkNull(dir, "dir");
		Checker.checkEmpty(filename, "filename");
		Checker.checkMinValue(numDays, 0, "numDays");
		Checker.checkMaxValue(numDays, 360, "numDays");
		_dir = dir;
		_filename = filename;
		_numDays = numDays;
		if(_filename.endsWith(".txt") || _filename.endsWith(".log")) {
			_format = FORMAT.TXT;
		}
		else if(_filename.endsWith(".csv")) {
			_format = FORMAT.CSV;
		}
		else if(_filename.endsWith(".xls")) {
			_format = FORMAT.XLS;
		}
		else {
			throw new IllegalArgumentException("illegal file format: "+_filename);
		}
		_hasDateIdentifier = filename.indexOf(_DATE) != -1;
		if(_hasDateIdentifier) {
			// The actual filename will change from <date> to yyyyMMdd:
			_filenameLength = _filename.length() + 2;
		}
		else {
			_filenameLength = _filename.length();
		}
		if(numDays > 0 && !_hasDateIdentifier) {
			throw new IllegalArgumentException("<date> identifier not found, numDays = "+numDays);
		}
		if(numDays == 0 && _hasDateIdentifier) {
			throw new IllegalArgumentException("numDays = 0 used when filename has a <date> identifier");
		}
		if(numDays > 0 && Strings.countOccurrences(filename, _DATE) > 1) {
			throw new IllegalArgumentException("<date> identifier can only appear once");
		}
		_lastFilename = null;
		_lastDate = null;
	}

	public LogFile(Directory dir, String filename) {
		this(dir, filename, 0);
	}

	public Directory getDirectory() {
		return _dir;
	}

	public String getOriginalFileName() {
		return _filename;
	}

	public FORMAT getFormat() {
		return _format;
	}

	public int getNumDays() {
		return _numDays;
	}

	// For AbstractLogger:
	String getNextLogFile() throws FileSystemException {
		if(_lastFilename == null) {
			// this LogFile hasn't been initialized yet:
			_lastFilename = _findLastFilename();
			if(needsToUpdateFilename()) {
				_lastFilename = _generateNew();
			}
		}
		else {
			if(needsToUpdateFilename()) {
				_lastFilename = _generateNew();
			}
			else {
				_lastFilename = _appendNumber();
			}
		}
		return _lastFilename;
	}

	// For AbstractLogger:
	String getCurrentLogFile() {
		if(_lastFilename == null) {
			return getOriginalFileName();
		}
		else {
			return _lastFilename;
		}
	}

	// For AbstractLogger:
	void reset() {
		_lastFilename = null;
		_lastDate = null;
	}

	// For AbstractLogger:
	boolean needsToUpdateFilename() {
		if(!_hasDateIdentifier) {
			return false;
		}
		long difference = new Timestamp().getTimeInMilliseconds() - _lastDate.getTimeInMilliseconds();
		long numDays = new TimeValue(difference, MEASURE.MILLISECONDS).getAs(MEASURE.DAYS);
		if(numDays > _numDays) {
			return true;
		}
		else {
			return false;
		}
	}

	// For Logger:
	Date getLastFileChangeDate() {
		if(_lastDate == null) {
			return null;
		}
		else {
			return _lastDate.getDate();
		}
	}

	// Finds the last matching log file.
	// Please note that this method is only called once.
	private String _findLastFilename() throws FileSystemException {
		FilenameFilter filter = null;
		String[] array = null;
		if(_hasDateIdentifier) {
			array = Strings.replaceAll(_filename, _DATE, ":").split("[:]");
			if(array.length != 2) {
				throw new IntegrityException(array.length);
			}
			filter = new DateFilenameFilter(array[0], array[1]);
		}
		else {
			filter = new PlainFilenameFilter(_filename);
		}
		IList<File> list = _dir.getFiles(new SearchOptions(filter, true, SearchOptions.ORDER.NAME_DESCENDING));
		// By now we have a sorted list of files where the last one may be what we want:
		if(list.isEmpty()) {
			if(_hasDateIdentifier) {
				return _generateNew();
			}
			else {
				return _filename;
			}
		}
		else {
			if(_hasDateIdentifier) {
				// We need to update the _lastDate according to the file name:
				String extractedDate = list.get(0).getName().substring(array[0].length(), list.get(0).getName().length() - array[1].length());
				// No need to catch a DateFormatException:
				// the FilenameFilter already checked this.
				_lastDate = new Timestamp(extractedDate, _DATE_FORMAT);
			}
			return list.get(0).getName();
		}
	}

	// Generates a new filename with an updated date:
	private String _generateNew() {
		_lastDate = new Timestamp();
		_lastFilename = Strings.replaceAll(_filename, _DATE, _lastDate.format(_DATE_FORMAT));
		return _lastFilename;
	}

	// Generates a new filename with an appended "_XX" number:
	private String _appendNumber() {
		// Break the filename to remove extension:
		String[] array  = new String[2];
		int index = _lastFilename.lastIndexOf('.');
		array[0] = _lastFilename.substring(0, index);
		array[1] = _lastFilename.substring(index);
		if(_lastFilename.length() > _filenameLength) {
			// Already has a "_XX" suffix, need to increment it:
			index = array[0].lastIndexOf('_');
			String prefix = array[0].substring(0, index);
			String suffix = array[0].substring(index+1);
			int newNumber = new Integer(suffix) + 1;
			return prefix + "_" + Strings.valueOf(newNumber, (newNumber > 99 ? 3 : 2)) + array[1];
		}
		else {
			// No "_XX" suffix, we just need to add it:
			return array[0] + _SUFFIX + array[1];
		}
	}
	
	private class DateFilenameFilter implements FilenameFilter {

		private final String _prefix;
		private final String _suffix;
		
		public DateFilenameFilter(String prefix, String suffix) {
			_prefix = prefix;
			_suffix = suffix;
		}

		// Only accepts the filename if it starts with the specified prefix,
		// and if it ends with the specified suffix (possibly added with an 
		// extra _XX), and has a date in the middle. 
		public boolean accept(String filename) {
			// Plain length, for efficiency:
			if(filename.length() != _prefix.length()+8+_suffix.length() && filename.length() != _prefix.length()+8+3+_suffix.length()) {
				return false;
			}
			// Prefix:
			if(!filename.startsWith(_prefix)) {
				return false;
			}
			// Date:
			filename = filename.substring(_prefix.length());
			StringBuilder date = new StringBuilder();
			for(int i=0; i<8; i++) {
				if(!Character.isDigit(filename.charAt(i))) {
					return false;
				}
				date.append(filename.charAt(i));
			}
			// (Possible) _XX:
			filename = filename.substring(8);
			if(filename.length() > _suffix.length()) {
				if(filename.charAt(0) != '_') {
					return false;
				}
				if(!Character.isDigit(filename.charAt(1))) {
					return false;
				}
				if(!Character.isDigit(filename.charAt(2))) {
					return false;
				}
				filename = filename.substring(3);
			}
			// Suffix:
			if(!filename.equals(_suffix)) {
				return false;
			}
			Date fileDate = null;
			// Check whether the date String is a valid date:
			try {
				fileDate = new Date(date.toString(), _DATE_FORMAT);
			}
			catch(DateFormatException dfe) {
				return false;
			}
			// Check whether the date on the log file is bigger than today:
			if(fileDate.after(new Timestamp().getDate())) {
				return false;
			}
			// All conditions have passed:
			return true;
		}
	}
	
	private class PlainFilenameFilter implements FilenameFilter {
		
		public final String _filename;
		public final String _withoutExtension;
		
		public PlainFilenameFilter(String filename) {
			_filename = filename;
			_withoutExtension = _filename.substring(0, _filename.lastIndexOf('.'));
		}
		
		public boolean accept(String filename) {
			// Plain length, for efficiency:
			if((filename.length() != _filename.length()) && (filename.length() != _filename.length()+3)) {
				return false;
			}
			// If the filename is the same, we're done:
			if(_filename.equals(filename)) {
				return true;
			}
			// Otherwise, this must be a filename ended with "_XX":
			filename = filename.substring(_withoutExtension.length());
			if(filename.charAt(0) != '_') {
				return false;
			}
			if(!Character.isDigit(filename.charAt(1))) {
				return false;
			}
			if(!Character.isDigit(filename.charAt(2))) {
				return false;
			}
			// Check filename extension:
			filename = filename.substring(4);
			return _filename.endsWith(filename);
		}
	}
}
