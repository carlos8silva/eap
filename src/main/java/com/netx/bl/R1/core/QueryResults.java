package com.netx.bl.R1.core;
import java.sql.ResultSet;


public class QueryResults {

	private final PreparedQuery _pq;
	private final ResultSet _rs;
	
	public QueryResults(PreparedQuery pq, ResultSet rs) {
		_pq = pq;
		_rs = rs;
	}
	
	public ResultSet getResultSet() {
		return _rs;
	}

	public void close() throws WrappedSQLException {
		_pq.close();
	}
}
