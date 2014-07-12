package com.netx.bl.R1.core;
import java.util.Map;


public class UT {

	public static void setPrimaryKey(EntityInstance<?,?> ei, Field f, Comparable<?> value) {
		ei.setPrimaryKey(f, value);
	}
	
	public static Map<PrimaryKey,?> getCurrent(AssociationMap<?> map) {
		return map.getCurrent();
	}

	public static Map<PrimaryKey,?> getAdded(AssociationMap<?> map) {
		return map.getAdded();
	}
	
	public static Map<PrimaryKey,?> getRemoved(AssociationMap<?> map) {
		return map.getRemoved();
	}
}
