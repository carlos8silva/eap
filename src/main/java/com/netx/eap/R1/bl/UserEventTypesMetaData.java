package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserEventTypesMetaData extends TimedMetaData {

	// Fields:
	public final Field eventTypeId = new FieldInt(this, "eventTypeId", "event_type_id", null, true, true, null, null);
	public final Field description = new FieldText(this, "description", "description", null, true, true, 0, 50, false, null, new Validators.ReadableText());

	public UserEventTypesMetaData() {
		super("UserEventTypes", "eap_u_event_types");
		addPrimaryKeyField(eventTypeId);
		addField(description);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<UserEventType> getInstanceClass() {
		return UserEventType.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
