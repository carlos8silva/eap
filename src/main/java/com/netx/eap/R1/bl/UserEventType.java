package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserEventType extends TimedInstance<UserEventTypesMetaData, UserEventTypes> {

	public UserEventType(Integer eventTypeId) throws ValidationException {
		setPrimaryKey(getMetaData().eventTypeId, eventTypeId);
	}

	public UserEventTypes getEntity() {
		return UserEventTypes.getInstance();
	}

	public Integer getEventTypeId() {
		return (Integer)getValue(getMetaData().eventTypeId);
	}

	public String getDescription() {
		return (String)getValue(getMetaData().description);
	}
}
