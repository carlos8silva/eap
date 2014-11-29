package com.netx.bl.R1.sql;


class Constants {

	// Note: we cannot scan word-based operators as operators (they need to be keywords)
	public static final String[] KEYWORDS = new String[] {"select", "insert", "update", "delete", "truncate", "as", "from", "where", "null", "is", "and", "or", "join", "on", "like", "order", "by", "asc", "desc", "limit", "set"};
	public static final String[] OPERATORS = new String[] {"*", "?", "/", "+", "-", "=", "<=", ">=", "<>", "<", ">", "!=", "%"};
	public static final String ERROR_START = "error in query: ";
	
	private Constants() {
	}
}
