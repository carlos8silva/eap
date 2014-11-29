package com.netx.basic.R1.io;
import java.util.List;
import java.util.ArrayList;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.l10n.L10n;


// TODO create an empty constructor that means the current directory
public class FileSystem extends Directory {

	// TYPE:
	private static final List<FilenameFilter> _defaultFilters = new ArrayList<FilenameFilter>();
	
	public static void addDefaultFilenameFilter(FilenameFilter filter) {
		Checker.checkNull(filter, "filter");
		_defaultFilters.add(filter);
	}

	// For Directory:
	static List<FilenameFilter> getDefaultFilenameFilters() {
		return _defaultFilters;
	}
	
	// INSTANCE:
	private boolean _ignoreReadOnly;
	
	private FileSystem(String basePath, String username, String password, boolean checkUP) throws FileNotFoundException, AccessDeniedException {
		super(null, ProtocolImpl.resolve(basePath, username, password, checkUP));
		if(!getImpl().exists()) {
			throw new FileNotFoundException(basePath, L10n.BASIC_MSG_FOLDER_NOT_FOUND);
		}
		if(!getImpl().isDirectory()) {
			throw new FileNotFoundException(basePath, L10n.BASIC_MSG_FOLDER_IS_FILE);
		}
		if(!getImpl().getReadable()) {
			throw new ReadAccessDeniedException(getAbsolutePath(), L10n.getContent(L10n.GLOBAL_WORD_DIRECTORY));
		}
		_ignoreReadOnly = true;
	}
	
	public FileSystem(String basePath) throws FileNotFoundException, AccessDeniedException {
		this(basePath, null, null, false);
	}

	public FileSystem(String basePath, String username, String password) throws FileNotFoundException, AccessDeniedException {
		this(basePath, username, password, true);
	}

	public FileSystem(java.io.File file) throws FileNotFoundException, AccessDeniedException {
		this(((java.io.File)Checker.checkNull(file, "file")).getAbsolutePath());
	}

	public FileSystem getFileSystem() {
		return this;
	}
	
	public boolean getIgnoreReadOnly() {
		return _ignoreReadOnly;
	}

	public void setIgnoreReadOnly(boolean value) {
		_ignoreReadOnly = value;
	}

	// TODO
	// getTotalSpace
	// getFreeSpace
	// getUsableSpace
	
}
