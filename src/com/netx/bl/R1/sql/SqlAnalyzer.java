package com.netx.bl.R1.sql;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.lang.reflect.Method;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.TYPE;
import com.netx.generics.R1.translation.Token;
import com.netx.generics.R1.translation.TranslationStep;
import com.netx.generics.R1.collections.IList;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.MetaData;
import com.netx.bl.R1.core.Entity;
import com.netx.bl.R1.core.Query;
import com.netx.bl.R1.core.Repository;
import com.netx.bl.R1.core.Field;
import com.netx.bl.R1.core.FieldForeignKey;
import com.netx.bl.R1.core.Select;
import com.netx.bl.R1.core.Update;
import com.netx.bl.R1.core.WhereExpr;
import com.netx.bl.R1.core.OrderBy;
import com.netx.bl.R1.core.Limit;
import com.netx.bl.R1.core.UniqueConstraint;
import com.netx.bl.R1.core.ValidationException;


class SqlAnalyzer extends TranslationStep {

	private final MetaData _metaData;
	private final Repository _rep;
	private SymbolQuery _sq;
	
	public SqlAnalyzer(SqlParser parser, MetaData metaData, Repository rep) {
		super(parser);
		_metaData = metaData;
		_rep = rep;
	}

	public Object performWork(Object o, ErrorList el) {
		_sq = (SymbolQuery)o;
		if(_sq instanceof SymbolSelect) {
			if(_metaData == null) {
				return _analyzeGlobal((SymbolSelect)_sq, el);
			}
			else {
				return _analyzeSelect((SymbolSelect)_sq, el);
			}
		}
		else if(_sq instanceof SymbolUpdate) {
			return _analyzeUpdate((SymbolUpdate)_sq, el);
		}
		else if(_sq instanceof SymbolDelete) {
			return _analyzeDelete((SymbolDelete)_sq, el);
		}
		else {
			throw new IntegrityException(_sq);
		}
	}
	
