package com.netx.bl.R1.core;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.netx.generics.R1.collections.IList;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;


public abstract class MetaData {

	// TYPE:
	public static enum DATA_TYPE {
		REFERENCE,
		TRANSACTIONAL
	}
	
	// INSTANCE:
	private final String _name;
	private final String _tableName;
	private final List<Field> _primKey;
	private final List<Field> _fields;
	private final Map<String,Field> _fieldsByCol;
	private final List<MetaData> _linkedEntities;
	private final List<UniqueConstraint> _unique;
	
	protected MetaData(String name, String tableName) {
		_name = name;
		_tableName = tableName;
		_primKey = new ArrayList<Field>();
		_fields = new ArrayList<Field>();
		_fieldsByCol = new HashMap<String,Field>();
		_linkedEntities = new ArrayList<MetaData>();
		_unique = new ArrayList<UniqueConstraint>();
	}

	public abstract Class<?> getInstanceClass();
	public abstract DATA_TYPE getDataType();
	public abstract Field getAutonumberKeyField();

	public final String getName() {
		return _name;
	}

	public final String getTableName() {
		return _tableName;
	}

	public final boolean hasAutonumberKey() {
		return getAutonumberKeyField() != null;
	}

	public final IList<Field> getPrimaryKeyFields() {
		return new IList<Field>(_primKey);
	}

	// Note: fields MUST be returned as a List.
	// Order matters for the automated EI constructors.
	public final IList<Field> getFields() {
		return new IList<Field>(_fields);
	}

	public final Field getField(String name) {
		Checker.checkEmpty(name, "name");
		for(Field f : getFields()) {
			if(f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
	
	public final Field getFieldByColumnName(String name) {
		Checker.checkEmpty(name, "name");
		return _fieldsByCol.get(name);
	}

	public final IList<UniqueConstraint> getUniqueConstraints() {
		return new IList<UniqueConstraint>(_unique);
	}

	protected void addField(Field f) {
		if(_fields.contains(f)) {
			throw new IntegrityException(f);
		}
		if(_fieldsByCol.get(f.getColumnName()) != null) {
			throw new IntegrityException(f);
		}
		_fields.add(f);
		_fieldsByCol.put(f.getColumnName(), f);
	}

	protected void addPrimaryKeyField(Field f) {
		if(_primKey.contains(f)) {
			throw new IntegrityException(f);
		}
		_primKey.add(f);
		addField(f);
	}

	protected void addUnique(Field ... fields) {
		_unique.add(new UniqueConstraint(fields));
	}

	// For FieldForeignKey:
	void addLinkedEntity(MetaData md) {
		_linkedEntities.add(md);
	}

	// For Connection, Transaction and SqlAnalyzer:
	public IList<MetaData> getLinkedEntities() {
		return new IList<MetaData>(_linkedEntities);
	}
	
	// For Connection:
	List<FieldForeignKey> getForeignKeysTo(MetaData m) {
		List<FieldForeignKey> list = new ArrayList<FieldForeignKey>();
		for(Field f : _fields) {
			if(f instanceof FieldForeignKey) {
				FieldForeignKey fk = (FieldForeignKey)f;
				if(fk.getForeignField().getOwner() == m) {
					list.add(fk);
				}
			}
		}
		if(list.isEmpty()) {
			return null;
		}
		else {
			return list;
		}
	}
}
