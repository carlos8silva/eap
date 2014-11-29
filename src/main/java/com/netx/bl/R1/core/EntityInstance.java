package com.netx.bl.R1.core;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;
import com.netx.generics.R1.collections.Node;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;


public abstract class EntityInstance<M extends MetaData,E extends Entity<M,?>> implements Cloneable {

	// Internal maps cannot be final because of clone().
	private final PrimaryKey _primKey;
	private Map<Field,Comparable<?>> _values;
	private Map<Field,Comparable<?>> _updates;
	private boolean _fullInfo;
	private Node<EntityInstance<?,?>> _chainNode;

	protected EntityInstance() {
		_primKey = new PrimaryKey(getMetaData().getPrimaryKeyFields());
		_values = new HashMap<Field,Comparable<?>>();
		_updates = new HashMap<Field,Comparable<?>>();
		_fullInfo = false;
		_chainNode = null;
	}

	public abstract E getEntity();

	public M getMetaData() {
		return getEntity().getMetaData();
	}

	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(this == o) {
			return true;
		}
		if(!(o instanceof EntityInstance)) {
			return false;
		}
		// Verify primary key values:
		return (((EntityInstance<?,?>)o).getPrimaryKey()).equals(getPrimaryKey());
	}

	// The hash code of an entity instance is composed of its
	// class plus the primary key's hash code:
	public int hashCode() {
		if(_primKey == null) {
			return 0;
		}
		else {
			return _primKey.hashCode();
		}
	}

	public Comparable<?> getValue(Field field, boolean includeNull) {
		Checker.checkNull(field, "field");
		Comparable<?> value = _updates.get(field);
		if(value == null) {
			value = _values.get(field);
		}
		if(value == Field.NULL && !includeNull) {
			value = null;
		}
		return value;
	}

	public Comparable<?> getValue(Field field) {
		return getValue(field, false);
	}

	// Used by the application level to set the value of a field.
	// This method ensures consistency of the entity instance by validating
	// the input value according to field type and eventual validation rules,
	// and does not allow setting primary key fields. Every update performed
	// using this method is tracked for UPDATE statements.
	// This method takes care to only register updates that really change the field.
	// It also checks for situations where you make two updates:
	// - first to a different value;
	// - then to the same value as initially.
	// and ignores the update in this case.
	public Comparable<?> setValue(Field f, Comparable<?> value) throws ValidationException {
		return _setValue(f, value, false, true);
	}

	public PrimaryKey getPrimaryKey() {
		_primKey.checkStatus(getMetaData().getAutonumberKeyField());
		return _primKey;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getMetaData().getInstanceClass().getSimpleName());
		sb.append(':');
		sb.append(' ');
		Iterator<Field> it = getMetaData().getFields().iterator(); 
		while(it.hasNext()) {
			Field f = it.next();
			sb.append('[');
			sb.append(f.getName());
			sb.append('=');
			sb.append(getValue(f));
			sb.append(']');
			if(it.hasNext()) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	public boolean fullInformation() {
		return _fullInfo;
	}
	
	public boolean hasUpdates() {
		return _updates.size() > 0;
	}

	@SuppressWarnings("unchecked")
	public EntityInstance<?,?> clone() {
		try {
			if(_updates.size() > 0) {
				throw new IntegrityException();
			}
			EntityInstance<M,E> clone = (EntityInstance<M,E>)super.clone();
			// Copy the internal values to a new Map:
			clone._values = new HashMap<Field,Comparable<?>>();
			clone._values.putAll(_values);
			// Create a new Map for updates:
			clone._updates = new HashMap<Field,Comparable<?>>();
			return clone;
		}
		catch(CloneNotSupportedException cnse) {
			throw new IntegrityException();
		}
	}

	// Used internally by field setters.
	// This method skips internal validations like null Field objects or
	// attempts to set primary key fields, which has a better performance.
	// This method can skip these validations because it is guaranteed to be used
	// in automatically generated code. Updates made with this method are tracked.
	protected Comparable<?> safelySetValue(Field f, Comparable<?> value) throws ValidationException {
		return _setValue(f, value, true, true);
	}

	// Used internally by constructors to set primary key fields
	// (which still need to validate the field's value, but unlike the
	// regular setValue, allow setting primary keys and do not track updates).
	protected void setPrimaryKey(Field f, Comparable<?> value) throws ValidationException {
		// We do not set the field to null if it is an auto-number key
		if(value == null) {
			if(f instanceof FieldForeignKey) {
				// TODO there are many situations where we need to test this; can we do it without a type cast?
				Field fk = ((FieldForeignKey)f).getForeignField();
				if(fk.isAutonumber()) {
					return;
				}
			}
			return;
		}
		_setValue(f, value, true, false);
		_primKey.putValue(f, value);
	}

	// For subclasses to retrieve an allowed value based on its code
	protected Object getAllowedValue(Class<? extends AllowedValue<?>> cs, Object value) {
		AllowedValue<?>[] array = cs.getEnumConstants();
		for(AllowedValue<?> v : array) {
			if(v.getCode().equals(value)) {
				return v;
			}
		}
		return null;
	}

	// For DAFacade:
	// (to set fields without marking them as updates when running SELECT queries)
	void setWithoutTrackingUpdates(Field f, Comparable<?> value) {
		_values.put(f, value);
	}

	// For DAFacade:
	void setFullInformation(boolean fullInfo) {
		_fullInfo = fullInfo;
	}

	// For DAFacade:
	void flushUpdates() {
		_values.putAll(_updates);
		_updates.clear();
	}

	// For DAFacade:
	Map<Field,Comparable<?>> getUpdatedFields() {
		return _updates;
	}

	// For Cache:
	void updateFrom(EntityInstance<?,?> ei) {
		if(_updates.size() > 0) {
			throw new IntegrityException();
		}
		Iterator<Entry<Field,Comparable<?>>> it = ei._updates.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Field,Comparable<?>> e = it.next();
			setWithoutTrackingUpdates(e.getKey(), e.getValue());
		}
	}

	// For Cache.updateList:
	void updateFrom(ValidatedArgument[] updateArgs) {
		if(updateArgs == null || updateArgs.length == 0) {
			throw new IntegrityException();
		}
		for(ValidatedArgument arg : updateArgs) {
			setWithoutTrackingUpdates(arg.getKey(), arg.getValue());
		}
	}

	// For Cache:
	Node<EntityInstance<?,?>> getChainNode() {
		return _chainNode;
	}

	// For Cache:
	void setChainNode(Node<EntityInstance<?,?>> chainNode) {
		_chainNode = chainNode;
	}

	private Comparable<?> _setValue(Field f, Comparable<?> value, boolean bypassInternalChecks, boolean trackUpdates) throws ValidationException {
		if(!bypassInternalChecks) {
			Checker.checkNull(f, "f");
			// Check if attempting to change a non existing field:
			if(!getEntity().getMetaData().getFields().contains(f)) {
				throw new IllegalArgumentException("attempting to change non existing field '"+f.getName()+"'");
			}
			// Check if attempting to change primary keys:
			if(getEntity().getMetaData().getPrimaryKeyFields().contains(f)) {
				throw new IllegalArgumentException("attempting to change primary key '"+f.getName()+"'");
			}
		}
		// Validate the field (this sets value to Field.NULL if needed):
		value = f.validate(value);
		// All clear, register the update:
		if(trackUpdates) {
			_updates.put(f, value);
			// Check if the current value is the same as what we are trying to update to:
			// (if it is, we don't register the update and any attempt to run the update
			// with this object will not hit the database).
			Comparable<?> previousValue = _values.get(f);
			if(previousValue != null) {
				if(value.equals(previousValue)) {
					// It is, so we need to remove this update:
					_updates.remove(f);
				}
			}
		}
		else {
			_values.put(f, value);
		}
		return value;
	}
}
