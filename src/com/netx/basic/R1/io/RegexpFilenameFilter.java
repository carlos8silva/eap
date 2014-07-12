package com.netx.basic.R1.io;
import java.util.regex.Pattern;

import com.netx.basic.R1.eh.Checker;


public class RegexpFilenameFilter implements FilenameFilter {

	private final Pattern _pattern;
	
	public RegexpFilenameFilter(String regexp) {
		Checker.checkNull(regexp, "regexp");
		_pattern = Pattern.compile(regexp);
	}

	public boolean accept(String name) {
		return _pattern.matcher(name).matches();
	}
}
