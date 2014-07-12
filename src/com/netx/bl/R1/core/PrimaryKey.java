package com.netx.bl.R1.core;
import java.util.List;
import java.util.Iterator;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.eh.IllegalUsageException;


public class PrimaryKey {

	private final Argument[] _args;
	private int _hashCode = -1;
	private String _toString = null;
	
	// For Entity, EntityInstance, HolderInstance:
	PrimaryKey(List<Field> primKeyFields) {
		_args = new Argument[primKeyFields.size()];
		Iterator<Field> it = primKeyFields.iterator();
		for(int i=0; it.hasNext(); i++) {
			_args[i] = new Argument(it.next(), null);
		}
	}

	// For HolderInstance:
	PrimaryKey(List<Field> primKeyFields, Comparable<?>[] values) {
		_args = new Argument[primKeyFields.size()];
		Iterator<Field> itFields = primKeyFields.iterator();
		for(int i=0; itFields.hasNext(); i++) {
			_args[i] = new Argument(itFields.next(), values[i]);
		}
	}

	// For Entity:
	// TODO remove
	PrimaryKey(Argument[] args) {
		_args = args;
	}

	public Comparable<?> getValue(Field f) {
		for(Argument arg : _args) {
			if(f.equals(arg.getKey())) {
				return arg.getValue();
			}
		}
		// Could not find specified field:
		throw new IntegrityException(f);
	}

	public Argument[] toArgumentArray() {
		return _args;
	}

	public Comparable<?>[] getValues() {
		Comparable<?>[] values = new Comparable<?>[_args.length];
		for(int i=0; i<_args.length; i++) {
			values[i] = _args[i].getValue();
		}
		return values;
	}

	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null) {
			return false;
		}
		if(o instanceof PrimaryKey) {
			PrimaryKey pk = (PrimaryKey)o;
			for(Argument arg : _args) {
				Comparable<?> other = pk.getValue(arg.getKey());
				if(Field.compare(arg.getKey(), arg.getValue(), other) != 0) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public int hashCode() {
		if(_hashCode == -1) {
			_hashCode = 7;
			for(Argument arg : _args) {
				Object value = arg.getValue();
				if(arg.getKey().getType().equals(Field.TYPE.TEXT) && ((FieldText)arg.getKey()).ignoreCase()) {
					value = value.toString().toLowerCase();
				}
				if(arg.getKey().getType().equals(Field.TYPE.CHAR) && ((FieldChar)arg.getKey()).ignoreCase()) {
					value = Character.toLowerCase((Character)value);
				}
				_hashCode = 31 * _hashCode + value.hashCode();
			}
		}
		return _hashCode;
	}
	
	public String toString() {
		if(_toString == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for(int i=0; i<_args.length; i++) {
				sb.append(_args[i].getKey().getName());
				sb.append("=");
				sb.append(_args[i].getValue());
				if(i < _args.length - 1) {
					sb.append(",");
				}
			}
			sb.append("]");
			_toString = sb.toString();
		}
		return _toString;
	}

	// For Entity, EntityInstance, HolderEntityInstance:
	// Note: we use IllegalArgumentException in this method because it can be called from
	// setPrimaryKey, which in turn can be called by the client developer by mistake.
	// We cannot prevent setPrimaryKey from being called outside of the core package
	// because EntityInstances need to call it when setting their primary key.
	void putValue(Field f, Comparable<?> value) {
		for(Argument arg : _args) {
			if(f.equals(arg.getKey())) {
				if(arg.isSet()) {
					// Primary key value can only be set once:
					throw new IllegalUsageException("primary key field '"+f.getName()+"' has already been set");
				}
				arg.setValue(value);
				_toString = null;
				return;
			}
		}
		// Could not find specified field:
		throw new IllegalUsageException("field '"+f.getName()+"' is not part of the primary key");
	}
	
	void checkStatus(Field f) {
		if(f != null) {
			for(Argument arg : _args) {
				if(f.equals(arg.getKey()) && !arg.isSet()) {
					throw new IllegalUsageException("primary key field '"+f.getName()+"' has not been set yet");
				}
			}
		}
	}
}
