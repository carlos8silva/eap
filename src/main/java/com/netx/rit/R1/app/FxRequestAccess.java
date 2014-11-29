package com.netx.rit.R1.app;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import com.netx.generics.R1.util.Tools;
import com.netx.generics.R1.time.Timestamp;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Users;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.core.Config;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.IllegalParameterException;
import com.netx.eap.R1.core.Template;
import com.netx.eap.R1.core.Values;
import com.netx.eap.R1.core.ValueList;
import com.netx.rit.R1.bl.Projects;
import com.netx.rit.R1.bl.Project;
import com.netx.rit.R1.bl.UserProjects;
import com.netx.rit.R1.bl.UserProject;
import com.netx.rit.R1.bl.Conversations;
import com.netx.rit.R1.bl.Conversation;
import com.netx.rit.R1.bl.Message;
import com.netx.rit.R1.bl.UserConversations;
import com.netx.rit.R1.bl.UserConversation;


public class FxRequestAccess extends Function {
	
	public FxRequestAccess() {
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		Template page = null;
		final String doParam = request.getParameter("do", true);
		final Connection c = request.getConnection();
		if(doParam.equals("list")) {
			page = request.getTemplate("templates/rit-request-access.tpt.html");
			Values v = page.getValues();
			String event = request.getParameter("event");
			if(event != null) {
				if(event.equals("no-access")) {
					v.setIf("no-access-1", true);
					v.setIf("no-access-2", true);
				}
				else {
					throw new IllegalParameterException("event", event);
				}
			}
			ValueList vReqList = v.getList("requests");
			ValueList vMenuList = v.getList("menus");
			// Get all projects:
			List<Project> pList = Projects.getInstance().listAll(c);		
			User me = request.getUserSession().getUser(c);
			// Get all access requests (approved or not):
			List<UserProject> upList = UserProjects.getInstance().listAccessRequestsFor(c, me);
			Iterator<Project> pIt = pList.iterator();
			// Iterate over list of projects and display only those projects
			// which are not owned by the user of have not been approved:
			for(int i=1; pIt.hasNext(); i++) {
				Project p = pIt.next();
				UserProject up = null;
				// TODO maybe we need a map?
				for(UserProject tmp : upList) {
					if(p.getProjectId().equals(tmp.getProjectId())) {
						up = tmp;
						break;
					}
				}
				if(up == null || up.getTimeAccepted() == null) {
					Values vRequest = vReqList.next();
					Values vMenu = vMenuList.next();
					vRequest.set("project", p.getName());
					vRequest.set("description", p.getDescription());
					vRequest.set("owner", p.getOwner(c).getFullName());
					vRequest.set("i", i);
					vMenu.set("i", i);
					boolean noRequest = (up == null);
					Values vIfNone = vMenu.setIf("none", noRequest);
					vIfNone.set("i", i);
					if(noRequest) {
						vRequest.set("request", "None");
						vIfNone.set("project-id", p.getProjectId());
					}
					else {
						Conversation conv = up.getConversation(c);
						vIfNone.set("conv-id", conv.getConversationId());
						if(up.getTimeRejected() != null) {
							vRequest.set("request", "Rejected");
							vIfNone.setIf("pending", false);
						}
						else {
							vRequest.set("request", "Pending");
							Values vIfPending = vIfNone.setIf("pending", true);
							vIfPending.set("i", i);
							vIfPending.set("conv-id", conv.getConversationId());
							vIfPending.set("last-msg", conv.getMessages(c).get(0).getMessageId());
						}
					}
				}
			}
		}
		else if(doParam.equals("create")) {
			Long prjId = request.getLongParameter("project_id", true);
			Project p = Projects.getInstance().get(c, prjId);
			page = request.getTemplate("templates/rit-message-create.tpt.html");
			Values v = page.getValues();
			//v.set("prj-id", p.getProjectId());
			v.set("prj-name", p.getName());
			v.set("owner", p.getOwner(c).getFullName());
			v.set("self", request.getUserSession().getUser(c).getFirstName());
			v.setIf("null-message", "null-message".equals(request.getParameter("event")));
			// TODO impl
		}
		else if(doParam.equals("view")) {
			Long convId = request.getLongParameter("conv_id", true);
			User me = request.getUserSession().getUser(c);
			// Set message to read:
			_markRead(c, convId, me);
			// Conversation info:
			Conversation conv = Conversations.getInstance().get(c, convId);
			page = request.getTemplate("templates/rit-message-view.tpt.html");
			Values v = page.getValues();
			v.set("subject", conv.getSubject());
			UserProject up = UserProjects.getInstance().getByConversation(c, conv.getConversationId());
			if(up == null) {
				throw new IntegrityException(conv.getConversationId());
			}
			// Message info:
			Iterator<Message> msgIt = conv.getMessages(c).iterator();
			Message m = msgIt.next();
			_setMessageFields(v, conv, m, c);
			ValueList vlMessages = v.getList("messages");
			while(msgIt.hasNext()) {
				m = msgIt.next();
				Values vMsg = vlMessages.next();
				_setMessageFields(vMsg, conv, m, c);
			}
			// Ownership info:
			Message lastMsg = conv.getLastMessage(c);
			Project prj = Projects.getInstance().get(c, up.getProjectId());
			v.set("conv-id", conv.getConversationId());
			v.set("last-msg", lastMsg.getMessageId());
			boolean isOwner = me.getUserId().equals(prj.getOwnerId());
			boolean isPending = up.getStatus() == UserProject.STATUS.PENDING;
			Values vIfPending = v.setIf("pending", isPending);
			if(isPending) {
				Values vIfOwner = vIfPending.setIf("prj-owner", isOwner);
				vIfOwner.set("conv-id", conv.getConversationId());
				vIfOwner.set("last-msg", lastMsg.getMessageId());
			}
			else {
				vIfPending.set("conv-id", conv.getConversationId());
				vIfPending.set("last-msg", lastMsg.getMessageId());
			}
			v.setIf("show-accept", isOwner && isPending);
			// Events:
			String event = request.getParameter("event");
			if(event != null) {
				if(event.equals("message-sent")) {
					v.setIf("message-sent", true);
				}
				else if(event.equals("new-message")) {
					v.setIf("new-message", true);
				}
				else {
					throw new IllegalParameterException("event", event);
				}
			}
		}
		else if(doParam.equals("accept") || doParam.equals("reject") || doParam.equals("reply")) {
			Long convId = request.getLongParameter("conv_id", true);
			Conversation conv = Conversations.getInstance().get(c, convId);
			User me = request.getUserSession().getUser(c);
			// Set message to read:
			_markRead(c, convId, me);
			// Check if another message arrived in the meantime:
			Long lastMsgId = request.getLongParameter("last_message");
			if(lastMsgId != null) {
				Message lastMsg = conv.getLastMessage(c);
				if(!lastMsgId.equals(lastMsg.getMessageId())) {
					response.sendRedirect(getAliasURL()+"?do=view&event=new-message&conv_id="+convId);
					return;
				}
			}
			page = request.getTemplate("templates/rit-message-reply.tpt.html");
			Values v = page.getValues();
			v.set("action", doParam);
			v.set("conv-id", convId);
			v.setIf("null-message", "null-message".equals(request.getParameter("event")));
			StringBuilder subject = new StringBuilder(conv.getSubject());
			if(doParam.equals("accept")) {
				subject.append(": accepted");
			}
			else if(doParam.equals("reject")) {
				subject.append(": rejected");
			}
			v.set("subject", subject.toString());
			List<Message> msgList = conv.getMessages(c);
			Message first = msgList.get(0);
			String toUser = first.getFromUser();
			if(toUser.equals(me.getUsername())) {
				toUser = first.getToUsers();
			}
			v.set("to", toUser);
			v.set("to-name", Users.getInstance().getUserByUsername(c, toUser).getFullName());
			Iterator<Message> msgIt = msgList.iterator();
			ValueList vlMessages = v.getList("messages");
			while(msgIt.hasNext()) {
				Message m = msgIt.next();
				Values vMsg = vlMessages.next();
				_setMessageFields(vMsg, conv, m, c);
			}
		}
		else if(doParam.equals("mark-read")) {
			Long convId = request.getLongParameter("conv_id", true);
			User me = request.getUserSession().getUser(c);
			// Set message to read:
			_markRead(c, convId, me);
			response.sendRedirect("eu-list-messages.x");
			return;
		}
		else if(doParam.equals("mark-unread")) {
			Long convId = request.getLongParameter("conv_id", true);
			User me = request.getUserSession().getUser(c);
			// Set message to read:
			_markUnread(c, convId, me);
			response.sendRedirect("eu-list-messages.x");
			return;
		}
		else {
			throw new IllegalParameterException("do", doParam);
		}
		// Render page:
		response.setDisableCache();
		page.render(response);
	}
	
