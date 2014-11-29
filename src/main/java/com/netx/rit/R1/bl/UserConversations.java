package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;


public class UserConversations extends Association<UserConversationsMetaData,UserConversation> {

	// TYPE:
	public static UserConversations getInstance() {
		return RIT.getUserConversations();
	}

	// INSTANCE:
	// For Domain:
	UserConversations() {
		super(new UserConversationsMetaData(), RIT.getUsers(), RIT.getConversations());
	}
	
	public void create(Connection c, UserConversation uc) throws BLException {
		insert(c, uc);
	}

	public void update(Connection c, UserConversation uc) throws BLException {
		updateInstance(c, uc);
	}
}
