package com.netx.basic.R1.io;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.shared.Machine;


public class File extends FileObject {

	// For Directory:
	File(FileSystem fs, ProtocolImpl impl) {
		super(fs, impl);
	}
	
	public String getNameWithoutExtension() {
		String name = getName();
		if(name.contains(".")) {
			return name.substring(0, name.lastIndexOf("."));
		}
		else {
			return name;
		}
	}
	
	public String getExtension() {
		String name = getName();
		if(name.contains(".")) {
			return name.substring(name.lastIndexOf(".")+1);
		}
		else {
			return null;
		}
	}

	public long getSize() throws FileNotFoundException {
		checkExists();
		return getImpl().getSize();
	}

	public boolean isBlank() throws FileNotFoundException {
		return getSize() == 0L;
	}

	public boolean appearsSame(File aFile) throws FileNotFoundException, OperationFailedException {
		Checker.checkNull(aFile, "aFile");
		return getName().equals(aFile.getName()) && getSize()==aFile.getSize() && getLastModified().equals(aFile.getLastModified());
	}

	public void copyTo(Directory dir, String name) throws BasicIOException {
		Checker.checkNull(dir, "dir");
		Checker.checkEmpty(name, "name");
		checkExists();
		// Note: createFile throws ObjectAlreadyExistsException
		File dest = dir.createFile(name);
		ExtendedInputStream in = getInputStream();
		ExtendedOutputStream out = dest.getOutputStream();
		Streams.copy(in, out);
		out.close();
		in.close();
		dest.setLastModified(getLastModified());
		if(!getWritable()) {
			dest.setWritable(false);
		}
		if(getHidden()) {
			dest.setHidden(true);
		}
	}

	public void delete() throws FileSystemException {
		checkExists();
		if(Machine.runningOnWindows() && getFileSystem().getIgnoreReadOnly() == false) {
			checkWriteAccess();
		}
		if(!getImpl().delete()) {
			if(Machine.runningOnUnix()) {
				getParent().checkWriteAccess();
			}
			// This can be happen if the file has open streams or is open by another process:
			throw new OperationFailedException(getAbsolutePath());
		}
	}

	public ExtendedInputStream getInputStream() throws FileNotFoundException, FileLockedException {
		checkExists();
		return getImpl().getInputStream();
	}

	public ExtendedOutputStream getOutputStream() throws FileNotFoundException, AccessDeniedException {
		return getOutputStream(false);
	}

	public ExtendedOutputStream getOutputStream(boolean append) throws FileNotFoundException, AccessDeniedException {
		checkExists();
		checkWriteAccess();
		return getImpl().getOutputStream(append);
	}

	public ExtendedOutputStream getOutputStreamAndLock() throws FileNotFoundException, AccessDeniedException, ReadWriteException {
		return getOutputStreamAndLock(false);
	}

	public ExtendedOutputStream getOutputStreamAndLock(boolean append) throws FileNotFoundException, AccessDeniedException, ReadWriteException {
		checkExists();
		checkWriteAccess();
		return getImpl().getOutputStreamAndLock(append);
	}
	
	OBJECT_TYPE getObjectType() {
		return OBJECT_TYPE.FILE;
	}
}
