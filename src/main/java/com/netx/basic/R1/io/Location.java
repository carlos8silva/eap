package com.netx.basic.R1.io;

class Location {

	// TYPE:
	public static final String UNC_PREFIX_1 = "\\\\";
	public static final String UNC_PREFIX_2 = "//";

	// INSTANCE:
	private final String _hostname;
	private final String _path;
	
	public Location(String hostname, String path) {
		_hostname = hostname;
		_path = ProtocolImpl.makeCanonical(path, true);
	}
	
	public Location(String path) {
		this(null, path);
	}
	
	public String getHostname() {
		return _hostname;
	}
	
	public String getPath() {
		return _path;
	}
	
	public String getAbsolutePath() {
		if(_hostname == null) {
			return _path;
		}
		else {
			return UNC_PREFIX_2 + _hostname + '/' + _path;
		}
	}
	
	public String toString() {
		return getAbsolutePath();
	}
}
