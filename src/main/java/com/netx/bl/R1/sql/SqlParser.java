package com.netx.bl.R1.sql;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import com.netx.bl.R1.core.Query;
import com.netx.generics.R1.translation.TranslationStep;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.Token;
import com.netx.generics.R1.translation.TYPE;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.eh.IntegrityException;


class SqlParser extends TranslationStep {

	private final String _queryName;
	private final Query.TYPE _type;
	
	public SqlParser(SqlScanner scanner, Query.TYPE type, String queryName) {
		super(scanner);
		_queryName = queryName;
		_type = type;
	}

	@SuppressWarnings("unchecked")
	public Object performWork(Object o, ErrorList el) {
		List<? extends Token> tokens = (List<? extends Token>)o;
		ListIterator<? extends Token> it = tokens.listIterator();
		try {
			Token t = it.next();
			String stmtType = t.getValue(true);
			if(_type == null) {
				if(stmtType.equals("select")) {
					return _parseSelect(tokens, it, el);
				}
				el.addError(t, "expected SELECT keyword (only SELECT is allowed for global queries), found: "+t);
				return null;
			}
			else if(_type == Query.TYPE.SELECT) {
				if(stmtType.equals("select")) {
					return _parseSelect(tokens, it, el);
				}
				el.addError(t, "expected SELECT keyword, found: "+t);
				return null;
			}
			else if(_type == Query.TYPE.UPDATE) {
				if(t.getValue(true).equals("update")) {
					return _parseUpdate(it, el);
				}
				else if(stmtType.equals("delete")) {
					return _parseDelete(it, el);
				}
				else if(stmtType.toLowerCase().equals("insert")) {
					el.addError(t, "INSERT statements are disabled for entities. Use Entity.insert instead");
					return null;
				}
				else if(stmtType.equals("truncate")) {
					el.addError(t, "TRUNCATE statements are disabled for entities. Use Entity.truncate instead");
					return null;
				}
				else {
					el.addError(t, "expected UPDATE, DELETE or TRUNCATE keyword, found: "+t);
					return null;
				}
			}
			else {
				throw new IntegrityException(_type);
			}
		}
		catch(NoSuchElementException nsee) {
			el.addError(it.previous(), "unexpected end of expression");
			return null;
		}
	}
	
