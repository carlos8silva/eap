package com.netx.eap.R1.app;
import java.util.List;
import com.netx.generics.R1.collections.IMap;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.AssociationMap;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.UserSession;
import com.netx.eap.R1.core.Template;
import com.netx.eap.R1.core.Values;
import com.netx.eap.R1.core.ValueList;
import com.netx.eap.R1.core.XmlResponse;
import com.netx.eap.R1.core.Constants;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.IllegalParameterException;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.bl.Role;
import com.netx.eap.R1.bl.UserRole;
import com.netx.eap.R1.bl.Permission;
import com.netx.eap.R1.bl.Users;
import com.netx.eap.R1.bl.SecurityQuestions;
import com.netx.eap.R1.bl.SecurityQuestion;
import com.netx.eap.R1.bl.Session.EndReason;


public class FunctionMyAccount extends Function {

	protected void doGet(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		// Retrieve data from the database:
		Connection c = request.getConnection();
		// Populate template with data:
		final User user = request.getUserSession().getUser(c);
		Template page = request.getTemplate("templates/all-my-account.tpt.html");
		Values v = page.getValues();
		v.set("username", user.getUsername());
		v.set("full-name", user.getFullName(false));
		// Roles:
		ValueList vRoles = v.getList("roles");
		ValueList vJsRoles = v.getList("js-roles");
		final AssociationMap<UserRole> userRoles = user.getUserRoles(c);
		int i = 1;
		for(UserRole ur : userRoles) {
			Role role = ur.getRole(c);
			Values roleValues = vRoles.next();
			Values jsRoleValues = vJsRoles.next();
			roleValues.set("i", i+"");
			roleValues.set("role-name", role.getName());
			jsRoleValues.set("i", i+"");
			// JS names and descriptions need to be escaped to avoid JS errors:
			jsRoleValues.set("role-name", Strings.addSlashes(role.getName()));
			jsRoleValues.set("role-desc", Strings.addSlashes(role.getDescription()));
			i++;
		}
		// Permissions:
		// Note: user permission information is cached in the HTTP session so that
		// there is no inconsistency in case the user permissions are changed while
		// the user is logged in (as per FDD)
		IMap<String,Permission> userPerms = request.getUserSession().getUserPermissions();
		if(userPerms.isEmpty()) {
			v.setIf("has-individual-perms", false);
		}
		else {
			Values ifValues = v.setIf("has-individual-perms", true);
			ValueList vPerms = ifValues.getList("perms");
			ValueList vJsPerms = v.getList("js-perms");
			i = 1;
			for(Permission af : userPerms.values()) {
				Values permValues = vPerms.next();
				Values jsPermValues = vJsPerms.next();
				permValues.set("i", i+"");
				permValues.set("perm-name", af.getName());
				jsPermValues.set("i", i+"");
				// JS names and descriptions need to be escaped to avoid JS errors:
				jsPermValues.set("perm-name", Strings.addSlashes(af.getName()));
				jsPermValues.set("perm-desc", Strings.addSlashes(af.getDescription()));
				i++;
			}
		}
		// Security questions:
		final List<SecurityQuestion> questions = SecurityQuestions.getInstance().listAll(c);
		ValueList vQuestions1 = v.getList("questions1");
		ValueList vQuestions2 = v.getList("questions2");
		for(SecurityQuestion sq : questions) {
			vQuestions1.next().set("value", sq.getSeqQuestionId()).set("question", sq.getQuestion());
			vQuestions2.next().set("value", sq.getSeqQuestionId()).set("question", sq.getQuestion());
		}
		// Session expiration:
		v.setLenient(true);
		v.set("session-expires-0", "");
		v.set("session-expires-10", "");
		v.set("session-expires-20", "");
		v.set("session-expires-30", "");
		v.set("session-expires-60", "");
		v.set("session-expires-120", "");
		v.set("session-expires-"+user.getSessionTimeoutTime(), "selected=\"selected\"");
		response.setDisableCache();
		page.render(MimeTypes.TEXT_HTML, response);
		c.close();
	}

