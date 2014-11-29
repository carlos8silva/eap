package com.netx.bl.R1.core;
import com.netx.basic.R1.eh.Checker;


public final class Select extends Query {

	private final String _originalSQL;
	private final String _parsedSQL;
	private final OrderBy[] _orderBy;
	private final Limit _limit;
	private final Field[] _params;
	private final int _numParams;
	private final boolean _uniqueResult;
	private boolean _updatesCache;

	// Constructor for static SELECT statements:
	public Select(String name, MetaData metaData, String originalSQL, String parsedSQL, WhereExpr whereExpr, OrderBy[] orderBy, Limit limit, Field[] params, boolean uniqueResult) {
		super(TYPE.SELECT, name, metaData, whereExpr);
		_originalSQL = originalSQL;
		_parsedSQL = parsedSQL;
		_orderBy = orderBy;
		_limit = limit;
		_params = params;
		_numParams = _params.length;
		_uniqueResult = uniqueResult;
		_updatesCache = false;
	}

	// Constructor for global queries:
	public Select(String name, String originalSQL, String parsedSQL, OrderBy[] orderBy, Limit limit, int numParams) {
		super(TYPE.SELECT, name, null, null);
		_originalSQL = originalSQL;
		_parsedSQL = parsedSQL;
		_orderBy = orderBy;
		_limit = limit;
		_params = null;
		_numParams = numParams;
		_uniqueResult = false;
		_updatesCache = false;
	}
	
	public String getOriginalSQL() {
		return _originalSQL;
	}

	public String getParsedSQL() {
		return _parsedSQL;
	}

	public int getParameterCount() {
		return _numParams + (_limit==null ? 0 : _limit.getParameterCount());
	}

	public OrderBy[] getOrderByClause() {
		return _orderBy;
	}

	public Limit getLimitClause() {
		return _limit;
	}

	public boolean uniqueResult() {
		return _uniqueResult;
	}
	
	public boolean getUpdatesCache() {
		return _updatesCache;
	}

	public Select setUpdatesCache(boolean value) {
		_updatesCache = value;
		return this;
	}

	// For Entity:
	Argument[] prepareArgs(Comparable<?> ... values) {
		if(values.length != getParameterCount()) {
			throw new IllegalArgumentException("wrong number of arguments: expected "+_params.length+", found "+values.length);
		}
		Argument[] args = new Argument[_params.length];
		for(int i=0; i<args.length; i++) {
			args[i] = new Argument(_params[i], values[i]);
		}
		return args;
	}

	// For Entity:
	Limit prepareLimit(Comparable<?> ... values) {
		if(_limit == null) {
			return null;
		}
		int numParams = _limit.getParameterCount();
		if(numParams == 0) {
			return _limit;
		}
		int index = _params.length;
		Integer offset = _limit.offset;
		Integer numRows = _limit.numRows;
		if(offset == null) {
			Checker.checkNull(values[index], "values");
			offset = (Integer)values[index++];
		}
		if(numRows == null) {
			Checker.checkNull(values[index], "values");
			numRows = (Integer)values[index++];
		}
		return new Limit(offset, numRows);
	}

	// For Entity:
	QueryResults execute(Connection c, Argument[] args, Limit limit) throws WrappedSQLException {
		PreparedQuery pq = new PreparedQuery(this, c, _parsedSQL, args, limit);
		countDatabaseHit(c.getRepository());
		return pq.executeQuery();
	}

	// For Connection:
	QueryResults execute(Connection c, Comparable<?>[] values, Limit limit) throws WrappedSQLException {
		PreparedQuery pq = new PreparedQuery(this, c, _parsedSQL, values, limit);
		countDatabaseHit(c.getRepository());
		return pq.executeQuery();
	}
}
