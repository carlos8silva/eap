package com.netx.bl.R1.core;


public abstract class HolderInstance<M extends TimedMetaData,E extends Entity<M,?>> extends TimedInstance<M,E> {

	protected HolderInstance() {
	}

	protected abstract AssociationInstance<?,?,?> createAssociationFor(MetaData metaData, Comparable<?> ... targetKey) throws ValidationException;
}