	private Select _analyzeSelect(SymbolSelect ss, ErrorList el) {
		// Check entities:
		if(ss.from.size() > 1) {
			el.addError(ss.from.get(1).getAlias(), "entity queries cannot refer to multiple tables");
			return null;
		}
		SymbolTable st = ss.from.get(0);
		Entity<?,?> e = _rep.getEntityByTableName(st.pTableName.getValue());
		if(e == null) {
			el.addError(st.pTableName, "could not find table '"+st.pTableName.getValue()+"'");
			return null;
		}
		st.aTable = e.getMetaData();
		if(st.aTable != _metaData) {
			el.addError(st.pTableName, st.pTableName.getValue()+": wrong entity for query. Please move query into '"+st.aTable.getName()+"'");
			return null;
		}
		// Check fields:
		if(ss.fields.size() > 1) {
			// Choose appropriate token to point the error to:
			SymbolField sf = ss.fields.get(1);
			Token t = sf.pFieldName;
			if(t.getValue().equals("*") && sf.pTableAlias != null) {
				t = sf.pTableAlias;
			}
			el.addError(t, "cannot name individual fields in entity queries (use SELECT * instead)");
			return null;
		}
		SymbolField sf = ss.fields.get(0);
		if(!sf.pFieldName.getValue().equals("*")) {
			el.addError(sf.pFieldName, "cannot name individual fields in entity queries (use SELECT * instead)");
			return null;
		}
		if(sf.pTableAlias != null) {
			// Ensure the table is in the FROM clause:
			SymbolTable sTmp = _getTable(sf.pTableAlias.getValue(true), ss.from.toArray(new SymbolTable[0]));
			if(sTmp == null) {
				el.addError(sf.pTableAlias, "alias '"+sf.pTableAlias.getValue()+"' could not be found in FROM clause");
				return null;
			}
			// Ensure we are selecting *:
			if(!sf.pFieldName.getValue().equals("*")) {
				el.addError(sf.pFieldName, "cannot name individual fields in entity queries (use SELECT *)");
				return null;
			}
		}
		// Check JOIN:
		Set<Field> joinedFields = new HashSet<Field>();
		if(ss.join != null) {
			// Check table name:
			e = _rep.getEntityByTableName(ss.join.table.pTableName.getValue());
			if(e == null) {
				el.addError(ss.join.table.pTableName, "could not find table '"+ss.join.table.pTableName.getValue()+"'");
				return null;
			}
			ss.join.table.aTable = e.getMetaData();
			// Check ON clause:
			ss.join.where.expr = _analyzeWhere(ss.join.where, false, el, st, ss.join.table);
			// Check if JOIN is based on foreign key to the other entity:
			boolean manyToOne = false;
			IList<Field> fieldList = st.aTable.getFields();
			for(Field f : fieldList) {
				if(f instanceof FieldForeignKey) {
					FieldForeignKey fk = (FieldForeignKey)f;
					if(fk.getForeignField().getOwner() == ss.join.table.aTable) {
						if(!_findJoinField(fk, ss.join.where)) {
							el.addError(st.pTableName, "join on '"+fk.getFullColumnName()+"' is required");
							return null;
						}
						// Keep track of what we found:
						manyToOne = true;
						joinedFields.add(fk);
						joinedFields.add(fk.getForeignField());
					}
				}
			}
			if(!manyToOne) {
				// Find correct error message:
				boolean oneToMany = false;
				For:
				for(MetaData linked : st.aTable.getLinkedEntities()) {
					if(linked == ss.join.table.aTable) {
						oneToMany = true;
						break For;
					}
				}
				if(oneToMany) {
					el.addError(st.pTableName, "cannot join to table '"+ss.join.table.aTable.getTableName()+"': one-to-many relationships are not supported");
				}
				else {
					el.addError(st.pTableName, "cannot join unrelated tables '"+st.pTableName+"' and '"+ss.join.table.pTableName+"'");
				}
				return null;
			}
		}
		// Check WHERE clause:
		if(ss.where != null) {
			ss.where.expr = _analyzeWhere(ss.where, true, el, st, ss.join == null ? null : ss.join.table);
		}
		// Check ORDER BY:
		for(SymbolOrderBy sob : ss.pOrderBy) {
			OrderBy ob = _analyzeEntityOrderBy(sob, el, st, ss.join == null ? null : ss.join.table);
			if(ob != null) {
				ss.aOrderBy.add(ob);
			}
		}
		// Check LIMIT:
		if(ss.pLimit != null) {
			ss.aLimit = _analyzeLimit(ss.pLimit);
		}
		// Check if query returns a unique result:
		Set<Field> whereFields = _listFieldsFromWhere(ss.where);
		if(whereFields != null) {
			whereFields.removeAll(joinedFields);
		}
		boolean uniqueResult = _checkIfUniqueResult(ss.from.get(0).aTable, whereFields);
		// Create Select object:
		OrderBy[] orderBy = ss.aOrderBy.isEmpty() ? null : ss.aOrderBy.toArray(new OrderBy[0]);
		return new Select(ss.queryName, ss.from.get(0).aTable, ss.originalSQL, ss.toSQL(), ss.where==null ? null : ss.where.expr, orderBy, ss.aLimit, ss.whereParams.toArray(new Field[0]), uniqueResult);
	}

	private Select _analyzeGlobal(SymbolSelect ss, ErrorList el) {
		// Check ORDER BY (ensure columns exist in the SELECT clause):
		for(SymbolOrderBy sob : ss.pOrderBy) {
			OrderBy ob = _analyzeGlobalOrderBy(sob, el, ss.fields);
			if(ob != null) {
				ss.aOrderBy.add(ob);
			}
		}
		// Check LIMIT:
		if(ss.pLimit != null) {
			ss.aLimit = _analyzeLimit(ss.pLimit);
		}
		// Create Select object:
		OrderBy[] orderBy = ss.aOrderBy.isEmpty() ? null : ss.aOrderBy.toArray(new OrderBy[0]);
		return new Select(ss.queryName, ss.originalSQL, ss.toSQL(), orderBy, ss.aLimit, ss.pNumParams);
	}

