package com.netx.bl.R1.core;


public abstract class Query {

	// TYPE:
	public static enum TYPE {
		SELECT,
		INSERT,
		UPDATE,
		DELETE,
		TRUNCATE
	}
	
	// INSTANCE:
	private final TYPE _type;
	private final String _name;
	private final MetaData _metaData;
	private final WhereExpr _where;
	private boolean _usePreparedStatements = true;
	private long _databaseHits = 0;
	private long _cacheHits = 0;

	protected Query(TYPE type, String name, MetaData metaData, WhereExpr where) {
		_type = type;
		_name = name;
		_metaData = metaData;
		_where = where;
	}
	
	public abstract String getOriginalSQL();
	public abstract String getParsedSQL();
	public abstract int getParameterCount();

	public TYPE getType() {
		return _type;
	}

	public String getName() {
		return _name;
	}

	public String getFullName() {
		if(_metaData == null) {
			return getName()==null ? null : "Global."+getName();
		}
		return _metaData.getName()+"."+getName();
	}

	public MetaData getMetaData() {
		return _metaData;
	}

	public boolean getUsePreparedStatements() {
		return _usePreparedStatements;
	}

	public void setUsePreparedStatements(boolean value) {
		_usePreparedStatements = value;
	}
	
	public WhereExpr getWhereClause() {
		return _where;
	}
	
	public long getDatabaseHitCount() {
		return _databaseHits;
	}

	public long getCacheHitCount() {
		return _cacheHits;
	}

	public long getTotalHitCount() {
		return _databaseHits + _cacheHits;
	}

	protected void countDatabaseHit(Repository r) {
		_databaseHits++;
		r.countDatabaseHit();
	}

	protected void countCacheHit(Repository r) {
		_cacheHits++;
		r.countCacheHit();
	}
}
