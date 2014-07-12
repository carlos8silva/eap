package com.netx.basic.R1.io;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.netx.generics.R1.collections.Vector;
import com.netx.generics.R1.collections.IList;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.io.SearchOptions.ORDER;
import com.netx.basic.R1.l10n.L10n;
import com.netx.basic.R1.shared.Machine;


public class Directory extends FileObject {

	private Vector<FilenameFilter> _filters = null;
	
	Directory(FileSystem fs, ProtocolImpl provider) {
		super(fs, provider);
		// Add FileSystem's default FilenameFilters to this directory:
		List<FilenameFilter> filters = FileSystem.getDefaultFilenameFilters();
		for(FilenameFilter f : filters) {
			addFilenameFilter(f);
		}
	}

	public boolean getTraversable() throws FileNotFoundException {
		checkExists();
		return getImpl().getExecutable();
	}

	public void setTraversable(boolean value, boolean ownerOnly) throws AccessDeniedException {
		// This only has an effect on UNIX platforms:
		if(Machine.runningOnUnix()) {
			if(!getImpl().setExecutable(value, ownerOnly)) {
				throwNotOwner();
			}
		}
	}

	public void setTraversable(boolean value) throws AccessDeniedException {
		setTraversable(value, true);
	}

	public Directory mkdirs(String path) throws FileSystemException {
		Checker.checkEmpty(path, "path");
		checkExists();
		if(Machine.runningOnUnix()) {
			checkWriteAccess();
		}
		// Check if a file or directory with the same name already exist:
		ProtocolImpl child = getImpl().getChild(path);
		if(child.exists()) {
			if(child.isDirectory()) {
				throw new FileAlreadyExistsException(L10n.GLOBAL_WORD_DIRECTORY, child.getAbsolutePath());
			}
			else {
				throw new FileAlreadyExistsException(L10n.GLOBAL_WORD_FILE, child.getAbsolutePath());
			}
		}
		if(!child.mkdirs()) {
			if(Machine.runningOnUnix()) {
				checkWriteAccess();
			}
			throw new OperationFailedException(getAbsolutePath());
		}
		return new Directory(getFileSystem(), child);
	}

	public File createFile(String path, boolean force) throws BasicIOException {
		checkExists();
		// Check whether a file or directory with the same name already exist:
		ProtocolImpl child = getImpl().getChild(path);
		if(child.exists() && child.isFile()) {
			throw new FileAlreadyExistsException(L10n.GLOBAL_WORD_FILE, child.getAbsolutePath());
		}
		if(child.exists() && child.isDirectory()) {
			throw new FileAlreadyExistsException(L10n.GLOBAL_WORD_DIRECTORY, child.getAbsolutePath());
		}
		// Check write access:
		if(Machine.runningOnUnix()) {
			checkWriteAccess();
		}
		// Create parent dir:
		ProtocolImpl parent = null;
		if(path.indexOf("/") != -1) {
			parent = child.getParent();
		}
		else {
			parent = getImpl();
		}
		if(!parent.exists()) {
			if(force) {
				parent.mkdirs();
			}
			else {
				throw new FileNotFoundException(parent.getAbsolutePath(), L10n.BASIC_MSG_FOLDER_NOT_FOUND);
			}
		}
		// Create file:
		if(!child.createNewFile()) {
			// createNewFile only returns false if the file
			// already exists, which has been checked previously:
			// TODO check whether this also happens when creating a file with more than 256 chars
			throw new IntegrityException(path);
		}
		return new File(getFileSystem(), child);
	}
	
	public File createFile(String path) throws BasicIOException {
		return createFile(path, false);
	}

	public void copyTo(Directory dir, SearchOptions options) throws BasicIOException {
		copyTo(dir, getName(), options);
	}

	public void copyTo(Directory dir, String name) throws BasicIOException {
		copyTo(dir, name, null);
	}

