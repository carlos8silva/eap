package com.netx.rit.R1.bl;
import java.util.List;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;


public class Messages extends Entity<MessagesMetaData,Message> {

	// TYPE:
	public static Messages getInstance() {
		return RIT.getMessages();
	}

	// INSTANCE:
	private Select _qSelectMessagesFor = null;

	Messages() {
		super(new MessagesMetaData());
	}

	protected void onLoad() {
		_qSelectMessagesFor = createSelect("select-messages-for", "SELECT * FROM eap_messages WHERE conversation_id = ? ORDER BY message_id DESC");
		_qSelectMessagesFor.setUpdatesCache(true);
	}

	public Message create(Connection c, String subject, String fromUser, String toUsers, String message) throws BLException {
		Conversation conv = new Conversation();
		conv.setSubject(subject);
		Message m = new Message();
		m.setFromUser(fromUser);
		m.setToUsers(toUsers);
		m.setMessage(message);
		synchronized(c) {
			c.startTransaction();
			Conversations.getInstance().create(c, conv);
			m.setConversationId(conv.getConversationId());
			insert(c, m);
			c.commit();
		}
		return m;
	}

	public Message create(Connection c, Long convId, String fromUser, String toUsers, String message) throws BLException {
		Message m = new Message();
		m.setConversationId(convId);
		m.setFromUser(fromUser);
		m.setToUsers(toUsers);
		m.setMessage(message);
		insert(c, m);
		return m;
	}

	// TODO this should be an automatic association and retrieved by:
	// conv.getMessages(Connection c):List<Message>
	public List<Message> listMessagesFor(Connection c, Conversation conv) throws BLException {
		Checker.checkNull(conv,  "conv");
		return selectList(c, _qSelectMessagesFor, conv.getConversationId());
	}
}
