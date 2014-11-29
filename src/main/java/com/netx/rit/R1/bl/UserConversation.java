package com.netx.rit.R1.bl;
import com.netx.generics.R1.time.Timestamp;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.bl.*;


public class UserConversation extends AssociationInstance<UserConversationsMetaData,UserConversations,User> {

	public UserConversation(Long conversationId, Long userId) throws ValidationException {
		setPrimaryKey(getMetaData().conversationId, conversationId);
		setPrimaryKey(getMetaData().userId, userId);
	}

	public UserConversations getEntity() {
		return UserConversations.getInstance();
	}

	public Long getConversationId() {
		return (Long)getValue(getMetaData().conversationId);
	}
	
	public Conversation getConversation(Connection c) throws BLException {
		Long conversationId = getConversationId();
		if(conversationId == null) {
			return null;
		}
		return Conversations.getInstance().get(c, conversationId);
	}

	public Conversation getAssociatedInstance(Connection c) throws BLException {
		return getConversation(c);
	}

	public Long getUserId() {
		return (Long)getValue(getMetaData().userId);
	}

	public User getUser(Connection c) throws BLException {
		Long userId = getUserId();
		if(userId == null) {
			return null;
		}
		return Users.getInstance().get(c, userId);
	}

	public User getHolder(Connection c) throws BLException {
		return getUser(c);
	}

	public Timestamp getTimeRead() {
		return (Timestamp)getValue(getMetaData().timeRead);
	}

	public UserConversation setTimeRead(Timestamp value) {
		safelySetValue(getMetaData().timeRead, value);
		return this;
	}
}
