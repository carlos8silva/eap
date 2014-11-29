package com.netx.basic.R1.io;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Stack;
import java.util.EmptyStackException;
import java.util.Iterator;
import com.netx.generics.R1.util.Strings;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.Machine;


// A ProtocolImpl provides the needed functions for FileImpl and
// DirectoryImpl to work. This abstract class provides path handling,
// while subclasses must provide the protocol-specific functions for
// working with the supplied file and directory paths.
// This class is made public for unit testing purposes.
public abstract class ProtocolImpl {

	// TYPE:
	private static final String _ILLEGAL_CHARS_BASE = "*?\"|<>";
	private static final String _ILLEGAL_CHARS_PATH = "\\:*?\"|<>";
	private static final int _TIMEOUT = 3000;
	protected static final String ROOT = "/";

	public static enum PROTOCOL {
		FILE,
		CIFS
	}

	public static ProtocolImpl resolve(String basePath, String username, String password, boolean checkUP) throws UnknownHostException {
		Checker.checkEmpty(basePath, "basePath");
		if(checkUP) {
			Checker.checkEmpty(username, "username");
			Checker.checkEmpty(password, "password");
		}
		if(username != null) {
			throw new IntegrityException("CIFS not yet supported");
		}
		else {
			basePath = basePath.trim();
			Location base = null;
			// Check whether the path is on the network (UNC):
			if(basePath.startsWith(Location.UNC_PREFIX_1) || basePath.startsWith(Location.UNC_PREFIX_2)) {
				// Check if the host is available:
				base = _parseHostName(basePath);
				if(Character.isDigit(base.getHostname().charAt(0))) {
					boolean isReachable = true;
					try {
						InetAddress address = InetAddress.getByAddress(Tools.parseInetAddress(base.getHostname()));
						// When using IP address, we do not get a host not found exception
						// and so need to explicitly check whether the host is reachable.
						// Calling isReachable, however, does result on a host not found exception.
						isReachable = address.isReachable(_TIMEOUT);
					}
					catch(java.net.UnknownHostException uhe) {
						throw new UnknownHostException(base.getHostname(), uhe);
					}
					catch(IOException io) {
						// We always get UnknownHostException on InetAddress.isReachable,
						// so this should never actually happen:
						throw new IntegrityException(io);
					}
					if(!isReachable) {
						throw new UnknownHostException(base.getHostname(), null);
					}
				}
				else {
					try {
						// For host names, it simply throws UnknownHostException.
						InetAddress.getByName(base.getHostname());
					}
					catch(java.net.UnknownHostException uhe) {
						throw new UnknownHostException(base.getHostname(), uhe);
					}
				}
			}
			else {
				// Regular local path:
				base = new Location(basePath);
			}
			return new ProtocolImplFile(base);
		}
	}

	public static String makeCanonical(String path, boolean isRoot) {
		if(!path.contains(".")) {
			return path;
		}
		String newPath = path;
		if(path.startsWith(ROOT)) {
			newPath = path.substring(1);
		}
		String driveLetter = null;
		if(isRoot && Machine.runningOnWindows()) {
			// Get rid of drive letter:
			driveLetter = newPath.substring(0, 2);
			newPath = newPath.substring(3);
			// Get rid of '\':
			newPath = Strings.replaceAll(newPath, "\\", ROOT);
		}
		Stack<String> stack = new Stack<String>();
		String[] pathElems = newPath.split("[/]");
		for(String p : pathElems) {
			if(p.equals("..")) {
				try {
					stack.pop();
				}
				catch(EmptyStackException ese) {
					throw new IllegalArgumentException("path '"+path+"' refers to a location above the file system's root");
				}
			}
			else if(p.equals(".")) {
				continue;
			}
			else {
				stack.push(p);
			}
		}
		
		// Ok, build final path:
		if(stack.isEmpty()) {
			// Path refers to root:
			if(isRoot && Machine.runningOnWindows()) {
				return driveLetter + '/';
			}
			else {
				return ROOT;
			}
		}
		else {
			// Regular path:
			StringBuilder sb = new StringBuilder();
			Iterator<?> it = stack.iterator();
			while(it.hasNext()) {
				sb.insert(0, stack.pop());
				sb.insert(0, ROOT);
			}
			if(isRoot && Machine.runningOnWindows()) {
				// Need to add drive letter:
				String s = driveLetter + sb.toString();
				// Need to convert '/' to '\':
				return Strings.replaceAll(s, ROOT, "\\");
			}
			else {
				return sb.toString();
			}
		}
	}
	
