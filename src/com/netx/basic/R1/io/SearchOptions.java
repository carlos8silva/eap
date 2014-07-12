package com.netx.basic.R1.io;
import com.netx.basic.R1.eh.Checker;


public class SearchOptions {

	public static enum ORDER {
		NAME_ASCENDING,
		NAME_DESCENDING,
		SIZE_ASCENDING,
		SIZE_DESCENDING,
		NO_ORDER
	}
	
	// For Directory:
	final FilenameFilter filter;
	final boolean showHidden;
	final ORDER order;
	
	public SearchOptions(FilenameFilter filter, boolean showHidden, ORDER order) {
		this.filter = filter;
		this.showHidden = showHidden;
		this.order = order;
		Checker.checkNull(order, "order");
	}

	public SearchOptions(FilenameFilter filter, boolean showHidden) {
		this(filter, showHidden, ORDER.NO_ORDER);
	}

	public SearchOptions(FilenameFilter filter) {
		this(filter, true);
		Checker.checkNull(filter, "filter");
	}
}