	public void copyTo(Directory dir, String name, SearchOptions options) throws BasicIOException {
		Checker.checkNull(dir, "dir");
		Checker.checkEmpty(name, "name");
		checkExists();
		Directory newMe = dir.getDirectory(name);
		if(newMe == null) {
			newMe = dir.mkdirs(name);
		}
		IList<File> allFiles = getFiles(options);
		for(File f : allFiles) {
			f.copyTo(newMe);
		}
		IList<Directory> allDirs = getDirectories(options);
		for(Directory subDir : allDirs) {
			subDir.copyTo(newMe);
		}
	}

	public void delete(boolean force) throws FileSystemException {
		checkExists();
		if(Machine.runningOnUnix()) {
			getParent().checkWriteAccess();
		}
		if(force) {
			// Delete the contents of this directory:
			if(Machine.runningOnUnix()) {
				checkWriteAccess();
			}
			IList<File> allFiles = getFiles();
			for(File f : allFiles) {
				f.delete();
			}
			// Recursively delete sub-directories:
			IList<Directory> allDirs = getDirectories();
			for(Directory dir : allDirs) {
				dir.delete(true);
			}
		}
		else {
			if(!isEmpty()) {
				throw new OperationFailedException(getAbsolutePath(), L10n.BASIC_MSG_FOLDER_NOT_EMPTY);
			}
		}
		if(!getImpl().delete()) {
			throw new IntegrityException(getAbsolutePath());
		}
	}

	public void delete() throws FileSystemException {
		delete(false);
	}
	
	public void deleteContents() throws FileSystemException {
		checkExists();
		if(Machine.runningOnUnix()) {
			checkWriteAccess();
		}
		IList<File> allFiles = getFiles();
		for(File f : allFiles) {
			f.delete();
		}
	}
	
	public long getSize() throws FileSystemException {
		return getSize(false);
	}
	
	public long getSize(boolean expanded) throws FileSystemException {
		checkExists();
		IList<File> allFiles = getFiles();
		long size = 0;
		for(File f : allFiles) {
			size += f.getSize();
		}
		if(expanded) {
			IList<Directory> allDirs = getDirectories();
			for(Directory dir : allDirs) {
				size += dir.getSize(true);
			}
		}
		return size;
	}
	
	public boolean exists(String path) throws FileNotFoundException {
		Checker.checkEmpty(path, "path");
		checkExists();
		return getImpl().getChild(path).exists();
	}

	public boolean isFile(String path) throws FileNotFoundException {
		Checker.checkEmpty(path, "path");
		checkExists();
		return getImpl().getChild(path).isFile();
	}

	public boolean isDirectory(String path) throws FileNotFoundException {
		Checker.checkEmpty(path, "path");
		checkExists();
		return getImpl().getChild(path).isDirectory();
	}

	public boolean canRead(String path) throws FileNotFoundException {
		Checker.checkEmpty(path, "path");
		checkExists();
		return getImpl().getChild(path).getReadable();
	}

	public boolean canWrite(String path) throws FileNotFoundException {
		Checker.checkEmpty(path, "path");
		checkExists();
		return getImpl().getChild(path).getWritable();
	}
	
	public boolean canExecute(String path) throws FileNotFoundException {
		Checker.checkEmpty(path, "path");
		checkExists();
		return getImpl().getChild(path).getExecutable();
	}

	public File getFile(String path) throws FileNotFoundException, ReadAccessDeniedException {
		Checker.checkEmpty(path, "path");
		checkExists();
		checkReadAccess();
		ProtocolImpl child = getImpl().getChild(path);
		if(child.exists() && child.isFile()) {
			return new File(getFileSystem(), child);
		}
		return null;
	}

	public File getFile(String path, boolean create) throws BasicIOException {
		File f = getFile(path);
		if(f != null) {
			return f;
		}
		if(create) {
			return createFile(path);
		}
		return null;
	}

	public Directory getDirectory(String path) throws FileNotFoundException, ReadAccessDeniedException {
		Checker.checkEmpty(path, "path");
		checkExists();
		checkReadAccess();
		ProtocolImpl child = getImpl().getChild(path);
		if(child.exists() && child.isDirectory()) {
			return new Directory(getFileSystem(), child);
		}
		return null;
	}

	public Directory getDirectory(String path, boolean mkdirs) throws FileSystemException {
		Directory dir = getDirectory(path);
		if(dir != null) {
			return dir;
		}
		if(mkdirs) {
			return mkdirs(path);
		}
		return null;
	}

