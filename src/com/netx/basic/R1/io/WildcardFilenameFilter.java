package com.netx.basic.R1.io;
import com.netx.generics.R1.util.Wildcards;


public class WildcardFilenameFilter implements FilenameFilter {
	
	private final Wildcards _pattern;
	
	public WildcardFilenameFilter(String pattern) {
		_pattern = Wildcards.compile(pattern);
	}

	public boolean accept(String filename) {
		return _pattern.matches(filename);
	}
}