	private WhereExpr _analyzeWhere(SymbolWhere sw, boolean initGetter, ErrorList el, SymbolTable ... tables) {
		if(sw.op.getValue(true).equals("and") || sw.op.getValue(true).equals("or")) {
			WhereExpr left = _analyzeWhere((SymbolWhere)sw.left, initGetter, el, tables);
			WhereExpr right = _analyzeWhere((SymbolWhere)sw.right, initGetter, el, tables);
			if(sw.op.getValue(true).equals("and")) {
				return new WhereExpr(left, WhereExpr.OPERATOR.AND, right);
			}
			if(sw.op.getValue(true).equals("or")) {
				return new WhereExpr(left, WhereExpr.OPERATOR.OR, right);
			}
			throw new IntegrityException(sw.op);
		}
		else {
			Object left = _analyzeExpr(sw.left, initGetter, el, tables);
			Object right = _analyzeExpr(sw.right, initGetter, el, tables);
			// Operator:
			WhereExpr.OPERATOR op = null;
			if(sw.op.getValue().equals("=")) {
				op = WhereExpr.OPERATOR.EQUALS;
			}
			else if(sw.op.getValue().equals("<>")) {
				op = WhereExpr.OPERATOR.DIFFERS;
			}
			else if(sw.op.getValue().equals("!=")) {
				op = WhereExpr.OPERATOR.DIFFERS;
			}
			else if(sw.op.getValue(true).equals("like")) {
				op = WhereExpr.OPERATOR.LIKE;
			}
			else if(sw.op.getValue().equals(">")) {
				op = WhereExpr.OPERATOR.BIGGER_THAN;
			}
			else if(sw.op.getValue().equals("<")) {
				op = WhereExpr.OPERATOR.SMALLER_THAN;
			}
			else if(sw.op.getValue().equals(">=")) {
				op = WhereExpr.OPERATOR.BIGGER_THAN_EQUALS;
			}
			else if(sw.op.getValue().equals("<=")) {
				op = WhereExpr.OPERATOR.SMALLER_THAN_EQUALS;
			}
			else if(sw.op.getValue(true).equals("is")) {
				SymbolExpr expr = (SymbolExpr)sw.right;
				if(expr.not == null) {
					op = WhereExpr.OPERATOR.IS_NULL;
				}
				else {
					op = WhereExpr.OPERATOR.IS_NOT_NULL;
				}
			}
			else {
				throw new IntegrityException(sw.op);
			}
			if(el.hasErrors()) {
				return null;
			}
			return _checkDataTypes(left, op, right, el);
		}
	}

	private Object _analyzeExpr(Object expr, boolean initGetter, ErrorList el, SymbolTable ... tables) {
		if(expr instanceof SymbolField) {
			SymbolField sf = (SymbolField)expr;
			sf.aField = _analyzeField(sf, el, tables);
			if(sf.aField == null) {
				return null;
			}
			// Initialize a getter method if we have a join:
			if(initGetter && tables.length > 1) {
				if(sf.aField.getOwner() != tables[0].aTable) {
					try {
						sf.aGetter = tables[0].aTable.getInstanceClass().getMethod("get"+tables[1].aTable.getInstanceClass().getSimpleName(), new Class[]{Connection.class});
					}
					catch(Exception e) {
						el.addError(sf.pFieldName, "could not initialize field getter: "+e.getMessage());
						return null;
					}
				}
			}
		}
		// Note: SymbolExpr is analyzed in _checkDataTypes(SymbolField,SymbolExpr,ErrorList)
		return expr;
	}

	private Field _analyzeField(SymbolField sf, ErrorList el, SymbolTable ... tables) {
		// Find field:
		if(sf.pTableAlias == null) {
			List<Field> found = new ArrayList<Field>();
			String fieldName = sf.pFieldName.getValue();
			for(SymbolTable st : tables) {
				if(st != null) {
					Field f = st.aTable.getFieldByColumnName(fieldName);
					if(f != null) {
						found.add(f);
					}
				}
			}
			if(found.isEmpty()) {
				el.addError(sf.pFieldName, "could not find field '"+fieldName+"'");
				return null;
			}
			if(found.size() > 1) {
				el.addError(sf.pFieldName, "field '"+fieldName+"' is ambiguous");
				return null;
			}
			return found.get(0);
		}
		else {
			SymbolTable st = _getTable(sf.pTableAlias.getValue(true), tables);
			if(st == null) {
				el.addError(sf.pTableAlias, "could not find alias '"+sf.pTableAlias.getValue()+"'");
				return null;
			}
			Field f = st.aTable.getFieldByColumnName(sf.pFieldName.getValue());
			if(f == null) {
				el.addError(sf.pFieldName, "could not find column '"+sf.toString()+"'");
				return null;
			}
			return f;
		}
	}

