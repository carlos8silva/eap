package com.netx.rit.R1.bl;
import com.netx.eap.R1.bl.EAP;


public class RIT extends EAP {

	// TYPE:
	// Entities must be put in a specific order to prevent 
	// initialization problems with foreign keys. The order is:
	// - Reference data before transactional data
	// - Entities with fields relating to other entities must come last
	// Immutable data:
	// Reference data:
	// Transactional data:
	private static final Projects _projects = new Projects();
	private static final Items _items = new Items();
	private static final Conversations _conversations = new Conversations();
	private static final UserConversations _userConversations = new UserConversations();
	private static final Messages _messages = new Messages();
	private static final UserProjects _userProjects = new UserProjects();
	
	// Entity getters:
	public static Projects getProjects() {
		return _projects;
	}

	public static Items getItems() {
		return _items;
	}

	public static Conversations getConversations() {
		return _conversations;
	}

	public static UserConversations getUserConversations() {
		return _userConversations;
	}

	public static Messages getMessages() {
		return _messages;
	}

	public static UserProjects getUserProjects() {
		return _userProjects;
	}
}
