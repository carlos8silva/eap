package com.netx.bl.R1.core;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.netx.basic.R1.eh.Checker;


public abstract class Association<M extends TimedMetaData, AI extends AssociationInstance<M,?,?>> extends Entity<M,AI> {

	private final MetaData _holderMetaData;
	private final MetaData _associatedMetaData;
	// TODO query to remove all associations for a holder entity?
	protected Select qSelectAssociations = null;

	protected Association(M metaData, HolderEntity<?,?> holder, Entity<?,?> associated) {
		super(metaData);
		Checker.checkNull(holder, "holder");
		Checker.checkNull(associated, "associated");
		_holderMetaData = holder.getMetaData();
		_associatedMetaData = associated.getMetaData();
	}

	// For Repository:
	void load(Repository r) {
		super.load(r);
		// TODO this needs to be moved elsewhere
		// Build selectAssociations query:
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(getMetaData().getTableName());
		sb.append(" WHERE ");
		Iterator<Field >it = _holderMetaData.getPrimaryKeyFields().iterator();
		sb.append(it.next().getColumnName());
		sb.append(" = ?");
		while(it.hasNext()) {
			sb.append(" AND ");
			sb.append(it.next().getColumnName());
			sb.append(" = ?");
		}
		qSelectAssociations = createSelect("select-associations", "SELECT * FROM "+getMetaData().getTableName()+" WHERE "+WhereExpr.toSQL(_holderMetaData.getPrimaryKeyFields()));
	}

	public MetaData getHolderMetaData() {
		return _holderMetaData;
	}

	public MetaData getAssociatedMetaData() {
		return _associatedMetaData;
	}

	public AssociationMap<AI> getAssociationsFor(Connection c, HolderInstance<?,?> hei) throws BLException {
		Checker.checkNull(c, "c");
		Checker.checkNull(hei, "hei");
		// Check if the holder's primary key is already set:
		if(hei.getMetaData().hasAutonumberKey()) {
			if(hei.getValue(hei.getMetaData().getAutonumberKeyField()) == null) {
				// Primary key not set, return an empty AssociationMap:
				return new AssociationMap<AI>(getMetaData(), getAssociatedMetaData(), hei, null);
			}
		}
		// Primary key is set, retrieve associations:
		List<AI> list = selectList(c, qSelectAssociations, hei.getPrimaryKey().getValues());
		return new AssociationMap<AI>(getMetaData(), getAssociatedMetaData(), hei, list);
	}

	protected int insertOrUpdate(Connection c, AssociationMap<AI> map) throws BLException {
		Checker.checkNull(map, "updates");
		int numUpdates = 0;
		// Note: getAdded() updates the association instances' primary key if needed
		Collection<AI> inserts = map.getAdded().values();
		for(AI ai : inserts) {
			insert(c, ai);
			numUpdates++;
		}
		Collection<AI> removals = map.getRemoved().values();
		for(AI ai : removals) {
			deleteInstance(c, ai);
			numUpdates++;
		}
		Collection<AI> updates = map.getCurrent().values();
		for(AI ai : updates) {
			if(ai.hasUpdates()) {
				updateInstance(c, ai);
				numUpdates++;
			}
		}
		map.resetChanges();
		return numUpdates;
	}
}
