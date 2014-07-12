package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;


public class UserProjectsMetaData extends TimedMetaData {

	// Fields:
	public final Field userId = new FieldForeignKey(this, "userId", "user_id", null, true, true, RIT.getUsers().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field projectId = new FieldForeignKey(this, "projectId", "project_id", null, true, true, RIT.getProjects().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field conversationId = new FieldForeignKey(this, "conversationId", "conversation_id", null, true, true, RIT.getConversations().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field timeAccepted = new FieldDateTime(this, "timeAccepted", "time_accepted", Field.NULL, false, false);
	public final Field timeRejected = new FieldDateTime(this, "timeRejected", "time_rejected", Field.NULL, false, false);
	
	public UserProjectsMetaData() {
		super("UserProjects", "rit_user_projects");
		addPrimaryKeyField(userId);
		addPrimaryKeyField(projectId);
		addField(conversationId);
		addField(timeAccepted);
		addField(timeRejected);
		addDefaultFields();
		addUnique(conversationId);
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<UserProject> getInstanceClass() {
		return UserProject.class;
	}
	
	public Field getAutonumberKeyField() {
		return null;
	}
}
