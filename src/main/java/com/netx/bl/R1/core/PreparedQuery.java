package com.netx.bl.R1.core;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.netx.basic.R1.logging.Logger;
import com.netx.basic.R1.shared.Constants;
import com.netx.bl.R1.spi.DatabaseDriver;


public class PreparedQuery {

	private final Connection _c;
	private final String _sql;
	private final Argument[] _args;
	private final String[] _values;
	private final Limit _limit;
	private final Statement _stmt;
	private final PreparedStatement _ps;
	private final boolean _retrieveKeys;

	// For Entity Select:
	public PreparedQuery(Query query, Connection c, String sql, Argument[] args, Limit limit) throws WrappedSQLException {
		this(query, c, sql, args, null, limit, false);
	}

	// For Global Select:
	public PreparedQuery(Query query, Connection c, String sql, Comparable<?>[] values, Limit limit) throws WrappedSQLException {
		this(query, c, sql, null, values, limit, false);
	}

	// For Insert and Update:
	public PreparedQuery(Query query, Connection c, String sql, Argument[] args, boolean retrieveKeys) throws WrappedSQLException {
		this(query, c, sql, args, null, null, retrieveKeys);
	}

	private PreparedQuery(Query query, Connection c, String sql, Argument[] args, Comparable<?>[] values, Limit limit, boolean retrieveKeys) throws WrappedSQLException {
		_c = c;
		_args = args;
		_retrieveKeys = retrieveKeys;
		// Add LIMIT clause to query if driver supports it.
		// Note: at this stage, the limit object is guaranteed to have any possible parameters resolved.
		if(limit != null && c.getRepository().getDriver().supportsLimitClause()) {
			_sql = sql + " " + limit;
			// Prevent the limit object from being used within PreparedQuery:
			_limit = null;
			limit = null;
		}
		else {
			_sql = sql;
			_limit = limit;
		}
		// Initialize values:
		if(args != null) {
			_values = new String[args.length];
			for(int i=0; i<args.length; i++) {
				_values[i] = Field.toSQL(args[i].getValue(), c.getRepository().getDriver());
			}
		}
		else if(values != null) {
			_values = new String[values.length];
			for(int i=0; i<values.length; i++) {
				_values[i] = Field.toSQL(values[i], c.getRepository().getDriver());
			}
		}
		else {
			_values = null;
		}
		try {
			if(query.getUsePreparedStatements() && c.getRepository().getConfig().getUsePreparedStatements() && c.getRepository().getDriver().supportsPreparedStatements()) {
				_stmt = null;
				_ps = c.getPreparedStatementFor(query, _sql, _getRetrieveKeysFlag());
				// Set WHERE arguments:
				int paramNum = 1;
				if(_values != null) {
					for(int i=0; i<_values.length; i++) {
						_ps.setObject(paramNum++, _values[i]==null ? Constants.NULL_UC : _values[i]);
					}
				}
				// Set LIMIT arguments:
				if(limit != null ) {
					_ps.setObject(paramNum++, limit.offset);
					_ps.setObject(paramNum++, limit.numRows);
				}
			}
			else {
				_stmt = c.createStatement();
				_ps = null;
			}
		}
		catch(SQLException sqle) {
			throw new WrappedSQLException(sqle, c, _sql, args);
		}
	}
	
	public long executeUpdate() throws WrappedSQLException {
		String sql = null;
		try {
			Statement stmt = null;
			long result = -1;
			if(_stmt != null) {
				stmt = _stmt;
				sql = _replaceArgs(_c.getRepository().getDriver(), _sql, _values);
				result = _stmt.executeUpdate(sql, _getRetrieveKeysFlag());
			}
			else {
				stmt = _ps;
				result = _ps.executeUpdate();
			}
			if(_retrieveKeys) {
				ResultSet rs = stmt.getGeneratedKeys();
				rs.next();
				result = rs.getLong(1);
				rs.close();
				stmt = null;
			}
			return result;
		}
		catch(SQLException sqle) {
			if(sql == null) {
				// Initialize SQL in case we are using a prepared statement:
				sql = _replaceArgs(_c.getRepository().getDriver(), _sql, _values);
			}
			throw new WrappedSQLException(sqle, _c, sql, _args);
		}
	}
	
	public QueryResults executeQuery() throws WrappedSQLException {
		Logger logger = _c.getRepository().getConfig().getLogger();
		String sql = null;
		try {
			if(_stmt != null) {
				sql = _replaceArgs(_c.getRepository().getDriver(), _sql, _values);
				logger.info(sql);
				return new QueryResults(this, _stmt.executeQuery(sql));
			}
			else {
				if(logger.getLevel() == Logger.LEVEL.INFO) {
					sql = _replaceArgs(_c.getRepository().getDriver(), _sql, _values);
					logger.info(sql);
				}
				return new QueryResults(this, _ps.executeQuery());
			}
		}
		catch(SQLException sqle) {
			if(sql == null) {
				// Initialize SQL in case we are using a prepared statement:
				sql = _replaceArgs(_c.getRepository().getDriver(), _sql, _values);
			}
			throw new WrappedSQLException(sqle, _c, sql, _args);
		}
	}
	
	public void close() throws WrappedSQLException {
		try {
			if(_stmt != null) {
				_stmt.close();
			}
			if(_ps != null) {
				// We do not close the prepared statement so
				// that it remains cached on the connection.
				_ps.clearParameters();
			}
		}
		catch(SQLException sqle) {
			throw new WrappedSQLException(sqle, _c);
		}
	}

	private String _replaceArgs(DatabaseDriver driver, String baseSQL, String[] values) {
		// Set any WHERE arguments for the query:
		StringBuilder preparedSQL = new StringBuilder(baseSQL);
		int index = 0;
		if(values != null) {
			for(int i=0; i<values.length; i++) {
				String value = values[i];
				if(value == null) {
					value = Constants.NULL_UC;
				}
				else {
					value = "'"+value+"'";
				}
				index = _replaceArg(preparedSQL, value, index);
			}
		}
		if(_limit != null) {
			if(_limit.offset != null) {
				index = _replaceArg(preparedSQL, _limit.offset.toString(), index);
			}
			if(_limit.numRows != null) {
				index = _replaceArg(preparedSQL, _limit.numRows.toString(), index);
			}
		}
		return preparedSQL.toString();
	}

	// Note: the "startIndex" argument is to ensure that any "?" character present
	// in the query's arguments is not picked up by indexOf("?")
	private int _replaceArg(StringBuilder target, String arg, int startIndex) {
		int index = target.indexOf("?", startIndex);
		target.replace(index, index+1, arg);
		return index + arg.length();
	}

	private int _getRetrieveKeysFlag() {
		return _retrieveKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
	}
}