	private Limit _analyzeLimit(SymbolLimit sl) {
		Integer left = null, right = null;
		if(sl.pLeft == null) {
			left = new Integer(0);
		}
		else {
			if(!sl.pLeft.getValue().equals("?")) {
				left = new Integer(sl.pLeft.getValue());
			}
		}
		if(!sl.pRight.getValue().equals("?")) {
			right = new Integer(sl.pRight.getValue());
		}
		return new Limit(left, right);
	}

	// TODO improve error reporting (currently it just prints out the affected enum's name)
	private WhereExpr _checkDataTypes(Object left, WhereExpr.OPERATOR op, Object right, ErrorList el) {
		if(left instanceof SymbolField) {
			SymbolField sfLeft = (SymbolField)left;
			if(right instanceof SymbolField) {
				SymbolField sfRight = (SymbolField)right;
				if(!_checkDataTypes(sfLeft, sfRight, el)) {
					return null;
				}
			}
			else {
				SymbolExpr seRight = (SymbolExpr)right;
				if(!_checkDataTypes(sfLeft, seRight, el)) {
					return null;
				}
			}
		}
		else if(left instanceof SymbolExpr) {
			SymbolExpr seLeft = (SymbolExpr)left;
			if(right instanceof SymbolField) {
				SymbolField sfRight = (SymbolField)right;
				if(!_checkDataTypes(sfRight, seLeft, el)) {
					return null;
				}
			}
			else {
				SymbolExpr seRight = (SymbolExpr)right;
				if(!_checkDataTypes(seLeft, seRight, el)) {
					return null;
				}
			}
		}
		else {
			throw new IntegrityException(left);
		}
		return new WhereExpr(_findGetter(left, right), _convert(left), op, _convert(right));
	}

	// For Field to Field comparisons:
	private boolean _checkDataTypes(SymbolField sf1, SymbolField sf2, ErrorList el) {
		if(sf1.aField.resolveType() != sf2.aField.resolveType()) {
			el.addError(sf1.pFieldName, "in column '"+sf2+"': expected type "+sf1.aField.resolveType()+", found: "+sf2.aField.resolveType());
			return false;
		}
		return true;
	}
	
	// For Field to Expr comparisons:
	private boolean _checkDataTypes(SymbolField sf, SymbolExpr se, ErrorList el) {
		String value = se.pRegexp != null ? se.pRegexp : se.pValue.getValue();
		if(value.equals("?")) {
			se.aValue = null;
			_sq.whereParams.add(sf.aField);
			return true;
		}
		if(value.toLowerCase().equals("null")) {
			se.aValue = Field.NULL;
			return true;
		}
		try {
			se.aValue = sf.aField.toObject(value, null);
		}
		catch(ValidationException wfe) {
			Field.TYPE type = sf.aField.getType();
			if(type == Field.TYPE.FOREIGN_KEY) {
				FieldForeignKey fk = (FieldForeignKey)sf.aField;
				type = fk.getForeignField().getType();
			}
			el.addError(se.pValue, "expected "+type+" constant for field '"+sf.aField.getFullColumnName()+"', found '"+value+"'");
			return false;
		}
		// Extra comparisons:
		if(sf.aField.resolveType() == Field.TYPE.TEXT || sf.aField.resolveType() == Field.TYPE.BINARY) {
			if(se.pValue.getType() != TYPE.STRING_CONSTANT) {
				el.addError(se.pValue, "expected "+sf.aField.resolveType()+", found: '"+value+"'");
				return false;
			}
			se.aValue = value;
		}
		return true;
	}

	// For Expr to Expr comparisons:
	private boolean _checkDataTypes(SymbolExpr se1, SymbolExpr se2, ErrorList el) {
		// TODO
		return true;
	}

	private Method _findGetter(Object ... array) {
		for(Object o : array) {
			if(o instanceof SymbolField) {
				SymbolField sf = (SymbolField)o;
				if(sf.aGetter != null) {
					return sf.aGetter;
				}
			}
		}
		return null;
	}

