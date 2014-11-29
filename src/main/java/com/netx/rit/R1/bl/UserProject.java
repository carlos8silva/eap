package com.netx.rit.R1.bl;
import com.netx.generics.R1.time.Timestamp;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.bl.*;


public class UserProject extends TimedInstance<UserProjectsMetaData,UserProjects> {
	
	public UserProject(Long userId, Long projectId) throws ValidationException {
		setPrimaryKey(getMetaData().userId, userId);
		setPrimaryKey(getMetaData().projectId, projectId);
	}
	
	public UserProjects getEntity() {
		return UserProjects.getInstance();
	}

	public Long getUserId() {
		return (Long)getValue(getMetaData().userId);
	}

	public User getUser(Connection c) throws BLException {
		Long id = getUserId();
		if(id == null) {
			return null;
		}
		return Users.getInstance().get(c, id);
	}

	public Long getProjectId() {
		return (Long)getValue(getMetaData().projectId);
	}

	public Project getProject(Connection c) throws BLException {
		Long id = getProjectId();
		if(id == null) {
			return null;
		}
		return Projects.getInstance().get(c, id);
	}

	public Timestamp getTimeAccepted() {
		return (Timestamp)getValue(getMetaData().timeAccepted);
	}

	public UserProject setTimeAccepted(Timestamp value) {
		safelySetValue(getMetaData().timeAccepted, value);
		return this;
	}

	public Timestamp getTimeRejected() {
		return (Timestamp)getValue(getMetaData().timeRejected);
	}

	public UserProject setTimeRejected(Timestamp value) {
		safelySetValue(getMetaData().timeRejected, value);
		return this;
	}

	public Long getConversationId() {
		return (Long)getValue(getMetaData().conversationId);
	}

	public Conversation getConversation(Connection c) throws BLException {
		Long convId = getConversationId();
		if(convId == null) {
			return null;
		}
		return Conversations.getInstance().get(c, convId);
	}
	
	public UserProject setConversationId(Long value) {
		safelySetValue(getMetaData().conversationId, value);
		return this;
	}

	public UserProject setConversation(Conversation value) {
		return setConversationId(value == null ? null : value.getConversationId());
	}
	
	// USER DEFINED METHODS:
	public static enum STATUS {PENDING, ACCEPTED, REJECTED};
	
	public STATUS getStatus() {
		if(getTimeAccepted() != null) {
			return STATUS.ACCEPTED;
		}
		if(getTimeRejected() != null) {
			return STATUS.REJECTED;
		}
		return STATUS.PENDING;
	}
}
