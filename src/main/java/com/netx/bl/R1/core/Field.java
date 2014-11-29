package com.netx.bl.R1.core;
import com.netx.bl.R1.spi.DatabaseDriver;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.Time;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Expr;
import com.netx.generics.R1.util.Strings;


public abstract class Field {

	// TYPE:
	private final static String _NULL_STR = "Field.NULL";
	private final static String _BOOLEAN_TRUE = "T";
	private final static String _BOOLEAN_FALSE = "F";
	
	public static final Comparable<?> NULL = new Comparable<Object>() {
		public int compareTo(Object o) {
			if(o == this) {
				return 0;
			}
			else {
				// Note: the comparison return value will not be used for sorting.
				// Sorting of NULL values is done according to the way that the
				// RDBMS handles sorting of NULL values. See Sorter for details.
				return -1;
			}
		}
		
		public String toString() {
			return _NULL_STR;
		}
	};
	
	public static enum TYPE {
		BOOLEAN,
		CHAR,
		BYTE,
		SHORT,
		INT,
		LONG,
		FLOAT,
		DOUBLE,
		TEXT,
		BINARY,
		DATE,
		TIME,
		DATETIME,
		FOREIGN_KEY
	}
	
	// For PreparedQuery:
	@SuppressWarnings("unchecked")
	static String toSQL(Object value, DatabaseDriver driver) {
		if(value == null) {
			return null;
		}
		if(value.equals(Field.NULL)) {
			return null;
		}
		if(value instanceof Boolean) {
			if(((Boolean)value).booleanValue()) {
				return _BOOLEAN_TRUE;
			}
			else {
				return _BOOLEAN_FALSE;
			}
		}
		if(value instanceof Timestamp) {
			return ((Timestamp)value).format(driver.getDateTimeFormat());
		}
		if(value instanceof Date) {
			return ((Date)value).format(driver.getDateFormat());
		}
		if(value instanceof Time) {
			return ((Time)value).format(driver.getTimeFormat());
		}
		if(value instanceof AllowedValue) {
			return ((AllowedValue<Comparable<?>>)value).getCode().toString();
		}
		return Strings.addSlashes(value.toString());
	}

	// For WhereExpr:
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compare(Field f, Comparable c1, Comparable c2) {
		if(f.getType().equals(TYPE.TEXT)) {
			if(((FieldText)f).ignoreCase()) {
				String s1 = c1.toString();
				String s2 = c2.toString();
				if(s1.equalsIgnoreCase(s2)) {
					return 0;
				}
				else {
					return s1.compareTo(s2);
				}
			}
		}
		if(f.getType().equals(TYPE.CHAR)) {
			if(((FieldChar)f).ignoreCase()) {
				Character char1 = (Character)c1;
				Character char2 = (Character)c2;
				char1 = Character.toLowerCase(char1);
				char2 = Character.toLowerCase(char2);
				return char1.compareTo(char2);
			}
		}
		return c1.compareTo(c2);
	}

	// INSTANCE:
	private final TYPE _type;
	private final MetaData _owner;
	private final String _name;
	private final String _columnName;
	private final Comparable<?> _default;
	private final boolean _mandatory;
	private final boolean _readOnly;
	private int _hashCode;
	private String _toString;

	public Field(TYPE type, MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly) {
		_type = type;
		_owner = owner;
		_name = name;
		_columnName = columnName;
		_default = defaultValue;
		_mandatory = mandatory;
		_readOnly = readOnly;
		_hashCode = -1;
		_toString = null;
	}

	// Throws WrongFormatException if the value has the wrong format,
	// and SizeExceededException if any size limit has been exceeded.
	// This is guaranteed to be called only when value != null and a String.
	public abstract Comparable<?> toObject(String value, DatabaseDriver driver) throws WrongFormatException, SizeExceededException;
	// Throws ClassCastException if the value is of an unexpected type.
	// This is guaranteed to be called only when value != null and a String.
	protected abstract void checkType(Object value);

	public String getName() {
		return _name;
	}

	public String getColumnName() {
		return _columnName;
	}
	
	public String getFullFieldName() {
		return getOwner().getName()+"."+getName();
	}

	public String getFullColumnName() {
		return getOwner().getTableName()+"."+getColumnName();
	}

	public TYPE getType() {
		return _type;
	}

	public TYPE resolveType() {
		if(_type == TYPE.FOREIGN_KEY) {
			return ((FieldForeignKey)this).getForeignField().getType();
		}
		return getType();
	}
	
	public MetaData getOwner() {
		return _owner;
	}
	
	public boolean isMandatory() {
		return _mandatory;
	}

	public boolean isReadOnly() {
		return _readOnly;
	}

	public boolean isAutonumber() {
		return false;
	}

	public Comparable<?> getDefault() {
		if(_default == null) {
			return null;
		}
		if(_default.getClass() == String.class) {
			return (Comparable<?>)Expr.evaluate(_default.toString());
		}
		return _default;
	}

	public Comparable<?> validate(Comparable<?> value) throws ValidationException {
		if(value == null || value == Field.NULL) {
			// Check if attempting to set a mandatory value to null:
			if(isMandatory()) {
				throw new MandatoryFieldException(this);
			}
			return NULL;
		}
		else {
			if(value.getClass() == String.class) {
				// If the field is a String, parse it:
				// (this may result in a WrongFormatException or SizeExceededException)
				value = toObject((String)value, null);
			}
			else {
				// Otherwise, make sure that the type is correct:
				checkType(value);
			}
			// If the field has a validation expression, use it:
			ValidationExpr expr = getValidationExpr();
			if(expr != null) {
				expr.validate(value);
			}
			// If the field has a validator, use it:
			Validator validator = getValidator();
			if(validator != null) {
				String msg = validator.validate(this, value);
				if(msg != null) {
					throw new WrongFormatException(msg);
				}
			}
			return value;
		}
	}

	protected ValidationExpr getValidationExpr() {
		return null;
	}

	protected Validator getValidator() {
		return null;
	}

	// Comparison between two fields yields equal if they:
	// a) Are exactly the same field
	// b) Are a field and a foreign key to the field
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(o == this) {
			return true;
		}
		if(o instanceof FieldForeignKey) {
			FieldForeignKey fk = (FieldForeignKey)o;
			return equals(fk.getForeignField());
		}
		return false;
	}

	public int hashCode() {
		if(_hashCode == -1) {
			_hashCode = 7;
			_hashCode = 31 * _hashCode + getOwner().hashCode();
			_hashCode = 31 * _hashCode + getName().hashCode();
		}
		return _hashCode;
	}

	public String toString() {
		if(_toString == null) {
			// Format: [Field name=<name> type=<type> owner=<entity-name>]
			StringBuilder sb = new StringBuilder();
			sb.append("[Field name=");
			sb.append(getName());
			sb.append(" type=");
			sb.append(getType());
			sb.append(" owner=");
			sb.append(getOwner().getName());
			sb.append("]");
			_toString = sb.toString();
		}
		return _toString;
	}
}
