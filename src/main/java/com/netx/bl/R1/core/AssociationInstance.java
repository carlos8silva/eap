package com.netx.bl.R1.core;
import java.util.List;
import java.util.Iterator;


public abstract class AssociationInstance<AM extends TimedMetaData,AE extends Association<AM,?>, HEI extends HolderInstance<?,?>> extends TimedInstance<AM,AE> {

	protected AssociationInstance() {
	}

	// TODO possible removal
	public abstract HEI getHolder(Connection c) throws BLException;
	// TODO possible removal
	public abstract EntityInstance<?,?> getAssociatedInstance(Connection c) throws BLException;
	
	// For AssociationMap:
	PrimaryKey getAssociatedKey() {
		List<Field> pkFields = getEntity().getAssociatedMetaData().getPrimaryKeyFields();
		Comparable<?>[] values = new Comparable<?>[pkFields.size()];
		Iterator<Field> it = pkFields.iterator();
		for(int i=0; it.hasNext(); i++) {
			Field f = getMetaData().getField(it.next().getName());
			values[i] = getValue(f);
		}
		return new PrimaryKey(pkFields, values);
	}
}
