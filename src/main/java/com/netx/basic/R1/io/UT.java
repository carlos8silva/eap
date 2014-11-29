package com.netx.basic.R1.io;

public class UT {

	public static ProtocolImpl createImpl(String base) {
		Location loc = new Location(base);
		return new ProtocolImplFile(loc);
	}

	public static ProtocolImpl createImpl(String base, String relative) {
		Location loc = new Location(base);
		return new ProtocolImplFile(loc, relative);
	}
}
