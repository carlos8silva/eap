package com.netx.rit.R1.app;
import java.util.List;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.UserSession;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.NotAuthorizedException;
import com.netx.rit.R1.bl.UserProject;
import com.netx.rit.R1.bl.UserProjects;
import com.netx.rit.R1.bl.Project;


public abstract class ProjectFunction extends Function {
	
	protected void checkPermission(EapRequest request, Project p) throws BLException {
		UserSession s = request.getUserSession();
		Connection c = request.getConnection();
		List<UserProject> uProjects = UserProjects.getInstance().listProjectsFor(c, s.getUser(c));
		c.close();
		for(UserProject up : uProjects) {
			if(up.getProjectId().equals(p.getProjectId())) {
				return;
			}
		}
		throw new NotAuthorizedException();
	}
}