	private void _setMessageFields(Values v, Conversation conv, Message m, Connection c) throws BLException {
		User fromUser = Users.getInstance().getUserByUsername(c, m.getFromUser());
		User toUser = Users.getInstance().getUserByUsername(c, m.getToUsers());
		v.set("from", fromUser == null ? m.getFromUser() : fromUser.getFullName());
		v.set("to", toUser == null ? m.getToUsers() : toUser.getFullName());
		v.set("sent", m.getTimeUpdated().format(Config.DATE_FORMAT));
		v.set("msg", Tools.toHTML(m.getMessage()));
	}

	private void _markRead(Connection c, Long convId, User me) throws BLException {
		UserConversation uc = UserConversations.getInstance().get(c, convId, me.getUserId());
		uc.setTimeRead(new Timestamp());
		UserConversations.getInstance().update(c, uc);
	}

	private void _markUnread(Connection c, Long convId, User me) throws BLException {
		UserConversation uc = UserConversations.getInstance().get(c, convId, me.getUserId());
		uc.setTimeRead((Timestamp)null);
		UserConversations.getInstance().update(c, uc);
	}

	protected void doPost(EapRequest request, EapResponse response) throws IOException, BLException {
		final String action = request.getParameter("action", true);
		final Connection c = request.getConnection();
		final User me = request.getUserSession().getUser(c);
		if(action.equals("create")) {
			final Long prjId = request.getLongParameter("project_id", true);
			final String message = request.getParameter("message");
			if(message == null) {
				_redirect(response, "do=create&event=null-message&project_id="+prjId);
				return;
			}
			Project prj = Projects.getInstance().get(c, prjId);
			Conversation conv = Conversations.getInstance().createRequest(c, me, prj, message);
			_redirect(response, "do=view&event=message-sent&conv_id="+conv.getConversationId());
			return;
		}
		else if(action.equals("accept") || action.equals("reject") || action.equals("reply")) {
			final Long convId = request.getLongParameter("conv_id", true);
			final String toUser = request.getParameter("to_user", true);
			final String message = request.getParameter("message");
			if(message == null) {
				_redirect(response, "do="+action+"&event=null-message&conv_id="+convId);
				return;
			}
			Conversation conv = Conversations.getInstance().get(c, convId);
			User to = Users.getInstance().getUserByUsername(c, toUser);
			if(action.equals("accept")) {
				Conversations.getInstance().accept(c, conv, me, to, message);
			}
			else if(action.equals("reject")) {
				Conversations.getInstance().reject(c, conv, me, to, message);
			}
			else {
				Conversations.getInstance().reply(c, conv, me, to, message);
			}
			_redirect(response, "do=view&event=message-sent&conv_id="+convId);
			return;
		}
		else {
			throw new IllegalParameterException("action", action);
		}
	}
	
	private void _redirect(EapResponse response, String params) throws IOException {
		response.sendRedirect(getAliasURL()+"?"+params);
	}
}
