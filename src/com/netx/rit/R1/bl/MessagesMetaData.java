package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.bl.Validators;
import com.netx.generics.R1.util.ByteValue;
import com.netx.generics.R1.util.ByteValue.MEASURE;


public class MessagesMetaData extends TimedMetaData {

	// Fields:
	public final Field messageId = new FieldLong(this, "messageId", "message_id", null, true, true, true, null, null);
	public final Field conversationId = new FieldForeignKey(this, "conversationId", "conversation_id", null, true, true, RIT.getConversations().getMetaData(), FieldForeignKey.ON_DELETE_CONSTRAINT.CASCADE);
	public final Field fromUser = new FieldText(this, "fromUser", "from_user", null, true, true, 2, 20, true, null, new Validators.Username());
	public final Field toUsers = new FieldText(this, "toUsers", "to_users", null, true, true, 2, (long)ByteValue.convert(16, MEASURE.MEGABYTES, MEASURE.BYTES), true, null, new Validators.Username());
	public final Field message = new FieldText(this, "message", "message", null, true, true, 0, (long)ByteValue.convert(16, MEASURE.MEGABYTES, MEASURE.BYTES), true, null, null);
	
	public MessagesMetaData() {
		super("Message", "eap_messages");
		addPrimaryKeyField(messageId);
		addField(conversationId);
		addField(fromUser);
		addField(toUsers);
		addField(message);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<Message> getInstanceClass() {
		return Message.class;
	}
	
	public Field getAutonumberKeyField() {
		return messageId;
	}
}
