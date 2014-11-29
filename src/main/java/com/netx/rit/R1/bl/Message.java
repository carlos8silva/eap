package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;


public class Message extends TimedInstance<MessagesMetaData,Messages> {
	
	public Message() {
	}

	public Message(Long messageId) throws ValidationException {
		setPrimaryKey(getMetaData().messageId, messageId);
	}
	
	public Messages getEntity() {
		return Messages.getInstance();
	}

	public Long getMessageId() {
		return (Long)getValue(getMetaData().messageId);
	}

	public Long getConversationId() {
		return (Long)getValue(getMetaData().conversationId);
	}

	public Conversation getConversation(Connection c) throws BLException {
		Long conversationId = getConversationId();
		if(conversationId == null) {
			return null;
		}
		return RIT.getConversations().get(c, conversationId);
	}
	
	public Message setConversationId(Long value) {
		safelySetValue(getMetaData().conversationId, value);
		return this;
	}

	public Message setConversation(Conversation conv) {
		return setConversationId(conv == null ? null : conv.getConversationId());
	}

	public String getFromUser() {
		return (String)getValue(getMetaData().fromUser);
	}

	public Message setFromUser(String value) {
		safelySetValue(getMetaData().fromUser, value);
		return this;
	}

	public String getToUsers() {
		return (String)getValue(getMetaData().toUsers);
	}

	public Message setToUsers(String value) {
		safelySetValue(getMetaData().toUsers, value);
		return this;
	}

	public String getMessage() {
		return (String)getValue(getMetaData().message);
	}

	public Message setMessage(String value) {
		safelySetValue(getMetaData().message, value);
		return this;
	}
}