	private Object _convert(Object o) {
		if(o instanceof SymbolField) {
			SymbolField sf = (SymbolField)o;
			return sf.aField;
		}
		else if(o instanceof SymbolExpr) {
			SymbolExpr se = (SymbolExpr)o;
			return se.aValue;
		}
		else {
			throw new IntegrityException(o);
		}
	}

	private SymbolTable _getTable(String tableName, SymbolTable[] from) {
		for(SymbolTable st : from) {
			if(st.pTableAlias != null) {
				if(st.pTableAlias.getValue(true).equals(tableName)) {
					return st;
				}
			}
			else {
				if(st.pTableName.getValue(true).equals(tableName)) {
					return st;
				}
			}
		}
		return null;
	}

	private boolean _findJoinField(FieldForeignKey fk, SymbolWhere sw) {
		// We only look at EQUALS operations:
		if(sw.op.getValue().equals("=")) {
			// We only look at expressions where we are comparing two columns
			if(sw.left instanceof SymbolField && sw.right instanceof SymbolField) {
				SymbolField left = (SymbolField)sw.left;
				SymbolField right = (SymbolField)sw.right;
				if(left.aField == fk && right.aField == fk.getForeignField()) {
					return true;
				}
				if(right.aField == fk && left.aField == fk.getForeignField()) {
					return true;
				}
			}
		}
		else {
			if(sw.op.getValue(true).equals("and") || sw.op.getValue(true).equals("or")) {
				if(_findJoinField(fk, (SymbolWhere)sw.left)) {
					return true;
				}
				if(_findJoinField(fk, (SymbolWhere)sw.right)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private OrderBy _analyzeEntityOrderBy(SymbolOrderBy sob, ErrorList el, SymbolTable ... tables) {
		OrderBy.ORDER order = sob.pOrder == null ? OrderBy.ORDER.ASC : OrderBy.ORDER.valueOf(sob.pOrder.getValue().toUpperCase());
		sob.field.aField = _analyzeField(sob.field, el, tables);
		if(sob.field.aField == null) {
			return null;
		}
		return new OrderBy(sob.field.aField, order);
	}

	private OrderBy _analyzeGlobalOrderBy(SymbolOrderBy sob, ErrorList el, List<SymbolField> fields) {
		OrderBy.ORDER order = sob.pOrder == null ? OrderBy.ORDER.ASC : OrderBy.ORDER.valueOf(sob.pOrder.getValue().toUpperCase());
		int index = _findField(sob.field, fields, el);
		if(index == -1) {
			return null;
		}
		return new OrderBy(index+1, order);
	}

	private int _findField(SymbolField sf, List<SymbolField> fields, ErrorList el) {
		List<Integer> found = new ArrayList<Integer>();
		for(int i=0; i<fields.size(); i++) {
			if(fields.get(i).pFieldName.getValue(true).equals(sf.pFieldName.getValue(true))) {
				found.add(i);
			}
		}
		if(found.isEmpty()) {
			return -1;
		}
		if(found.size() == 1) {
			return found.get(0);
		}
		if(sf.pTableAlias == null) {
			el.addError(sf.pFieldName, "ambiguous column name: '"+sf.pFieldName+"'");
			return -1;
		}
		for(Integer index : found) {
			SymbolField tmp = fields.get(index);
			if(sf.pTableAlias.getValue().equals(tmp.pTableAlias.getValue())) {
				return index;
			}
		}
		el.addError(sf.pFieldName, "could not find column '"+sf.pFieldName+"'");
		return -1;
	}

	private boolean _checkIfUniqueResult(MetaData m, Set<Field> fields) {
		if(fields == null) {
			return false;
		}
		_findConstraint(fields, m.getPrimaryKeyFields());
		if(fields.isEmpty()) {
			return true;
		}
		List<UniqueConstraint> c = m.getUniqueConstraints();
		for(UniqueConstraint uc : c) {
			_findConstraint(fields, uc.getFields());
			if(fields.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	private Set<Field> _listFieldsFromWhere(SymbolWhere sw) {
		if(sw == null) {
			return null;
		}
		if(sw.op.getValue(true).equals("and") || sw.op.getValue().equals("=")) {
			Set<Field> result = null;
			// Left:
			if(sw.left instanceof SymbolWhere) {
				result = _listFieldsFromWhere((SymbolWhere)sw.left);
			}
			else if(sw.left instanceof SymbolField) {
				SymbolField sf = (SymbolField)sw.left;
				result = new HashSet<Field>();
				result.add(sf.aField);
			}
			else if(sw.left instanceof SymbolExpr) {
				SymbolExpr se = (SymbolExpr)sw.left;
				if(se.aValue == Field.NULL) {
					return null;
				}
			}
			else {
				throw new IntegrityException(sw.left);
			}
			if(result == null) {
				return null;
			}
			// Right:
			if(sw.right instanceof SymbolWhere) {
				Set<Field> tmp = _listFieldsFromWhere((SymbolWhere)sw.right);
				if(tmp == null) {
					return null;
				}
				result.addAll(tmp);
			}
			else if(sw.right instanceof SymbolField) {
				SymbolField sf = (SymbolField)sw.right;
				result.add(sf.aField);
			}
			else if(sw.right instanceof SymbolExpr) {
				SymbolExpr se = (SymbolExpr)sw.right;
				if(se.aValue == Field.NULL) {
					return null;
				}
			}
			else {
				throw new IntegrityException(sw.right);
			}
			return result;
		}
		return null;
	}
	
	private void _findConstraint(Set<Field> fields, Collection<Field> constraint) {
		for(Field f : constraint) {
			if(!fields.contains(f)) {
				return;
			}
		}
		fields.removeAll(constraint);
	}
	
	public Update _analyzeUpdate(SymbolUpdate su, ErrorList el) {
		// Check entity:
		Entity<?,?> e = _rep.getEntityByTableName(su.table.pTableName.getValue());
		if(e == null) {
			el.addError(su.table.pTableName, "could not find table '"+su.table.pTableName.getValue()+"'");
			return null;
		}
		su.table.aTable = e.getMetaData();
		if(su.table.aTable != _metaData) {
			el.addError(su.table.pTableName, su.table.pTableName.getValue()+": wrong entity for query. Please move query into '"+su.table.aTable.getName()+"'");
			return null;
		}
		// Check SET clause:
		for(SymbolSet ss : su.set) {
			ss.field.aField = _analyzeField(ss.field, el, su.table);
			if(ss.expr != null && !(ss.expr instanceof SymbolExpr)) {
				// TODO right now we do not support SET clause where we set one field to the value of another field
				// (we just accept "?" parameters or constants), however this needs to be supported in the future.
				throw new IntegrityException(ss.expr);
			}
			SymbolExpr se = (SymbolExpr)ss.expr;
			_checkDataTypes(ss.field, se, el);
		}
		su.updateParams.addAll(su.whereParams);
		su.whereParams.clear();
		// Check WHERE clause:
		if(su.where != null) {
			su.where.expr = _analyzeWhere(su.where, true, el, su.table);
		}
		return new Update(Query.TYPE.UPDATE, su.queryName, _metaData, su.originalSQL, su.where == null ? null : su.where.expr, su.updateParams.toArray(new Field[0]), su.whereParams.toArray(new Field[0]));
	}

	public Update _analyzeDelete(SymbolDelete sd, ErrorList el) {
		// Check entity:
		Entity<?,?> e = _rep.getEntityByTableName(sd.table.pTableName.getValue());
		if(e == null) {
			el.addError(sd.table.pTableName, "could not find table '"+sd.table.pTableName.getValue()+"'");
			return null;
		}
		sd.table.aTable = e.getMetaData();
		if(sd.table.aTable != _metaData) {
			el.addError(sd.table.pTableName, sd.table.pTableName.getValue()+": wrong entity for query. Please move query into '"+sd.table.aTable.getName()+"'");
			return null;
		}
		// Check WHERE clause:
		if(sd.where != null) {
			sd.where.expr = _analyzeWhere(sd.where, true, el, sd.table);
		}
		return new Update(Query.TYPE.DELETE, sd.queryName, _metaData, sd.originalSQL, sd.where == null ? null : sd.where.expr, null, sd.whereParams.toArray(new Field[0]));
	}
}
