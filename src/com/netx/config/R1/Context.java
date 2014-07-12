package com.netx.config.R1;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.Time;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Expr;
import com.netx.generics.R1.collections.IList;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.ObjectNotFoundException;
import com.netx.basic.R1.eh.IllegalUsageException;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.l10n.L10n;
import com.netx.basic.R1.shared.Disposable;
import com.netx.basic.R1.shared.Globals;


// TODO change implementation, no hash maps, use arrays
public class Context implements Disposable {

	// TYPE:
	private static final String _GLOBALS = "globals";
	private static final Context _root;
	private static GlobalsConfig _globals = null;
	
	static {
		_root = new Context(new ContextDef("/"), null);
		_root.lock();
		// Register the root ctx as a disposable:
		Globals.registerDisposable(_root);
		// Register the CtxCall genexp function:
		Expr.registerCall(new CtxCall());
	}

	public static Context getRoot() {
		return _root;
	}

	public static GlobalsConfig getGlobals() {
		if(_globals == null) {
			_globals = new GlobalsConfig(_root.getContext(_GLOBALS), _GLOBALS);
		}
		return _globals;
	}

	// INSTANCE:
	private final Context _parent;
	private final ContextDef _def;
	private final Map<String,Property> _properties;
	private final Map<String, Context> _contexts;
	private boolean _locked;

	// Public constructor:
	public Context(String id) {
		Checker.checkEmpty(id, "id");
		_def = Dictionary.getContextDef(id);
		if(_def == null) {
			throw new IllegalArgumentException("could not find context definition for id '"+id+"'");
		}
		_properties = new HashMap<String,Property>();
		_contexts = new HashMap<String,Context>();
		_parent = null;
		_locked = false;
		_loadInternals();
	}
	
	// For self and ContextAnalyzer:
	Context(ContextDef def, Context parent) {
		_def = def;
		_properties = new HashMap<String,Property>();
		_contexts = new HashMap<String,Context>();
		_parent = parent;
		if(_parent != null) {
			if(_parent._contexts.get(def.name) != null) {
				throw new IntegrityException(def.name);
			}
			_parent._contexts.put(def.name, this);
		}
		_locked = false;
		_loadInternals();
	}

	public String getName() {
		return _def.name;
	}
	
	public Context getParent() {
		return _parent;
	}

	public String getPath() {
		StringBuilder path = new StringBuilder();
		Context ctx = this;
		while(true) {
			path.insert(0, '/'+getName());
			ctx = ctx.getParent();
			if(ctx == null) {
				return path.toString();
			}
		}
	}

	public Context getContext(String path) {
		_checkLocked();
		Path cpath = Path.breakContextPath(path);
		return _getContext(cpath);
	}

	// Getter for non-map properties:
	public Property getProperty(String path) {
		_checkLocked();
		Path cpath = Path.breakPropertyPath(path);
		if(cpath.path == null) {
			// If this is the target context, get the property for itself:
			Property p = _properties.get(cpath.name);
			if(p == null) {
				throw new ObjectNotFoundException(path, L10n.GLOBAL_WORD_PROPERTY);
			}
			_checkIsNotMap(p.getDef());
			return p;
		}
		else {
			// Otherwise, request the owner context to get it:
			return _getContext(cpath).getProperty(cpath.name);
		}
	}

	// Getter for map properties:
	public Object getMapObject(String path, String key) {
		Checker.checkEmpty(key, "key");
		_checkLocked();
		Property p = _getMapProperty(path);
		return ((Map<?,?>)p.getValue()).get(key);
	}

	// Setter for non-map properties:
	public Context setProperty(String path, Object value) throws ValidationException {
		Property p = getProperty(path);
		_checkIsNotMap(p.getDef());
		_checkReadOnly(p.getDef());
		p.setValue(null, value);
		return this;
	}

	// Setter for map properties:
	public Context setProperty(String path, String key, Object value) throws ValidationException {
		Checker.checkEmpty(key, "key");
		Property p = _getMapProperty(path);
		_checkReadOnly(p.getDef());
		p.setValue(key, value);
		return this;
	}

	// Type getters:
	public Object		getObject(String path) { return getProperty(path).getValue();	}
	public Boolean		getBoolean(String path) { return (Boolean)getObject(path); }
	public Character	getCharacter(String path) { return (Character)getObject(path); }
	public Byte			getByte(String path) { return (Byte)getObject(path); }
	public Short		getShort(String path) { return (Short)getObject(path); }
	public Integer		getInteger(String path) { return (Integer)getObject(path); }
	public Long			getLong(String path) { return (Long)getObject(path); }
	public Float		getFloat(String path) { return (Float)getObject(path); }
	public Double		getDouble(String path) { return (Double)getObject(path); }
	public String		getString(String path) { return (String)getObject(path); }
	public Timestamp	getTimestamp(String path) { return (Timestamp)getObject(path); }
	public Date			getDate(String path) { return (Date)getObject(path); }
	public Time			getTime(String path) { return (Time)getObject(path); }
	// Type getters for Map properties:
	// TODO just return a map??
	public Boolean		getMapBoolean(String path, String key) { return (Boolean)getMapObject(path, key); }
	public Character	getMapCharacter(String path, String key) { return (Character)getMapObject(path, key); }
	public Byte			getMapByte(String path, String key) { return (Byte)getMapObject(path, key); }
	public Short		getMapShort(String path, String key) { return (Short)getMapObject(path, key); }
	public Integer		getMapInteger(String path, String key) { return (Integer)getMapObject(path, key); }
	public Long			getMapLong(String path, String key) { return (Long)getMapObject(path, key); }
	public Float		getMapFloat(String path, String key) { return (Float)getMapObject(path, key); }
	public Double		getMapDouble(String path, String key) { return (Double)getMapObject(path, key); }
	public String		getMapString(String path, String key) { return (String)getMapObject(path, key); }
	public Timestamp	getMapTimestamp(String path, String key) { return (Timestamp)getMapObject(path, key); }
	public Date			getMapDate(String path, String key) { return (Date)getMapObject(path, key); }
	public Time			getMapTime(String path, String key) { return (Time)getMapObject(path, key); }
	
