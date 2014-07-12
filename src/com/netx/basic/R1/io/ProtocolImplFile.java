package com.netx.basic.R1.io;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import com.netx.basic.R1.eh.IntegrityException;


class ProtocolImplFile extends ProtocolImpl {

	private final File _file;

	public ProtocolImplFile(Location base) {
		super(base);
		_file = new File(getBase().getAbsolutePath());
	}

	public ProtocolImplFile(Location base, String relativePath) {
		super(base, relativePath);
		if(getRelativePath().equals(ROOT)) {
			_file = new File(base.getAbsolutePath());
		}
		else {
			_file = new File(base.getAbsolutePath(), getRelativePath());
		}
	}

	public PROTOCOL getProtocol() {
		return PROTOCOL.FILE;
	}

	public ProtocolImpl getImpl(Location base, String path) {
		return new ProtocolImplFile(base, path);
	}
	
	public String getName() {
		return _file.getName();
	}

	// Overrides super.getAbsolutePath to get '\'
	// instead of '/' on Windows file systems.
	public String getAbsolutePath() {
		return _file.getAbsolutePath();
	}
	
	public boolean exists() {
		return _file.exists();
	}
	
	public boolean isFile() {
		return _file.isFile();
	}

	public boolean isDirectory() {
		return _file.isDirectory();
	}

	public boolean getReadable() {
		return _file.canRead();
	}
	
	public boolean setReadable(boolean value, boolean ownerOnly) {
		return _file.setReadable(value, ownerOnly);
	}

	// TODO this doesn't work on network filesystems
	// (it returns true, even if you don't have permissions to write)
	public boolean getWritable() {
		return _file.canWrite();
	}

	public boolean setWritable(boolean value, boolean ownerOnly) {
		return _file.setWritable(value, ownerOnly);
	}

	public boolean getExecutable() {
		return _file.canWrite();
	}

	public boolean setExecutable(boolean value, boolean ownerOnly) {
		return _file.setExecutable(value, ownerOnly);
	}

	public boolean getHidden() {
		return _file.isHidden();
	}
	
	public long getSize() {
		 return _file.length();
	}

	public long getLastModified() {
		return _file.lastModified();
	}

	public boolean setLastModified(long value) {
		return _file.setLastModified(value);
	}

	public boolean renameTo(String name) {
		ProtocolImpl newFile = getParent().getChild(name);
		boolean result = _file.renameTo(new File(newFile.getAbsolutePath()));
		if(result) {
			String newPath = getParent().getRelativePath()+"/"+name;
			setRelativePath(newPath);
		}
		return result;
	}

	public boolean mkdirs() {
		return _file.mkdirs();
	}

	public boolean createNewFile() throws BasicIOException {
		try {
			return _file.createNewFile();
		}
		catch(IOException io) {
			throw Translator.translateIOE(io, _file.getAbsolutePath());
		}
	}

	public boolean delete() {
		return _file.delete();
	}

	public void deleteOnExit() {
		_file.deleteOnExit();
	}

	public String[] list() {
		return _file.list();
	}

	public URI toURI() {
		return _file.toURI();
	}

	public URL toURL() {
		try {
			return toURI().toURL();
		}
		catch(MalformedURLException mue) {
			throw new IntegrityException(mue);
		}
	}

	public ExtendedInputStream getInputStream() throws FileLockedException {
		return new ExtendedInputStream(_file);
	}

	public ExtendedOutputStream getOutputStream(boolean append) throws FileLockedException {
		try {
			return new ExtendedOutputStream(_file, append, false);
		}
		catch(ReadWriteException ioe) {
			// InputOutputException is only thrown if a lock is attempted:
			throw new IntegrityException(ioe);
		}
	}

	public ExtendedOutputStream getOutputStreamAndLock(boolean append) throws FileLockedException, ReadWriteException {
		return new ExtendedOutputStream(_file, append, true);
	}
}
