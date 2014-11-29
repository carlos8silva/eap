package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class UserEventsMetaData extends TimedMetaData {

	// Fields:
	public final Field eventId = new FieldLong(this, "eventId", "event_id", null, true, true, true, null, null);
	public final Field time = new FieldDateTime(this, "time", "time", "now", true, true);
	public final Field sessionId = new FieldForeignKey(this, "sessionId", "session_id", null, false, true, EAP.getSessions().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);
	public final Field serverAddress = new FieldText(this, "serverAddress", "server_address", null, true, true, 0, 15, false, null, null);
	public final Field clientAddress = new FieldText(this, "clientAddress", "client_address", null, true, true, 0, 15, false, null, null);
	public final Field browser = new FieldText(this, "browser", "browser", null, true, true, 0, 2, false, null, null);
	public final Field type = new FieldForeignKey(this, "type", "type", null, true, true, EAP.getUserEventTypes().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);
	public final Field functionId = new FieldForeignKey(this, "functionId", "function_id", null, false, true, EAP.getFunctions().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);
	public final Field page = new FieldText(this, "page", "page", null, false, true, 0, 100, false, null, new Validators.ReadableText());
	// TODO 16M
	public final Field details = new FieldText(this, "details", "details", null, false, true, 0, 500, false, null, null);
	// TODO 16M
	public final Field stack_trace = new FieldText(this, "stackTrace", "stack_trace", null, false, true, 0, 5000, false, null, null);

	public UserEventsMetaData() {
		super("UserEvents", "eap_user_events");
		addPrimaryKeyField(eventId);
		addField(time);
		addField(sessionId);
		addField(serverAddress);
		addField(clientAddress);
		addField(browser);
		addField(type);
		addField(functionId);
		addField(page);
		addField(details);
		addField(stack_trace);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<UserEvent> getInstanceClass() {
		return UserEvent.class;
	}
	
	public Field getAutonumberKeyField() {
		return eventId;
	}
}
