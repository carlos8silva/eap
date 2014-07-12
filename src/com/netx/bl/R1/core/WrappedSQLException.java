package com.netx.bl.R1.core;
import java.sql.SQLException;


class WrappedSQLException extends Exception {

	private final Connection _c;
	private final String _sql;
	private final Argument[] _args;
	
	public WrappedSQLException(SQLException sqle, Connection c, String sql, Argument[] args) {
		super(sqle);
		_c = c;
		_sql = sql;
		_args = args;
	}
	
	public WrappedSQLException(SQLException sqle, Connection c) {
		this(sqle, c, null, null);
	}

	public Connection getConnection() {
		return _c;
	}
	
	public String getSQL() {
		return _sql;
	}
	
	public Argument[] getArguments() {
		return _args;
	}
}