	public IList<Context> getContexts() {
		_checkLocked();
		return new IList<Context>(new ArrayList<Context>(_contexts.values()));
	}

	public IList<Property> getProperties() {
		_checkLocked();
		List<Property> values = new ArrayList<Property>(_properties.size());
		values.addAll(_properties.values());
		return new IList<Property>(values);
	}

	@SuppressWarnings("unchecked")
	public void lock() {
		// Load defaults:
		for(Property prop : _properties.values()) {
			if(prop.hasBeenInitialized()) {
				continue;
			}
			// Load default values for Map properties:
			if(prop.getDef().isMap) {
				// If the default value is null, we simply do not load any defaults.
				if(prop.getDef().defaultValue != null) {
					Map<String,Object> values = (Map<String,Object>)prop.getDef().defaultValue;
					for(Map.Entry<String,Object> entry : values.entrySet()) {
						try {
							Object loaded = prop.getDef().loadDefault(entry.getValue());
							prop.setValue(entry.getKey(), loaded);
						}
						catch(TypeLoadException tle) {
							// TODO what to do here?
							throw new RuntimeException(tle);
						}
					}
				}
				else {
					// Initialize the property with an empty map:
					prop.initMap();
				}
			}
			// Load default values for regular properties:
			else {
				Object defaultValue = prop.getDef().defaultValue;
				// For complex types, try to assume defaults from the type:
				if(defaultValue == null && prop.getDef().type.complexTL != null) {
					defaultValue = prop.getDef().type.defaultValue;
				}
				if(defaultValue == null) {
					if(prop.getDef().mandatory) {
						// Note: this check is redundant because it happens in prop.setValue as well. But
						// also doing it here allows us to provide a full property path on the error msg.
						throw new MandatoryPropertyException(getPath()+"/"+prop.getDef().name);
					}
					prop.setValue(null, null);
				}
				else {
					try {
						defaultValue = prop.getDef().loadDefault(defaultValue);
					}
					catch(TypeLoadException tle) {
						// TODO what to do here?
						throw new RuntimeException(tle);
					}
					prop.setValue(null, defaultValue);
				}
			}
		}
		// Lock sub-contexts:
		for(Context ctx : _contexts.values()) {
			ctx.lock();
		}
		_locked = true;
	}

	public String toString() {
		return _def.name;
	}

	public void onDispose() {
		// To prevent issues with non-locked contexts:
		_locked = true;
		Iterator<Context> it = getContexts().iterator();
		while(it.hasNext()) {
			it.next().onDispose();
		}
		_contexts.clear();
		_properties.clear();
	}

	// For ContextWrapper:
	ContextDef getDef() {
		return _def;
	}

	private void _checkLocked() {
		if(!_locked) {
			throw new IllegalStateException("context has not been locked yet");
		}
	}
	
	private void _checkReadOnly(PropertyDef def) {
		if(_locked && def.readOnly) {
			throw new ReadOnlyPropertyException(def.name);
		}
	}

	private void _checkIsNotMap(PropertyDef def) {
		if(def.isMap) {
			throw new IllegalUsageException("attempted to perform a map operation on non-map property '"+def.name+"'");
		}
	}

	private void _checkIsMap(PropertyDef def) {
		if(!def.isMap) {
			throw new IllegalUsageException("attempted to perform a non-map operatioin on map property '"+def.name+"'");
		}
	}

	// Used in the constructors:
	private void _loadInternals() {
		Iterator<PropertyDef> itProps = _def.properties.values().iterator();
		while(itProps.hasNext()) {
			PropertyDef pDef = itProps.next();
			_properties.put(pDef.name, new Property(pDef, this));
		}
		Iterator<ContextDef> itCtx = _def.contexts.values().iterator();
		while(itCtx.hasNext()) {
			ContextDef cDef = itCtx.next();
			_contexts.put(cDef.name, new Context(cDef, this));
		}
	}

	private Context _getContext(Path cpath) {
		Context c = this;
		// Used to create the path until which the context is found:
		StringBuilder fp = new StringBuilder();
		for(int i=0; i<cpath.path.length; i++) {
			c = c._contexts.get(cpath.path[i]);
			if(c == null) {
				fp.append(cpath.path[i]);
				cpath.failPath = fp.toString();
				throw new ObjectNotFoundException(cpath.failPath, L10n.GLOBAL_WORD_CONTEXT);
			}
			else {
				fp.append(cpath.path[i]);
				fp.append('/');
			}
		}
		return c;
	}

	private Property _getMapProperty(String path) {
		Path cpath = Path.breakPropertyPath(path);
		if(cpath.path == null) {
			// If this is the target Context, get the property for itself:
			Property p = _properties.get(cpath.name);
			if(p == null) {
				throw new ObjectNotFoundException(path, L10n.GLOBAL_WORD_PROPERTY);
			}
			_checkIsMap(p.getDef());
			return p;
		}
		else {
			// Otherwise, request the owner context to get it:
			return _getContext(cpath)._getMapProperty(cpath.name);
		}
	}
}