	public void addFilenameFilter(FilenameFilter filter) {
		if(_filters == null) {
			_filters = new Vector<FilenameFilter>();
		}
		_filters.append(filter);
	}

	public boolean isEmpty() throws FileNotFoundException, OperationFailedException {
		checkExists();
		String[] list = getImpl().list();
		if(list == null) {
			throw new OperationFailedException(getAbsolutePath());
		}
		return list.length == 0;
	}

	public IList<String> listFiles() throws FileSystemException {
		return listFiles(null);
	}

	public IList<String> listFiles(SearchOptions options) throws FileSystemException {
		return _list(options, OBJECT_TYPE.FILE);
	}

	public IList<String> listDirectories() throws FileSystemException {
		return listDirectories(null);
	}

	public IList<String> listDirectories(SearchOptions options) throws FileSystemException {
		return _list(options, OBJECT_TYPE.DIRECTORY);
	}

	public IList<String> listAll() throws FileSystemException {
		return listAll(null);
	}

	public IList<String> listAll(SearchOptions options) throws FileSystemException {
		return _list(options, null);
	}

	public IList<File> getFiles() throws FileSystemException {
		return getFiles(null);
	}

	public IList<File> getFiles(SearchOptions options) throws FileSystemException {
		IList<String> fileNames = listFiles(options);
		File files[] = new File[fileNames.size()];
		for(int i=0; i<fileNames.size(); i++) {
			files[i]= getFile(fileNames.get(i));
		}
		return new IList<File>(files);
	}
	
	public IList<Directory> getDirectories() throws FileSystemException {
		return getDirectories(null);
	}

	public IList<Directory> getDirectories(SearchOptions options) throws FileSystemException {
		IList<String> dirNames = listDirectories(options);
		Directory dirs[] = new Directory[dirNames.size()];
		for(int i=0; i<dirNames.size(); i++) {
			dirs[i] = getDirectory(dirNames.get(i));
		}
		return new IList<Directory>(dirs);
	}

	public IList<FileObject> getAll() throws FileSystemException {
		return getAll(null);
	}

	public IList<FileObject> getAll(SearchOptions options) throws FileSystemException {
		IList<String> allObj = listAll(options);
		FileObject obj[] = new FileObject[allObj.size()];
		for(int i=0; i<obj.length; i++) {
			if(isFile(allObj.get(i))) {
				obj[i] =  getFile(allObj.get(i));
			}
			else {
				obj[i] = getDirectory(allObj.get(i));
			}
		}
		return new IList<FileObject>(obj);
	}

	public IList<File> findFiles(String name) throws FileSystemException {
		return findFiles(name, true);
	}
	
	public IList<File> findFiles(String name, boolean showHidden) throws FileSystemException {
		SearchOptions options = new SearchOptions(new StringFilenameFilter(name), showHidden);
		return findFiles(options);
	}
	
	public IList<File> findFiles(SearchOptions options) throws FileSystemException {
		List<File> results = new ArrayList<File>();
		_find(this, options, OBJECT_TYPE.FILE, results);
		return new IList<File>(results);
	}

	public IList<Directory> findDirectories(String name) throws FileSystemException {
		return findDirectories(new SearchOptions(new StringFilenameFilter(name)));
	}
	
	public IList<Directory> findDirectories(SearchOptions options) throws FileSystemException {
		List<Directory> results = new ArrayList<Directory>();
		_find(this, options, OBJECT_TYPE.DIRECTORY, results);
		return new IList<Directory>(results);
	}
	
	public IList<FileObject> findAll(String name) throws FileSystemException {
		return findAll(new SearchOptions(new StringFilenameFilter(name)));
	}
	
	public IList<FileObject> findAll(SearchOptions options) throws FileSystemException {
		List<FileObject> results = new ArrayList<FileObject>();
		_find(this, options, null, results);
		return new IList<FileObject>(results);
	}

	public String toString() {
		return getName();
	}

	// For Self:
	Vector<FilenameFilter> getFilenameFilters() {
		return _filters;
	}