	private static Location _parseHostName(String path) {
		path = path.substring(2);
		path = Strings.replaceAll(path, "\\", "/");
		int index = path.indexOf('/');
		if(index == -1 || index == path.length()-1) {
			// Path only contains the host name:
			throw new IllegalArgumentException("UNC path must specify a directory on the remote host");
		}
		String hostname = path.substring(0, index);
		path = path.substring(index+1);
		return new Location(hostname, path);
	}

	// INSTANCE:
	private final Location _base;
	private String _path;
	
	// Constructor for FileSystem. This constructor guarantees that:
	// - base does not have illegal characters
	// - base is canonical (no "." or "..")
	protected ProtocolImpl(Location base) {
		_checkIllegalChars(base.getAbsolutePath(), _ILLEGAL_CHARS_BASE);
		_base = base;
		_path = ROOT;
	}

	// This constructor guarantees that:
	// - relativePath does not have illegal characters
	// - relativePath does not refer to a location before the FileSystem's root
	// - relativePath is canonical (no "." or "..")
	// relativePath is guaranteed to start with a leading slash (/) by Directory.
	protected ProtocolImpl(Location base, String relativePath) {
		_base = base;
		_checkIllegalChars(relativePath, _ILLEGAL_CHARS_PATH);
		_path = makeCanonical(relativePath, false);
	}

	public Location getBase() {
		return _base;
	}
	
	public String getAbsolutePath() {
		if(_path == null) {
			return _base.getAbsolutePath();
		}
		else {
			return _base.getAbsolutePath()+'/'+_path;
		}
	}

	public String getRelativePath() {
		return _path;
	}

	public ProtocolImpl getParent() {
		if(_path.equals(ROOT)) {
			// Already root:
			return null;
		}
		int index = _path.lastIndexOf('/');
		if(index == 0) {
			// Return root:
			return new ProtocolImplFile(_base);
		}
		else {
			// Regular directory:
			return new ProtocolImplFile(_base, _path.substring(index));
		}
	}

	public ProtocolImpl getChild(String path) {
		if(path.charAt(0) == '/') {
			// Relative to the root:
			return getImpl(_base, path);
		}
		else {
			// Relative to the current directory:
			path = _path.equals(ROOT) ? _path+path : _path+'/'+path;
			return getImpl(_base, path);
		}
	}

	protected void setRelativePath(String path) {
		_path = path;
	}

	// Protocol implementation's callbacks:
	public abstract PROTOCOL getProtocol();
	public abstract ProtocolImpl getImpl(Location base, String path);
	public abstract String getName();
	public abstract boolean exists();
	public abstract boolean isFile();
	public abstract boolean isDirectory();
	public abstract long getSize();
	public abstract boolean getReadable();
	public abstract boolean setReadable(boolean value, boolean ownerOnly);
	public abstract boolean getWritable();
	public abstract boolean setWritable(boolean value, boolean ownerOnly);
	public abstract boolean getExecutable();
	public abstract boolean setExecutable(boolean value, boolean ownerOnly);
	public abstract boolean getHidden();
	public abstract long getLastModified();
	public abstract boolean setLastModified(long value);
	public abstract boolean renameTo(String name);
	public abstract boolean mkdirs();
	public abstract boolean createNewFile() throws BasicIOException;
	public abstract boolean delete();
	public abstract void deleteOnExit();
	public abstract String[] list();
	public abstract URI toURI();
	public abstract URL toURL();
	public abstract ExtendedInputStream getInputStream() throws FileLockedException;
	public abstract ExtendedOutputStream getOutputStream(boolean append) throws AccessDeniedException;
	public abstract ExtendedOutputStream getOutputStreamAndLock(boolean append) throws AccessDeniedException, ReadWriteException;

	private void _checkIllegalChars(String path, String illegalChars) {
		for(int i=0; i<illegalChars.length(); i++) {
			if(path.indexOf(illegalChars.charAt(i)) != -1) {
				throw new IllegalArgumentException("malformed path '"+path+"': illegal char '"+illegalChars.charAt(i)+"'");
			}
		}
	}
}
