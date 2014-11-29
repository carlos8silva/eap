package com.netx.bl.R1.core;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import com.netx.generics.R1.collections.ISet;
import com.netx.basic.R1.eh.IntegrityException;


class Transaction {

	private final Map<MetaData,List<TxUpdate>> _changes;
	private final Set<MetaData> _participants;
	
	// For Connection:
	Transaction() {
		_changes = new HashMap<MetaData,List<TxUpdate>>();
		_participants = new HashSet<MetaData>();
	}

	// For DAFacade:
	void storeInsert(EntityInstance<?,?> ei) {
		_store(ei.getMetaData(), new TxUpdate(Query.TYPE.INSERT, ei));
	}

	// For DAFacade:
	void storeUpdate(EntityInstance<?,?> ei) {
		_store(ei.getMetaData(), new TxUpdate(Query.TYPE.UPDATE, ei));
	}

	// For DAFacade:
	void storeUpdate(MetaData m, WhereExpr where, ValidatedArgument[] updateArgs, Argument[] whereArgs) {
		_store(m, new TxUpdate(Query.TYPE.UPDATE, where, updateArgs, whereArgs));
	}
	
	// For DAFacade:
	void storeDelete(EntityInstance<?,?> ei) {
		_store(ei.getMetaData(), new TxUpdate(Query.TYPE.DELETE, ei));
	}

	// For DAFacade:
	void storeDelete(MetaData m, WhereExpr where, Argument[] whereArgs) {
		_store(m, new TxUpdate(Query.TYPE.DELETE, where, null, whereArgs));
	}

	// For DAFacade:
	void storeTruncate(MetaData m) {
		_store(m, new TxUpdate(Query.TYPE.TRUNCATE, null));
	}

	// For Connection:
	boolean hasUpdates() {
		return !_changes.isEmpty();
	}

	// For Connection:
	Collection<MetaData> getParticipants() {
		return new ISet<MetaData>(_participants);
	}

	// For Connection:
	List<TxUpdate> getUpdatesFor(MetaData m) {
		return _changes.get(m);
	}
	
	// For Connection:
	public void clearChanges() {
		_changes.clear();
	}
	
	private void _store(MetaData m, TxUpdate update) {
		List<TxUpdate> list = _changes.get(m);
		if(list == null) {
			list = new ArrayList<TxUpdate>();
			_changes.put(m, list);
		}
		list.add(update);
		// Sanity check:
		if(update.operation.equals(Query.TYPE.TRUNCATE) && list.size() != 1) {
			throw new IntegrityException();
		}
		// Add participants:
		boolean added = _participants.add(m);
		if(added && (update.operation == Query.TYPE.DELETE || update.operation == Query.TYPE.TRUNCATE)) {
			List<MetaData> linkedEntities = m.getLinkedEntities();
			if(!linkedEntities.isEmpty()) {
				for(MetaData lm : linkedEntities) {
					_participants.add(lm);
				}
			}
		}
	}
}