	OBJECT_TYPE getObjectType() {
		return OBJECT_TYPE.DIRECTORY;
	}

	void checkExists() throws FileNotFoundException {
		if(!getImpl().exists()) {
			// The directory has been deleted by another process / thread:
			throw new FileNotFoundException(getAbsolutePath(), L10n.BASIC_MSG_FOLDER_DELETED);
		}
	}

	private IList<String> _list(SearchOptions options, OBJECT_TYPE type) throws FileNotFoundException, ReadAccessDeniedException, OperationFailedException {
		checkExists();
		checkReadAccess();
		String[] all = getImpl().list();
		if(all == null) {
			// TODO list==null may happen, if the directory path's length is over 257 chars.
			// TODO and what else?
			throw new OperationFailedException(getAbsolutePath());
		}
		// If there are no filename filters specified, no options, 
		// and we want all objects, we can just return the full results:
		if(_filters == null && getFileSystem().getFilenameFilters() == null && options == null && type == null) {
			return new IList<String>(all);
		}
		// Otherwise we need to filter:
		Vector<FilenameFilter> fsFilters = getFileSystem().getFilenameFilters();
		ArrayList<String> list = new ArrayList<String>();
		TopCycle:
		for(String filename : all) {
			// Own filters:
			if(_filters != null) {
				for(int i=0; i<_filters.size(); i++) {
					if(!_filters.get(i).accept(filename)) {
						continue TopCycle;
					}
				}
			}
			// FileSystem filters:
			if(fsFilters != null) {
				for(int i=0; i<fsFilters.size(); i++) {
					if(!fsFilters.get(i).accept(filename)) {
						continue TopCycle;
					}
				}
			}
			// Search options:
			if(options != null) {
				if(options.filter != null && !options.filter.accept(filename)) {
					continue TopCycle;
				}
				if(options.showHidden == false && getImpl().getChild(filename).getHidden()) {
					continue TopCycle;
				}
			}
			// Object type:
			if(type == null) {
				list.add(filename);
			}
			else if(type.equals(OBJECT_TYPE.DIRECTORY)) {
				if(isDirectory(filename)) {
					list.add(filename);
				}
			}
			else if(type.equals(OBJECT_TYPE.FILE)) {
				if(isFile(filename)) {
					list.add(filename);
				}
			}
			else {
				throw new IntegrityException(type);
			}
		}
		// Sort if needed:
		if(options != null) {
			if(options.order != ORDER.NO_ORDER) {
				Collections.sort(list, new StringComparator(options.order));
			}
		}
		return new IList<String>(list);
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	private void _find(Directory dir, SearchOptions options, OBJECT_TYPE type, List results) throws FileSystemException {
		checkExists();
		checkReadAccess();
		// Search on this directory:
		if(type == null) {
			IList<FileObject> all = dir.getAll(options);
			results.addAll(all);
		}
		else if(type == OBJECT_TYPE.FILE) {
			IList<File> files = dir.getFiles(options);
			results.addAll(files);
		}
		else if(type == OBJECT_TYPE.DIRECTORY) {
			IList<Directory> dirs = dir.getDirectories(options);
			results.addAll(dirs);
		}
		else {
			throw new IntegrityException(type);
		}
		// Descend sub-directories:
		IList<Directory> dirs = dir.getDirectories();
		for(Directory d : dirs) {
			if(d.getReadable()) {
				_find(d, options, type, results);
			}
		}
	}

	private class StringComparator implements Comparator<String> {
		
		private final byte _order;
		
		public StringComparator(ORDER order) {
			if(order == ORDER.NAME_ASCENDING) {
				_order = 1;
			}
			else if(order == ORDER.NAME_DESCENDING) {
				_order = -1;
			}
			else {
				throw new IntegrityException(order);
			}
		}
		
		public int compare(String a, String b) {
			return a.compareTo(b) * _order;
		}
	}
	
	private class StringFilenameFilter implements FilenameFilter {
		
		private final String _name;
		
		StringFilenameFilter(String filter) {
			_name = filter;
		}

		public boolean accept(String name) {
			return _name.equals(name);
		}
	}
}
