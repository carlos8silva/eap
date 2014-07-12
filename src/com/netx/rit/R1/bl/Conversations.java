package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.bl.User;
import com.netx.generics.R1.time.Timestamp;


public class Conversations extends Entity<ConversationsMetaData,Conversation> {

	// TYPE:
	public static Conversations getInstance() {
		return RIT.getConversations();
	}

	// INSTANCE:
	Conversations() {
		super(new ConversationsMetaData());
	}
	
	public Conversation create(Connection c, Conversation conv) throws BLException {
		insert(c, conv);
		return conv;
	}

	public void update(Connection c, Conversation conv) throws BLException {
		updateInstance(c, conv);
	}

	public Conversation createRequest(Connection c, User user, Project prj, String message) throws BLException {
		Conversation conv = new Conversation();
		conv.setSubject("Request to access project \""+prj.getName()+"\"");
		UserProject up = new UserProject(user.getUserId(), prj.getProjectId());
		User toUser = prj.getOwner(c);
		synchronized(c) {
			c.startTransaction();
			Conversations.getInstance().create(c, conv);
			UserConversation uc1 = new UserConversation(conv.getConversationId(), user.getUserId());
			UserConversations.getInstance().create(c, uc1);
			UserConversation uc2 = new UserConversation(conv.getConversationId(), toUser.getUserId());
			UserConversations.getInstance().create(c, uc2);
			up.setConversationId(conv.getConversationId());
			UserProjects.getInstance().create(c, up);
			Messages.getInstance().create(c, conv.getConversationId(), user.getUsername(), toUser.getUsername(), message);
			c.commit();
		}
		return conv;
	}
	
	public Message accept(Connection c, Conversation conv, User from, User to, String message) throws BLException {
		conv.setSubject(conv.getSubject()+": accepted");
		UserProject up = UserProjects.getInstance().getByConversation(c, conv.getConversationId());
		up.setTimeAccepted(new Timestamp());
		up.setTimeRejected((Timestamp)null);
		return _reply(c, conv, from, to, message, up);
	}

	public Message reject(Connection c, Conversation conv, User from, User to, String message) throws BLException {
		conv.setSubject(conv.getSubject()+": rejected");
		UserProject up = UserProjects.getInstance().getByConversation(c, conv.getConversationId());
		up.setTimeRejected(new Timestamp());
		up.setTimeAccepted((Timestamp)null);
		return _reply(c, conv, from, to, message, up);
	}

	public Message reply(Connection c, Conversation conv, User from, User to, String message) throws BLException {
		return _reply(c, conv, from, to, message, null);
	}

	public Message _reply(Connection c, Conversation conv, User from, User to, String message, UserProject up) throws BLException {
		UserConversation uc = new UserConversation(conv.getConversationId(), to.getUserId());
		uc.setTimeRead((Timestamp)null);
		Message m = null;
		synchronized(c) {
			c.startTransaction();
			// Update conversation subject:
			Conversations.getInstance().update(c, conv);
			// Send message:
			m = Messages.getInstance().create(c, conv.getConversationId(), from.getUsername(), to.getUsername(), message);
			// Set recipient's conversation to unread:
			UserConversations.getInstance().update(c, uc);
			// Update permissions:
			if(up != null) {
				UserProjects.getInstance().update(c, up);
			}
			c.commit();
		}
		return m;
	}
}
