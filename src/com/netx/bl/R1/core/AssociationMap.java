package com.netx.bl.R1.core;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.netx.basic.R1.eh.Checker;


public class AssociationMap<AI extends AssociationInstance<?,?,?>> implements Iterable<AI> {

	private final HolderInstance<?,?> _holder;
	private final MetaData _metaData;
	private final List<Field> _associatedPK;
	private final Map<PrimaryKey,AI> _current;
	private final Map<PrimaryKey,AI> _added;
	private final Map<PrimaryKey,AI> _removed;
	private final Map<PrimaryKey,AI> _addedRemoved;
	private boolean _updateKeysRequired = false;
	
	// For Association:
	AssociationMap(MetaData metaData, MetaData associatedMetaData, HolderInstance<?,?> holder, List<AI> associations) {
		_holder = holder;
		_metaData = metaData;
		_associatedPK = associatedMetaData.getPrimaryKeyFields();
		_current = new HashMap<PrimaryKey,AI>();
		_added = new HashMap<PrimaryKey,AI>();
		_removed = new HashMap<PrimaryKey,AI>();
		_addedRemoved = new HashMap<PrimaryKey,AI>();
		if(associations != null) {
			for(AI ai : associations) {
				_current.put(ai.getAssociatedKey(), ai);
			}
		}
		if(_holder.getMetaData().hasAutonumberKey()) {
			_updateKeysRequired = _holder.getValue(_holder.getMetaData().getAutonumberKeyField())==null;
		}
	}

	@SuppressWarnings("unchecked")
	public AI put(EntityInstance<?,?> associated) {
		Checker.checkNull(associated, "associated");
		PrimaryKey pk = associated.getPrimaryKey();
		AI ai = (AI)_holder.createAssociationFor(_metaData, pk.getValues());
		AI tmp = _current.get(pk);
		if(tmp != null) {
			return tmp;
		}
		tmp = _added.get(pk);
		if(tmp != null) {
			return tmp;
		}
		tmp = _removed.get(pk);
		if(tmp != null) {
			_removed.remove(pk);
			_current.put(pk, tmp);
			return tmp;
		}
		tmp = _addedRemoved.get(pk);
		if(tmp != null) {
			_addedRemoved.remove(pk);
			_added.put(pk, tmp);
			return tmp;
		}
		_added.put(pk, ai);
		return ai;
	}

	public AI get(PrimaryKey pk) {
		Checker.checkNull(pk, "pk");
		AI tmp = _current.get(pk);
		if(tmp != null) {
			return tmp;
		}
		return _added.get(pk);
	}

	public AI get(Comparable<?> ... values) {
		Checker.checkEmpty(values, "values");
		return get(new PrimaryKey(_associatedPK, values));
	}
	
	public AI remove(PrimaryKey pk) {
		Checker.checkNull(pk, "pk");
		AI tmp = _current.remove(pk);
		if(tmp != null) {
			_removed.put(pk, tmp);
			return tmp;
		}
		tmp = _added.remove(pk);
		if(tmp != null) {
			_addedRemoved.put(pk, tmp);
			return tmp;
		}
		return null;
	}

	public AI remove(Comparable<?> ... values) {
		Checker.checkEmpty(values, "values");
		// TODO add check for length of "values" array
		return remove(new PrimaryKey(_associatedPK, values));
	}
	
	public void clear() {
		_removed.putAll(_current);
		_current.clear();
		_addedRemoved.putAll(_added);
		_added.clear();
	}

	public boolean hasUpdates() {
		if(!_added.isEmpty()) {
			return true;
		}
		if(!_removed.isEmpty()) {
			return true;
		}
		for(AI tmp : _current.values()) {
			if(tmp.hasUpdates()) {
				return true;
			}
		}
		return false;
	}
	
	public int size() {
		return _current.size() + _added.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public void finalize() {
		_current.clear();
		_added.clear();
		_removed.clear();
		_addedRemoved.clear();
	}

	public Iterator<AI> iterator() {
		List<AI> elements = new ArrayList<AI>();
		elements.addAll(_current.values());
		elements.addAll(_added.values());
		return elements.iterator();
	}
	
	// For Association:
	Map<PrimaryKey, AI> getCurrent() {
		return _current;
	}

	// For Association:
	Map<PrimaryKey, AI> getAdded() {
		// Update primary key for Associations. This is needed in case
		// the holder entity instance has an auto-number key.
		if(_updateKeysRequired) {
			Argument pkArg = null;
			for(Field f : _metaData.getPrimaryKeyFields()) {
				Field fk = ((FieldForeignKey)f).getForeignField();
				if(fk.isAutonumber()) {
					pkArg = new Argument(f, _holder.getValue(fk));
					break;
				}
			}
			for(AI ai : _added.values()) {
				ai.setPrimaryKey(pkArg.getKey(), pkArg.getValue());
			}
		}
		return _added;
	}

	// For Association:
	Map<PrimaryKey, AI> getRemoved() {
		return _removed;
	}

	// For Association:
	void resetChanges() {
		_current.putAll(_added);
		_added.clear();
		_removed.clear();
		_addedRemoved.clear();
		_updateKeysRequired = false;
	}
}