	private SymbolQuery _parseSelect(List<? extends Token> tokens, ListIterator<? extends Token> it, ErrorList el) {
		SymbolSelect ss = new SymbolSelect();
		ss.queryName = _queryName;
		Token t = it.next();
		ss.originalSQL = t.getSource();
		if(!_isGlobalQuery()) {
			// Entity SELECT:
			if(t.getValue().equals("*")) {
				Token tmp = t;
				t = it.next();
				if(t.getValue().equals(".")) {
					el.addError(tmp, "expected identifier, found: "+tmp);
					return null;
				}
				SymbolField sf = new SymbolField();
				sf.pFieldName = tmp;
				ss.fields.add(sf);
			}
			else {
				// Parse fields:
				while(true) {
					SymbolField sf = _parseField(t, it, el);
					ss.fields.add(sf);
					t = it.next();
					if(t.getValue().equals(",")) {
						t = it.next();
					}
					else {
						break;
					}
				}
			}
			if(!t.getValue(true).equals("from")) {
				el.addError("expected FROM keyword, found: "+t);
				return null;
			}
			// Parse entities:
			while(true) {
				SymbolTable st = _parseTable(it, el);
				ss.from.add(st);
				// Query may end here:
				if(!it.hasNext()) {
					return ss;
				}
				t = it.next();
				if(!t.getValue().equals(",")) {
					break;
				}
			}
			// TODO support INNER, OUTER, LEFT
			if(t.getValue(true).equals("join")) {
				ss.join = new SymbolJoin();
				ss.join.table = _parseTable(it, el);
				t = it.next();
				if(!t.getValue(true).equals("on")) {
					el.addError(t, "expected ON keyword, found: "+t);
					return null;
				}
				ss.join.where = _parseWhere(it, el);
				if(ss.join.where == null) {
					return null;
				}
				// Query may end here:
				if(!it.hasNext()) {
					return ss;
				}
				t = it.next();
			}
			if(t.getValue(true).equals("where")) {
				ss.where = _parseWhere(it, el);
				// Query may end here:
				if(!it.hasNext()) {
					return ss;
				}
				t = it.next();
			}
		}
		else {
			// Global SELECT:
			// We only parse fields if there is a ORDER BY clause:
			ListIterator<? extends Token> itTmp = tokens.listIterator();
			int breakAt = -1;
			while(true) {
				Token tmp = itTmp.next();
				if(tmp.getValue(true).equals("order")) {
					tmp = itTmp.next();
					if(tmp.getValue(true).equals("by")) {
						break;
					}
				}
				if(tmp.getValue(true).equals("limit")) {
					breakAt = tmp.getIndex();
					ss.pLimit = _parseLimit(itTmp, el);
					if(ss.pLimit == null) {
						return null;
					}
					if(itTmp.hasNext()) {
						tmp = itTmp.next();
						el.addError(tmp, "unexpected token: "+tmp);
						return null;
					}
				}
				if(!itTmp.hasNext()) {
					if(breakAt == -1) {
						ss.pSQL = ss.originalSQL;
					}
					else {
						ss.pSQL = ss.originalSQL.substring(0, breakAt).trim();
					}
					ss.pNumParams = Strings.countOccurrences(ss.pSQL, '?');
					return ss;
				}
			}
			// If we got here, there is an ORDER BY clause and we need to keep parsing:
			if(t.getValue().equals("*")) {
				el.addError(t, "cannot select \"*\" in global queries, use entity query instead");
				return null;
			}
			// Parse fields:
			while(true) {
				Token tmp = it.next();
				it.previous();
				if(t.getValue(true).equals("case") || tmp.getValue().equals("(")) {
					while(true) {
						t = it.next();
						if(t.getValue(true).equals("as")) {
							t = it.next();
							break;
						}
						if(!it.hasNext()) {
							if(tmp.getValue().equals("(")) {
								el.addError(tmp, "function columns must be aliased");
							}
							else {
								el.addError(t, "unexpected end of expression");
							}
							return null;
						}
					}
				}
				SymbolField sf = _parseField(t, it, el);
				ss.fields.add(sf);
				t = it.next();
				if(t.getValue().equals(",")) {
					t = it.next();
				}
				else {
					break;
				}
			}
			breakAt = -1;
			Find:
			while(true) {
				t = it.next();
				if(t.getValue(true).equals("order")) {
					t = it.next();
					if(t.getValue(true).equals("by")) {
						it.previous();
						it.previous();
						t = it.next();
						breakAt = t.getIndex();
						break Find;
					}
				}
				if(t.getValue(true).equals("limit")) {
					breakAt = t.getIndex();
					break Find;
				}
				if(!it.hasNext()) {
					ss.pSQL = ss.originalSQL;
					ss.pNumParams = Strings.countOccurrences(ss.pSQL, '?');
					return ss;
				}
			}
			// Produce the parsed SQL (which excludes ORDER BY and/or LIMIT):
			ss.pSQL = t.getSource().substring(0, breakAt).trim();
			ss.pNumParams = Strings.countOccurrences(ss.pSQL, '?');
		}
		// ORDER BY clause:
		if(t.getValue(true).equals("order")) {
			t = it.next();
			if(!t.getValue(true).equals("by")) {
				el.addError(t, "unexpected token: "+t);
				return null;
			}
			while(true) {
				t = it.next();
				if(t.getType() != TYPE.IDENTIFIER) {
					el.addError(t, "expected identifier, found: "+t);
					return null;
				}
				SymbolOrderBy sob = new SymbolOrderBy();
				sob.field = _parseField(t, it, el);
				ss.pOrderBy.add(sob);
				// Query may end here:
				if(!it.hasNext()) {
					return ss;
				}
				t = it.next();
				if(t.getValue(true).equals("asc")) {
					sob.pOrder = t;
					// Query may end here:
					if(!it.hasNext()) {
						return ss;
					}
					t = it.next();
				}
				else if(t.getValue(true).equals("desc")) {
					sob.pOrder = t;
					// Query may end here:
					if(!it.hasNext()) {
						return ss;
					}
					t = it.next();
				}
				if(t.getValue().equals(",")) {
					continue;
				}
				if(t.getValue(true).equals("limit")) {
					break;
				}
				el.addError(t, "unexpected token: "+t);
				return null;
			}
		}
		// LIMIT clause:
		if(t.getValue(true).equals("limit")) {
			ss.pLimit = _parseLimit(it, el);
		}
		else {
			el.addError(t, "unexpected token: "+t);
			return null;
		}
		// Query should end here:
		if(it.hasNext()) {
			t = it.next();
			el.addError(t, "expected end of query, found: "+t);
			return null;
		}
		return ss;
	}

