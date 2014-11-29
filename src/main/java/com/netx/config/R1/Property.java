package com.netx.config.R1;
import java.util.Map;
import java.util.HashMap;


public class Property {

	private final PropertyDef _def;
	private final Context _parent;
	private Object _value;
	private PropertyListener _listener;
	
	// For Context:
	Property(PropertyDef def, Context parent) {
		_def = def;
		_parent = parent;
		_value = null;
		_listener = null;
	}
	
	public String getName() {
		return _def.name;
	}
	
	public Object getValue() {
		if(_value == PropertyDef.NULL) {
			return null;
		}
		return _value;
	}

	public void setListener(PropertyListener listener) {
		if(_listener != null) {
			throw new IllegalStateException("listener already assigned");
		}
		_listener = listener;
	}

	// For Context:
	@SuppressWarnings("unchecked")
	void setValue(String key, Object value) throws ValidationException {
		// Check mandatory:
		if(_def.mandatory && value == null) {
			throw new MandatoryPropertyException(_def.name);
		}
		// Check type:
		if(value != null) {
			if(!_def.type.id.isAssignableFrom(value.getClass())) {
				throw new UnexpectedTypeException(_def.name, _def.type.id, value.getClass());
			}
		}
		_validate(value);
		if(_listener != null) {
			try {
				_listener.onBeforeSet(_parent, key, _value, value);
			}
			catch(Exception ex) {
				throw new PropertySetException(ex);
			}
		}
		// Set the value:
		value = (value == null ? PropertyDef.NULL : value);
		if(key != null) {
			if(_value == null) {
				_value = new HashMap<String,Object>();
			}
			((Map<String,Object>)_value).put(key, value);
		}
		else {
			_value = value;
		}
		if(_listener != null) {
			try {
				_listener.onAfterSet(_parent, key, value);
			}
			catch(Exception ex) {
				throw new PropertySetException(ex);
			}
		}
	}
	
	// For Context.lock:
	boolean hasBeenInitialized() {
		return _value != null;
	}
	
	// For Context:
	PropertyDef getDef() {
		return _def;
	}

	// For Context:
	void initMap() {
		_value = new HashMap<String,Object>();
	}
	
	private void _validate(Object value) {
		// TODO impl
		// This must use validation expressions
	}
}
