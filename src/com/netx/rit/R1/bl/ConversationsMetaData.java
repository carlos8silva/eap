package com.netx.rit.R1.bl;
import com.netx.generics.R1.util.ByteValue;
import com.netx.generics.R1.util.ByteValue.MEASURE;
import com.netx.bl.R1.core.*;


public class ConversationsMetaData extends TimedMetaData {

	// Fields:
	public final Field conversationId = new FieldLong(this, "conversationId", "conversation_id", null, true, true, true, null, null);
	public final Field subject = new FieldText(this, "subject", "subject", null, true, false, 2, (long)ByteValue.convert(16, MEASURE.MEGABYTES, MEASURE.BYTES), true, null, null);
	public final Field timeArchived = new FieldDateTime(this, "time_archived", "time_archived", null, false, false);
	
	public ConversationsMetaData() {
		super("Conversations", "eap_conversations");
		addPrimaryKeyField(conversationId);
		addField(subject);
		addField(timeArchived);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<Conversation> getInstanceClass() {
		return Conversation.class;
	}
	
	public Field getAutonumberKeyField() {
		return conversationId;
	}
}
