package com.netx.bl.R1.core;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.netx.basic.R1.shared.Constants;
import com.netx.basic.R1.eh.IntegrityException;


public final class Update extends Query {

	private final String _sql;
	private final Field[] _updateParams;
	private final Field[] _whereParams;

	// Constructor for UPDATE and DELETE entity statements:
	public Update(TYPE type, MetaData metaData) {
		super(type, type==TYPE.UPDATE ? "update-by-pk" : (type==TYPE.DELETE ? "delete-by-pk" : "truncate"), metaData, type==TYPE.TRUNCATE ? null : WhereExpr.toExpr(metaData.getPrimaryKeyFields()));
		if(type == TYPE.UPDATE) {
			_sql = null;
		}
		else if(type == TYPE.DELETE) {
			StringBuilder sb = new StringBuilder();
			sb = new StringBuilder();
			sb.append("DELETE FROM ");
			sb.append(metaData.getTableName());
			sb.append(" WHERE ");
			sb.append(WhereExpr.toSQL(getMetaData().getPrimaryKeyFields()));
			_sql = sb.toString();
		}
		else if(type == TYPE.TRUNCATE) {
			StringBuilder sb = new StringBuilder();
			sb = new StringBuilder();
			sb.append("TRUNCATE ");
			sb.append(metaData.getTableName());
			_sql = sb.toString();
		}
		else {
			throw new IntegrityException(type);
		}
		_updateParams = _whereParams = new Field[0];
	}

	// Constructor for static UPDATE or DELETE statements:
	public Update(TYPE type, String name, MetaData metaData, String sql, WhereExpr whereExpr, Field[] updateParams, Field[] whereParams) {
		super(type, name, metaData, whereExpr);
		_sql = sql;
		_updateParams = updateParams==null ? new Field[0] : updateParams;
		_whereParams = whereParams==null ? new Field[0] : whereParams;
	}

	public String getOriginalSQL() {
		return _sql;
	}

	public String getParsedSQL() {
		return getOriginalSQL();
	}

	public int getParameterCount() {
		return _updateParams.length + _whereParams.length;
	}

	// For Entity:
	public ValidatedArgument[] prepareUpdateArgs(Comparable<?> ... values) {
		if(values.length != getParameterCount()) {
			throw new IllegalArgumentException("wrong number of arguments: expected "+getParameterCount()+", found "+values.length);
		}
		ValidatedArgument[] args = new ValidatedArgument[_updateParams.length];
		for(int i=0; i<args.length; i++) {
			args[i] = new ValidatedArgument(_updateParams[i], values[i]);
		}
		return args;
	}
	
	// For Entity:
	public Argument[] prepareWhereArgs(Comparable<?> ... values) {
		// Note: no need to check arguments length here as they have been checked in prepareUpdateArgs
		Argument[] args = new Argument[_whereParams.length];
		for(int i=0; i<args.length; i++) {
			args[i] = new Argument(_whereParams[i], values[i + _updateParams.length]);
		}
		return args;
	}

	// For static UPDATE statements:
	public int execute(Connection c, Argument[] updateArgs, Argument[] whereArgs) throws WrappedSQLException {
		List<Argument> allArgs = new ArrayList<Argument>();
		for(Argument arg : updateArgs) {
			allArgs.add(arg);
		}
		for(Argument arg : whereArgs) {
			allArgs.add(arg);
		}
		Argument[] args = allArgs.toArray(whereArgs);
		return _execute(c, _sql, args);
	}

	public int execute(Connection c, EntityInstance<?,?> ei) throws WrappedSQLException {
		// Dynamically generate SQL according to the updates made to the EI:
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(ei.getMetaData().getTableName());
		sb.append(" SET ");
		Map<Field,Comparable<?>> mapUpdates = ei.getUpdatedFields();
		Iterator<Map.Entry<Field,Comparable<?>>> itUpdates = mapUpdates.entrySet().iterator();
		Argument[] args = new Argument[mapUpdates.size()];
		for(int i=0; itUpdates.hasNext(); i++) {
			Map.Entry<Field,Comparable<?>> entry = itUpdates.next();
			args[i] = new Argument(entry.getKey(), entry.getValue());
			// Validate values:
			Field f = entry.getKey();
			if(f.isReadOnly()) {
				throw new ReadOnlyFieldException(f);
			}
			sb.append(f.getColumnName());
			String value = Field.toSQL(entry.getValue(), c.getRepository().getDriver());
			if(value == null) {
				sb.append("=");
				sb.append(Constants.NULL_UC);
			}
			else {
				sb.append("='");
				sb.append(value);
				sb.append("'");
			}
			if(itUpdates.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(" WHERE ");
		// Build WHERE part based on the primary key:
		Iterator<Field> itPrimkey = ei.getMetaData().getPrimaryKeyFields().iterator();
		while(itPrimkey.hasNext()) {
			Field f = itPrimkey.next();
			sb.append(f.getColumnName());
			sb.append("='");
			sb.append(Field.toSQL(ei.getValue(f), c.getRepository().getDriver()));
			sb.append("'");
			if(itPrimkey.hasNext()) {
				sb.append(" AND ");
			}
		}
		return _execute(c, sb.toString(), null);
	}

	public int execute(Connection c, Argument[] args) throws WrappedSQLException {
		return _execute(c, _sql, args);
	}

	private int _execute(Connection c, String sql, Argument[] args) throws WrappedSQLException {
		PreparedQuery pq = new PreparedQuery(this, c, sql, args, false);
		countDatabaseHit(c.getRepository());
		long result = pq.executeUpdate();
		pq.close();
		return (int)result;
	}
}
