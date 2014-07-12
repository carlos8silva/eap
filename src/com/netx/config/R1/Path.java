package com.netx.config.R1;
import com.netx.basic.R1.eh.Checker;

class Path {

	// This method breaks a context path by "/". The broken path is
	// returned in the <var>.path variable, and <var>.name is always
	// null. If path is a direct name, <var>.path.length == 1.
	public static Path breakContextPath(String path) {
		if(path.startsWith("/")) {
			throw new IllegalArgumentException("context path cannot start with '/'");
		}
		Checker.checkTextIdentifier(path.replaceAll("/", ""), "path", true);
		// Just name:
		if(!path.contains("/")) {
			return new Path(new String[] {path}, null);
		}
		// Composed path:
		else {
			return new Path(path.split("[/]"), null);
		}
	}

	// Returns the name of the property in <var>.name, and a context
	// path only if the path is composed (otherwise, <var>.path is null).
	public static Path breakPropertyPath(String path) {
		Checker.checkEmpty(path, "path");
		// Composed path:
		if(path.contains("/")) {
			int index = path.lastIndexOf("/");
			String name = path.substring(index+1);
			// Treat regular context path:
			Path cpath = breakContextPath(path.substring(0, index));
			return new Path(cpath.path, name);
		}
		// Just the property name:
		else {
			Checker.checkTextIdentifier(path, "path", true);
			return new Path(null, path);
		}
	}
	
	public final String name;
	public final String[] path;
	public String failPath;
	
	private Path(String[] path, String name) {
		this.path = path;
		this.name = name;
		this.failPath = null;
	}
}
