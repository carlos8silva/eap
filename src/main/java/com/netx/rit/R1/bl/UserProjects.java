package com.netx.rit.R1.bl;
import java.util.List;

import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.bl.User;


public class UserProjects extends Entity<UserProjectsMetaData,UserProject> {

	// TYPE:
	public static UserProjects getInstance() {
		return RIT.getUserProjects();
	}

	// INSTANCE:
	private Select _qSelectUserProjects = null;
	private Select _qSelectAccessRequests = null;
	private Select _qSelectByConversation = null;

	UserProjects() {
		super(new UserProjectsMetaData());
	}

	protected void onLoad() {
		_qSelectUserProjects = createSelect("select-user-projects", "SELECT * FROM rit_user_projects WHERE user_id = ? AND time_accepted IS NOT NULL");
		_qSelectAccessRequests = createSelect("select-access-requests", "SELECT * FROM rit_user_projects WHERE user_id = ?");
		_qSelectByConversation = createSelect("select-by-conversation", "SELECT * FROM rit_user_projects WHERE conversation_id = ?");
	}

	public void create(Connection c, UserProject up) throws BLException {
		insert(c, up);
	}
	
	public void update(Connection c, UserProject up) throws BLException {
		updateInstance(c, up);
	}
	
	// TODO see if the three methods below should be refactored using relationships
	public List<UserProject> listProjectsFor(Connection c, User user) throws BLException {
		Checker.checkNull(user, "user");
		return selectList(c, _qSelectUserProjects, user.getUserId());
	}

	public List<UserProject> listAccessRequestsFor(Connection c, User user) throws BLException {
		Checker.checkNull(user, "user");
		return selectList(c, _qSelectAccessRequests, user.getUserId());
	}

	public UserProject getByConversation(Connection c, Long convId) throws BLException {
		Checker.checkNull(convId, "convId");
		return selectInstance(c, _qSelectByConversation, convId);
	}
}