	protected void doPost(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		final String actionParam = request.getParameter("action", true);
		if(actionParam.equals("account-settings")) {
			final int sessionExpires = request.getIntParameter("session_expires", true);
			// Update database:
			Connection c = request.getConnection();
			User user = request.getUserSession().getUser(c);
			user.setSessionTimeoutTime(sessionExpires);
			Users.getInstance().save(c, user, null, null);
			c.close();
			response.sendRedirect(getAliasURL());
		}
		else if(actionParam.equals("change-password")) {
			request.setXml(true);
			final String oldPassword = request.getParameter("old_password", true);
			final String newPassword = request.getParameter("password1", true);
			Connection c = request.getConnection();
			UserSession s = request.getUserSession();
			User user = s.getUser(c);
			XmlResponse xml = null;
			if(!user.getPassword().equals(oldPassword)) {
				xml = new XmlResponse("ERROR");
				boolean exceededAttempts = _incNumPasswordTries(c, s, request, response);
				if(exceededAttempts) {
					xml.getRoot().addElement("error-code").setText("too-many-tries");
				}
				else {
					xml.getRoot().addElement("error-code").setText("wrong-password");
					// TODO L10n
					xml.getRoot().addElement("message").setText("Password incorrect, please try again");
				}
			}
			else {
				xml = new XmlResponse("OK");
				// Reset number of failed password tries:
				s.setAttribute(Constants.SATTR_SESSION_NUM_FAILED_PASSWORDS, 0);
				// Change the password:
				user.setPassword(newPassword);
				user.setPasswordChangeTime(new Timestamp());
				Users.getInstance().save(c, user, null, null);
			}
			c.close();
			response.setDisableCache();
			xml.render(response);
		}
		else if(actionParam.equals("security-questions")) {
			request.setXml(true);
			final String password = request.getParameter("password_sc", true);
			Connection c = request.getConnection();
			UserSession s = request.getUserSession();
			User user = s.getUser(c);
			XmlResponse xml = null;
			if(!user.getPassword().equals(password)) {
				xml = new XmlResponse("ERROR");
				boolean exceededAttempts = _incNumPasswordTries(c, s, request, response);
				if(exceededAttempts) {
					xml.getRoot().addElement("error-code").setText("too-many-tries");
				}
				else {
					xml.getRoot().addElement("error-code").setText("wrong-password");
					// TODO L10n
					xml.getRoot().addElement("message").setText("Password incorrect, please try again");
				}
			}
			else {
				final Integer question1 = request.getIntParameter("question1", true);
				final Integer question2 = request.getIntParameter("question2", true);
				final String answer1 = request.getParameter("answer1", true);
				final String answer2 = request.getParameter("answer2", true);
				xml = new XmlResponse("OK");
				// Reset number of failed password tries:
				s.setAttribute(Constants.SATTR_SESSION_NUM_FAILED_PASSWORDS, 0);
				// Update security questions:
				user.setSecurityQuestion1(question1);
				user.setSecurityQuestion2(question2);
				user.setSecurityAnswer1(answer1);
				user.setSecurityAnswer2(answer2);
				Users.getInstance().save(c, user, null, null);
			}
			c.close();
			response.setDisableCache();
			xml.render(response);
		}
		else {
			throw new IllegalParameterException("action", actionParam);
		}
	}
	
	private boolean _incNumPasswordTries(Connection c, UserSession s, EapRequest request, EapResponse response) throws BLException {
		Integer numTries = (Integer)s.getAttribute(Constants.SATTR_SESSION_NUM_FAILED_PASSWORDS);
		if(numTries == null) {
			s.setAttribute(Constants.SATTR_SESSION_NUM_FAILED_PASSWORDS, 1);
			return false;
		}
		if(numTries > 2) {
			request.getEapContext().endSession(request, response, c, s, EndReason.PASSWORD, null);
			return true;
		}
		numTries++;
		s.setAttribute(Constants.SATTR_SESSION_NUM_FAILED_PASSWORDS, numTries);
		return false;
	}

}
