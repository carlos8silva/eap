package com.netx.rit.R1.bl;
import java.util.List;
import com.netx.generics.R1.time.Timestamp;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.*;


public class Conversation extends HolderInstance<ConversationsMetaData,Conversations> {
	
	public Conversation() {
	}

	public Conversation(Long conversationId) throws ValidationException {
		setPrimaryKey(getMetaData().conversationId, conversationId);
	}

	public Conversations getEntity() {
		return Conversations.getInstance();
	}

	public Long getConversationId() {
		return (Long)getValue(getMetaData().conversationId);
	}

	public String getSubject() {
		return (String)getValue(getMetaData().subject);
	}

	public Conversation setSubject(String value) {
		safelySetValue(getMetaData().subject, value);
		return this;
	}
	
	public Timestamp getTimeArchived() {
		return (Timestamp)getValue(getMetaData().timeArchived);
	}

	public Conversation setTimeArchived(Timestamp value) {
		safelySetValue(getMetaData().timeArchived, value);
		return this;
	}
	
	protected AssociationInstance<?,?,?> createAssociationFor(MetaData metaData, Comparable<?> ... targetKey) throws ValidationException {
		if(metaData == RIT.getUserConversations().getMetaData()) {
			return new UserConversation(getConversationId(), (Long)targetKey[0]);
		}
		throw new IntegrityException(metaData);
	}

	public AssociationMap<UserConversation> getUserConversations(Connection c) throws BLException {
		return UserConversations.getInstance().getAssociationsFor(c, this);
	}
	
	// USER DEFINED METHODS:
	public List<Message> getMessages(Connection c) throws BLException {
		return RIT.getMessages().listMessagesFor(c, this);
	}
	
	public Message getLastMessage(Connection c) throws BLException {
		// TODO improve performance
		return getMessages(c).get(0);
	}
}
