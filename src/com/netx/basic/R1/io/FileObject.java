package com.netx.basic.R1.io;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import com.netx.generics.R1.time.Timestamp;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.l10n.L10n;
import com.netx.basic.R1.shared.Machine;


public abstract class FileObject implements Comparable<FileObject> {

	// TYPE:
	static enum OBJECT_TYPE { FILE, DIRECTORY };
	
	// INSTANCE:
	private final FileSystem _fs;
	private final ProtocolImpl _impl;
	private Directory _parent;

	protected FileObject(FileSystem fs, ProtocolImpl impl) {
		_fs = fs;
		_impl = impl;
		_parent = null;
	}
	
	public FileSystem getFileSystem() {
		return _fs;
	}

	public String getAbsolutePath() {
		return _impl.getAbsolutePath();
	}

	public String getRelativePath() {
		return _impl.getRelativePath();
	}

	public String getName() {
		return _impl.getName();
	}

	public Directory getParent() {
		if(_parent == null) {
			ProtocolImpl impl = _impl.getParent();
			if(impl == null) {
				return null;
			}
			else {
				_parent = new Directory(getFileSystem(), _impl.getParent());
			}
		}
		return _parent;
	}

	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(this == o) {
			return true;
		}
		if(!(o instanceof FileObject)) {
			return false;
		}
		return compareTo((FileObject)o) == 0;
	}

	public int hashCode() {
		return _impl.getAbsolutePath().hashCode();
	}

	public int compareTo(FileObject fo) {
		return getAbsolutePath().compareTo(fo.getAbsolutePath());
	}
	
	public boolean getReadable() throws FileNotFoundException {
		checkExists();
		return _impl.getReadable();
	}

	public void setReadable(boolean value, boolean ownerOnly) throws FileNotFoundException, NotOwnerException {
		checkExists();
		// This only has an effect on UNIX platforms:
		if(Machine.runningOnUnix()) {
			if(!_impl.setReadable(value, ownerOnly)) {
				throwNotOwner();
			}
		}
	}

	public void setReadable(boolean value) throws FileNotFoundException, AccessDeniedException {
		setReadable(value, true);
	}

	public boolean getWritable() throws FileNotFoundException {
		checkExists();
		return _impl.getWritable();
	}

	public void setWritable(boolean value, boolean ownerOnly) throws FileNotFoundException, AccessDeniedException {
		checkExists();
		if(!_impl.setWritable(value, ownerOnly)) {
			throwNotOwner();
		}
	}

	public void setWritable(boolean value) throws FileNotFoundException, AccessDeniedException {
		setWritable(value, true);
	}

	public boolean getHidden() throws FileNotFoundException {
		checkExists();
		return _impl.getHidden();
	}

	public void setHidden(boolean value) throws FileNotFoundException, FileAlreadyExistsException, WriteAccessDeniedException {
		checkExists();
		if(Machine.runningOnUnix()) {
			getParent().checkWriteAccess();
			// On UNIX a file is hidden if its name starts with '.':
			if(!_impl.getHidden()) {
				renameTo('.'+getName());
			}
		}
		if(Machine.runningOnWindows()) {
			// On Windows a file is hidden if it has been marked as such:
			try {
				String command = "attrib.exe +H \""+getAbsolutePath()+"\"";
				int result = Runtime.getRuntime().exec(command).waitFor();
				if(result == 0) {
					return;
				}
			}
			catch(InterruptedException ie) {
				throw new IntegrityException(ie);
			}
			catch(IOException io) {
				throw new IntegrityException(io);
			}
		}
		throw new IntegrityException();
	}

	public Timestamp getLastModified() throws FileNotFoundException, OperationFailedException {
		checkExists();
		long time = _impl.getLastModified();
		if(time == 0L) {
			throw new OperationFailedException(getAbsolutePath());
		}
		return new Timestamp(time);
	}

	public void setLastModified(long lastModified) throws FileSystemException {
		checkExists();
		if(!_impl.setLastModified(lastModified)) {
			if(Machine.runningOnUnix()) {
				checkWriteAccess();
			}
			throw new OperationFailedException(getAbsolutePath());
		}
	}

	public void setLastModified(Timestamp lastModified) throws FileSystemException {
		Checker.checkNull(lastModified, "lastModified");
		setLastModified(lastModified.getTimeInMilliseconds());
	}

	public void renameTo(String name) throws FileNotFoundException, FileAlreadyExistsException, WriteAccessDeniedException {
		Checker.checkEmpty(name, "name");
		if(name.contains("/")) {
			throw new IllegalArgumentException("cannot use relative paths; only simple names");
		}
		checkExists();
		// Check whether a file or directory with the same name already exist:
		ProtocolImpl child = getParent().getImpl().getChild(name);
		if(child.exists() && child.isFile()) {
			throw new FileAlreadyExistsException(L10n.GLOBAL_WORD_FILE, child.getAbsolutePath());
		}
		if(child.exists() && child.isDirectory()) {
			throw new FileAlreadyExistsException(L10n.GLOBAL_WORD_DIRECTORY, child.getAbsolutePath());
		}
		if(!_impl.renameTo(name)) {
			if(Machine.runningOnUnix()) {
				getParent().checkWriteAccess();
			}
			throw new IntegrityException();
		}
	}

	public abstract void copyTo(Directory dir, String name) throws BasicIOException;

	public void copyTo(Directory dir) throws BasicIOException {
		copyTo(dir, getName());
	}

	public abstract void delete() throws FileSystemException;

	public void deleteOnExit() throws FileNotFoundException {
		checkExists();
		_impl.deleteOnExit();
	}

	public URI toURI() {
		return _impl.toURI();
	}

	public URL toURL() {
		return _impl.toURL();
	}

	public String toString() {
		return getName();
	}
	
	// For subclasses:
	ProtocolImpl getImpl() {
		return _impl;
	}
	
	// For subclasses:
	abstract OBJECT_TYPE getObjectType();
	
	// For subclasses:
	void checkExists() throws FileNotFoundException {
		if(!getImpl().exists()) {
			// The file or directory has been deleted by another process / thread:
			if(getObjectType().equals(OBJECT_TYPE.DIRECTORY)) {
				throw new FileNotFoundException(getAbsolutePath(), L10n.BASIC_MSG_FOLDER_DELETED);
			}
			else if(getObjectType().equals(OBJECT_TYPE.FILE)) {
				throw new FileNotFoundException(getAbsolutePath(), L10n.BASIC_MSG_FILE_DELETED);
			}
			else {
				throw new IntegrityException(getObjectType());
			}
		}
	}

	// For subclasses:
	void checkReadAccess() throws FileNotFoundException, ReadAccessDeniedException {
		if(!getReadable()) {
			throw new ReadAccessDeniedException(getAbsolutePath(), _getObjectTypeName());
		}
	}

	// For subclasses:
	void checkWriteAccess() throws FileNotFoundException, WriteAccessDeniedException {
		if(!getWritable()) {
			if(Machine.runningOnWindows()) {
				throw new WriteAccessDeniedException(L10n.BASIC_MSG_WRITE_ACCESS_DENIED_RO, getAbsolutePath(), _getObjectTypeName());
			}
			else if(Machine.runningOnUnix()) {
				throw new WriteAccessDeniedException(L10n.BASIC_MSG_WRITE_ACCESS_DENIED, getAbsolutePath(), _getObjectTypeName());
			}
			else {
				throw new IntegrityException();
			}
		}
	}

	// For Directory:
	void throwNotOwner() throws NotOwnerException {
		if(Machine.runningOnUnix()) {
			throw new NotOwnerException(getAbsolutePath(), _getObjectTypeName());
		}
		else {
			throw new IntegrityException();
		}
	}

	private String _getObjectTypeName() {
		if(getObjectType().equals(OBJECT_TYPE.DIRECTORY)) {
			return L10n.getContent(L10n.GLOBAL_WORD_DIRECTORY);
		}
		else if(getObjectType().equals(OBJECT_TYPE.FILE)) {
			return L10n.getContent(L10n.GLOBAL_WORD_FILE);
		}
		else {
			throw new IntegrityException(getObjectType());
		}
	}
}
