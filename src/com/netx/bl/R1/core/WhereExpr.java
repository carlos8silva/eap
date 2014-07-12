package com.netx.bl.R1.core;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import com.netx.generics.R1.collections.Collections;
import com.netx.generics.R1.util.Wildcards;
import com.netx.basic.R1.eh.IntegrityException;


public class WhereExpr {

	// TYPE:
	public static enum OPERATOR {
		AND,
		OR,
		EQUALS,
		DIFFERS,
		LIKE,
		BIGGER_THAN,
		SMALLER_THAN,
		BIGGER_THAN_EQUALS,
		SMALLER_THAN_EQUALS,
		IS_NULL,
		IS_NOT_NULL
	}
	
	public static String toSQL(Collection<? extends Field> fields) {
		StringBuffer sb = new StringBuffer();
		Iterator<? extends Field> it = fields.iterator();
		while(it.hasNext()) {
			sb.append(it.next().getColumnName());
			sb.append(" = ?");
			if(it.hasNext()) {
				sb.append(" AND ");
			}
		}
		return sb.toString();
	}

	public static WhereExpr toExpr(Collection<? extends Field> fields) {
		return _getExpr(fields.iterator());
	}

	private static WhereExpr _getExpr(Iterator<? extends Field> it) {
		Field f = it.next();
		WhereExpr comparison = new WhereExpr(f, WhereExpr.OPERATOR.EQUALS, null);
		if(it.hasNext()) {
			return new WhereExpr(comparison, WhereExpr.OPERATOR.AND, _getExpr(it));
		}
		else {
			return comparison;
		}
	}

	// INSTANCE:
	private final Method _getter;
	private final Object _left;
	private final OPERATOR _op;
	private final Object _right;
	
	public WhereExpr(Method getter, Object left, OPERATOR op, Object right) {
		_getter = getter;
		_left = left;
		_op = op;
		_right = right;
	}

	public WhereExpr(Object left, OPERATOR op, Object right) {
		this(null, left, op, right);
	}
	
	public boolean evaluate(Connection c, EntityInstance<?,?> ei, Argument[] args) throws BLException {
		// AND:
		if(_op.equals(OPERATOR.AND)) {
			if(!((WhereExpr)_right).evaluate(c, ei, args)) {
				return false;
			}
			else {
				return ((WhereExpr)_left).evaluate(c, ei, args);
			}
		}
		// OR:
		if(_op.equals(OPERATOR.OR)) {
			if(((WhereExpr)_right).evaluate(c, ei, args)) {
				return true;
			}
			else {
				return ((WhereExpr)_left).evaluate(c, ei, args);
			}
		}
		// Get the joined EI (if any):
		if(_getter != null) {
			try {
				ei = (EntityInstance<?,?>)_getter.invoke(ei, c);
			}
			catch(Exception e) {
				Throwable cause = e.getCause();
				if(cause instanceof BLException) {
					throw (BLException)cause;
				}
				throw new IntegrityException(e);
			}
		}
		// Primitive comparison:
		Field field = (Field)_left;
		Object value = _right;
		if(value == null) {
			// Get argument from input:
			// TODO ensure that the order of the args is correct and
			// we do not need to create a map (for better performance).
			Map<Field,Comparable<?>> tmp = Collections.toMap(args);
			value = tmp.get(field);
		}
		Comparable<?> eiVal = (Comparable<?>)ei.getValue(field, true);
		// A value coming from a cached entity instance can never be null:
		if(eiVal == null) {
			throw new IntegrityException(field);
		}
		// NULL comparisons:
		if(_op.equals(OPERATOR.IS_NULL)) {
			return value == Field.NULL;
		}
		if(_op.equals(OPERATOR.IS_NOT_NULL)) {
			return value != Field.NULL;
		}
		if(eiVal == Field.NULL) {
			if(_op.equals(OPERATOR.EQUALS)) {
				// value can never be 'null':
				return value == Field.NULL;
			}
			else {
				// < and > comparisons with NULL always return false:
				return false;
			}
		}
		// LIKE comparisons:
		if(_op.equals(OPERATOR.LIKE)) {
			String str = (String)value;
			str = str.replace('%', '*');
			// TODO create a version of Wildcards.compile that takes a "%" as the
			// wildcard char to avoid the previous replace and improve performance
			return Wildcards.compile(str, ((FieldText)field).ignoreCase()).matches((String)eiVal);
		}
		// Every other comparison:
		int result = Field.compare(field, eiVal, (Comparable<?>)value);
		if(_op.equals(OPERATOR.EQUALS)) {
			return result == 0;
		}
		else if(_op.equals(OPERATOR.DIFFERS)) {
			return result != 0;
		}
		// true if the entity value is bigger than 'value'
		else if(_op.equals(OPERATOR.BIGGER_THAN)) {
			return result > 0;
		}
		else if(_op.equals(OPERATOR.BIGGER_THAN_EQUALS)) {
			return result >= 0;
		}
		else if(_op.equals(OPERATOR.SMALLER_THAN)) {
			return result < 0;
		}
		else if(_op.equals(OPERATOR.SMALLER_THAN_EQUALS)) {
			return result <= 0;
		}
		else {
			throw new IntegrityException(_op);
		}
	}
}