	private SymbolField _parseField(Token t, ListIterator<? extends Token> it, ErrorList el) {
		SymbolField sf = new SymbolField();
		if(!it.hasNext()) {
			if(t.getType() != TYPE.IDENTIFIER) {
				el.addError(t, "expected entity identifier, found: "+t);
				return null;
			}
			sf.pTableAlias = null;
			sf.pFieldName = t;
			return sf;
		}
		Token tmp = t;
		t = it.next();
		if(t.getValue().equals(".")) {
			if(tmp.getType() != TYPE.IDENTIFIER) {
				el.addError(t, "expected entity identifier, found: "+tmp);
				return null;
			}
			sf.pTableAlias = tmp;
			t = it.next();
			if(_isGlobalQuery() && t.getValue().equals("*")) {
				el.addError(t, "cannot select \"*\" in global queries, use entity query instead");
				return null;
			}
			if(t.getType() != TYPE.IDENTIFIER && !t.getValue().equals("*")) {
				el.addError(t, "expected alias identifier, found: "+t);
				return null;
			}
			sf.pFieldName = t;
		}
		else {
			if(tmp.getType() != TYPE.IDENTIFIER) {
				el.addError(t, "expected entity identifier, found: "+tmp);
				return null;
			}
			sf.pFieldName = tmp;
			it.previous();
		}
		return sf;
	}

	private SymbolTable _parseTable(ListIterator<? extends Token> it, ErrorList el) {
		Token t = it.next();
		if(t.getType() != TYPE.IDENTIFIER) {
			el.addError(t, "expected entity identifier, found: "+t);
			return null;
		}
		SymbolTable st = new SymbolTable();
		st.pTableName = t;
		// Query can end here:
		if(!it.hasNext()) {
			return st;
		}
		t = it.next();
		if(t.getType() == TYPE.IDENTIFIER) {
			st.pTableAlias = t;
		}
		else {
			it.previous();
		}
		return st;
	}

	private SymbolWhere _parseWhere(ListIterator<? extends Token> it, ErrorList el) {
		SymbolWhere left = new SymbolWhere();
		Token t = it.next();
		// Left:
		if(t.getValue().equals("(")) {
			left = _parseWhere(it, el);
			t = it.next();
			if(!t.getValue().equals(")")) {
				el.addError(t, "expected ')', found: "+t);
				return null;
			}
		}
		else {
			left = _parseExpr(t, it, el);
		}
		// End:
		if(!it.hasNext()) {
			return left;
		}
		t = it.next();
		if(t.getValue().equals(")") || t.getValue(true).equals("where") || t.getValue(true).equals("order") || t.getValue(true).equals("limit")) {
			it.previous();
			return left;
		}
		// Right:
		SymbolWhere top = new SymbolWhere();
		top.left = left;
		top.op = t;
		if(!top.op.getValue(true).equals("and") && !top.op.getValue(true).equals("or")) {
			el.addError(top.op, "unexpected token: "+top.op);
			return null;
		}
		top.right = _parseWhere(it, el);
		return top;
	}

	// TODO this also has to support parentheses
	private SymbolWhere _parseExpr(Token t, ListIterator<? extends Token> it, ErrorList el) {
		SymbolWhere sw = new SymbolWhere();
		// Left:
		sw.left = _parseSide(t, it, el);
		// Operator:
		sw.op = it.next();
		if((sw.op.getType() != TYPE.OPERATOR) && !sw.op.getValue(true).equals("is") && !sw.op.getValue(true).equals("like")) {
			el.addError(sw.op, "expected operator, found: "+sw.op);
			return null;
		}
		// Right:
		t = it.next();
		sw.right = _parseSide(t, it, el);
		return sw;
	}

