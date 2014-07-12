package com.netx.bl.R1.core;
import java.util.Iterator;


final class Insert extends Query {

	private final String _sql;

	public Insert(MetaData metaData) {
		super(TYPE.INSERT, "insert", metaData, null);
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(metaData.getTableName());
		sb.append("(");
		Iterator<Field> it = metaData.getFields().iterator();
		int numFieldsAdded = 0;
		while(it.hasNext()) {
			Field f = it.next();
			if(!f.isAutonumber()) {
				if(numFieldsAdded > 0) {
					sb.append(", ");
				}
				numFieldsAdded++;
				sb.append(f.getColumnName());
			}
		}
		sb.append(") VALUES (?");
		while(numFieldsAdded > 1) {
			sb.append(", ?");
			numFieldsAdded--;
		}
		sb.append(")");
		_sql = sb.toString();
	}

	public String getOriginalSQL() {
		return _sql;
	}

	public String getParsedSQL() {
		return _sql;
	}

	public int getParameterCount() {
		return getMetaData().getFields().size();
	}
	
	public long execute(Connection c, EntityInstance<?,?> ei) throws WrappedSQLException, ValidationException {
		Argument[] args = _prepareArgsForInsert(c, ei);
		PreparedQuery pq = new PreparedQuery(this, c, _sql, args, getMetaData().hasAutonumberKey());
		countDatabaseHit(c.getRepository());
		long results = pq.executeUpdate();
		pq.close();
		return results;
	}

	private Argument[] _prepareArgsForInsert(Connection c, EntityInstance<?,?> ei) throws ValidationException {
		int size = ei.getMetaData().getFields().size();
		if(ei.getMetaData().hasAutonumberKey()) {
			size--;
		}
		Iterator<Field> itFields = ei.getMetaData().getFields().iterator();
		Argument[] args = new Argument[size];
		for(int i=0; itFields.hasNext(); ) {
			Field f = itFields.next();
			Comparable<?> value = ei.getValue(f);
			// Check and skip any automatically generated primary keys:
			if(f.isAutonumber()) {
				if(value != null) {
					throw new IllegalArgumentException("attempted to insert a primary key for an autonumber field");
				}
				else {
					continue;
				}
			}
			// If the value has not been set, set it to the default value:
			if(value == null) {
				value = f.getDefault();
				if(value == null && f.isMandatory()) {
					throw new MandatoryFieldException(f);
				}
				else {
					value = ei.safelySetValue(f, value);
				}
			}
			// Add this value to the list of arguments for the insert:
			args[i++] = new Argument(f, value);
		}
		return args;
	}
}
