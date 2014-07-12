package com.netx.bl.R1.sql;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import com.netx.bl.R1.core.MetaData;
import com.netx.bl.R1.core.Field;
import com.netx.bl.R1.core.OrderBy;
import com.netx.bl.R1.core.Limit;


public class SymbolSelect extends SymbolQuery {

	// Parsing:
	public final List<SymbolField> fields = new ArrayList<SymbolField>();
	public SymbolJoin join = null;
	public final List<SymbolTable> from = new ArrayList<SymbolTable>();
	public final List<SymbolOrderBy> pOrderBy = new ArrayList<SymbolOrderBy>();
	public SymbolLimit pLimit = null;
	// For Global queries:
	public String pSQL = null;
	public Integer pNumParams = null;
	// Analysis:
	public final List<OrderBy> aOrderBy = new ArrayList<OrderBy>();
	public Limit aLimit;
	
	public String toSQL() {
		StringBuilder sb = new StringBuilder();
		if(pSQL == null) {
			// Entity queries:
			boolean addEntityName = (join != null);
			MetaData m = from.get(0).aTable;
			sb.append("SELECT ");
			// Fields:
			Iterator<Field> itFields = m.getFields().iterator(); 
			while(itFields.hasNext()) {
				Field field = itFields.next();
				if(addEntityName) {
					sb.append(m.getTableName());
					sb.append('.');
				}
				sb.append(field.getColumnName());
				if(itFields.hasNext()) {
					sb.append(", ");
				}
			}
			// FROM:
			sb.append(" FROM ");
			sb.append(m.getTableName());
			// JOIN:
			if(join != null) {
				sb.append(" JOIN ");
				sb.append(join.table.aTable.getTableName());
				sb.append(" ON ");
				sb.append(join.where.toSQL(addEntityName));
			}
			// WHERE:
			if(where != null) {
				sb.append(" WHERE ");
				sb.append(where.toSQL(addEntityName));
			}
			// ORDER BY:
			// Note: we only add the ORDER BY clause if LIMIT is in the query as well:
			if(!pOrderBy.isEmpty() && pLimit != null) {
				sb.append(" ORDER BY ");
				Iterator<SymbolOrderBy> itOrderBy = pOrderBy.iterator();
				while(itOrderBy.hasNext()) {
					SymbolOrderBy sob = itOrderBy.next();
					sb.append(sob.toSQL(addEntityName));
					if(itOrderBy.hasNext()) {
						sb.append(", ");
					}
				}
			}
		}
		else {
			// Global queries:
			sb.append(pSQL);
			// ORDER BY:
			// Note: we only add the ORDER BY clause if LIMIT is in the query as well:
			if(!pOrderBy.isEmpty() && pLimit != null) {
				sb.append(" ORDER BY ");
				Iterator<SymbolOrderBy> itOrderBy = pOrderBy.iterator();
				while(itOrderBy.hasNext()) {
					SymbolOrderBy sob = itOrderBy.next();
					sb.append(sob.toString());
					if(itOrderBy.hasNext()) {
						sb.append(", ");
					}
				}
			}
		}
		// LIMIT:
		// We do not add the LIMIT clause yet. This is done at runtime,
		// if the database driver supports the LIMIT clause.
		return sb.toString();
	}
}
