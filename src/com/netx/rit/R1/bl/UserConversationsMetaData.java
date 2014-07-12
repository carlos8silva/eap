package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;


public class UserConversationsMetaData extends TimedMetaData {

	// Fields:
	public final Field conversationId = new FieldForeignKey(this, "conversationId", "conversation_id", null, true, true, RIT.getConversations().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field userId = new FieldForeignKey(this, "userId", "user_id", null, true, true, RIT.getUsers().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field timeRead = new FieldDateTime(this, "time_read", "time_read", null, false, false);
	
	public UserConversationsMetaData() {
		super("UserConversations", "eap_user_conversations");
		addPrimaryKeyField(conversationId);
		addPrimaryKeyField(userId);
		addField(timeRead);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<UserConversation> getInstanceClass() {
		return UserConversation.class;
	}
	
	public Field getAutonumberKeyField() {
		return null;
	}
}
