package com.netx.bl.R1.core;
import com.netx.generics.R1.time.Timestamp;


public abstract class TimedInstance<M extends TimedMetaData,E extends Entity<M,?>> extends EntityInstance<M,E> {

	protected TimedInstance() {
	}

	public Timestamp getTimeCreated() {
		return (Timestamp)getValue(getMetaData().timeCreated);
	}
	
	public Timestamp getTimeUpdated() {
		return (Timestamp)getValue(getMetaData().timeUpdated);
	}
}