	private Object _parseSide(Token t, ListIterator<? extends Token> it, ErrorList el) {
		SymbolExpr se = new SymbolExpr();
		se.pValue = t;
		if(t.getValue().equals("?")) {
			return se;
		}
		if(t.getValue(true).equals("null")) {
			return se;
		}
		if(t.getValue(true).equals("not")) {
			se.not = t;
			se.pValue = it.next();
			return se;
		}
		// Note: char constants are recognized as String.
		if(t.getType() == TYPE.STRING_CONSTANT) {
			return se;
		}
		if(t.getType() == TYPE.INTEGER_CONSTANT) {
			return se;
		}
		if(t.getType() == TYPE.FLOAT_CONSTANT) {
			return se;
		}
		if(t.getType() == TYPE.IDENTIFIER) {
			SymbolField sf = _parseField(t, it, el);
			return sf;
		}
		// Regular expression:
		if(t.getValue().equals("[")) {
			StringBuilder sb = new StringBuilder();
			while(true) {
				sb.append(t.getRawValue());
				t = it.next();
				if(t.getValue().equals("]")) {
					sb.append(t.getRawValue());
					se.pRegexp = sb.toString();
					return se;
				}
			}
		}
		el.addError(t, "unexpected token: "+t);
		return null;
	}
	
	private SymbolLimit _parseLimit(ListIterator<? extends Token> it, ErrorList el) {
		Token t = it.next();
		if(t.getType() != TYPE.INTEGER_CONSTANT && !t.getValue().equals("?")) {
			el.addError(t, "expected integer constant, found: "+t);
			return null;
		}
		SymbolLimit sl = new SymbolLimit();
		// Query may end here:
		if(!it.hasNext()) {
			sl.pRight = t;
			return sl;
		}
		sl.pLeft = t;
		t = it.next();
		if(!t.getValue().equals(",")) {
			el.addError(t, "expected ',', found: "+t);
			return null;
		}
		t = it.next();
		if(t.getType() != TYPE.INTEGER_CONSTANT && !t.getValue().equals("?")) {
			el.addError(t, "expected integer constant, found: "+t);
			return null;
		}
		sl.pRight = t;
		return sl;
	}

	private boolean _isGlobalQuery() {
		return _type == null;
	}

	private SymbolQuery _parseUpdate(ListIterator<? extends Token> it, ErrorList el) {
		SymbolUpdate su = new SymbolUpdate();
		su.queryName = _queryName;
		su.table = _parseTable(it, el);
		Token t = it.next();
		su.originalSQL = t.getSource();
		// Parse SET clause:
		if(!t.getValue(true).equals("set")) {
			el.addError("expected SET keyword, found: "+t);
			return null;
		}
		t = it.next();
		while(true) {
			SymbolSet ss = new SymbolSet();
			ss.field = _parseField(t, it, el);
			if(ss.field == null) {
				return null;
			}
			su.set.add(ss);
			t = it.next();
			if(!t.getValue().equals("=")) {
				el.addError("expected '=', found: "+t);
				return null;
			}
			t = it.next();
			ss.expr = _parseSide(t, it, el);
			// Query may end here:
			if(!it.hasNext()) {
				return su;
			}
			t = it.next();
			if(t.getValue().equals(",")) {
				t = it.next();
			}
			else {
				break;
			}
		}
		// Parse WHERE:
		if(t.getValue(true).equals("where")) {
			su.where = _parseWhere(it, el);
			// Query may end here:
			if(!it.hasNext()) {
				return su;
			}
			t = it.next();
		}
		// Query should end here:
		if(it.hasNext()) {
			t = it.next();
			el.addError(t, "expected end of query, found: "+t);
			return null;
		}
		return su;
	}
	
	private SymbolQuery _parseDelete(ListIterator<? extends Token> it, ErrorList el) {
		SymbolDelete sd = new SymbolDelete();
		sd.queryName = _queryName;
		Token t = it.next();
		sd.originalSQL = t.getSource();
		if(!t.getValue(true).equals("from")) {
			el.addError("expected FROM keyword, found: "+t);
			return null;
		}
		sd.table = _parseTable(it, el);
		// Query may end here:
		if(!it.hasNext()) {
			return sd;
		}
		t = it.next();
		// Parse WHERE:
		if(!t.getValue(true).equals("where")) {
			el.addError("expected WHERE keyword, found: "+t);
			return null;
		}
		sd.where = _parseWhere(it, el);
		// Query should end here:
		if(it.hasNext()) {
			t = it.next();
			el.addError(t, "expected end of query, found: "+t);
			return null;
		}
		return sd;
	}
}
