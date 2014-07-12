package com.netx.bl.R1.core;


public abstract class TimedMetaData extends MetaData {

	public final Field timeCreated = new FieldDateTime(this, "timeCreated", "time_created", "[now()]", true, true);
	public final Field timeUpdated = new FieldDateTime(this, "timeUpdated", "time_updated", "[now()]", true, false);
	
	protected TimedMetaData(String name, String tableName) {
		super(name, tableName);
	}

	protected void addDefaultFields() {
		addField(timeCreated);
		addField(timeUpdated);
	}
}
