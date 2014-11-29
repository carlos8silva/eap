package com.netx.rit.R1.app;
import java.util.List;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Preferences;
import com.netx.eap.R1.bl.Preference;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.Template;
import com.netx.eap.R1.core.Values;
import com.netx.eap.R1.core.ValueList;
import com.netx.eap.R1.bl.User;
import com.netx.rit.R1.bl.UserProjects;
import com.netx.rit.R1.bl.UserProject;
import com.netx.rit.R1.bl.Project;


public class FxUserInterfaceRIT extends Function {

	protected void doGet(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		Template startPage = request.getTemplate("templates/rm-tracker.html");
		Values v = startPage.getValues();
		Connection c = request.getConnection();
		// Screen name:
		final User u = request.getUserSession().getUser(c);
		v.set("screen-name", u.getFullName());
		// Projects menu:
		List<UserProject> list = UserProjects.getInstance().listProjectsFor(c, u);
		Values vIfHasPrjs = v.setIf("has-projects-1", !list.isEmpty());
		if(!list.isEmpty()) {
			ValueList vProjects = vIfHasPrjs.getList("projects");
			for(UserProject up : list) {
				Values vPrj = vProjects.next();
				Project p = up.getProject(c);
				vPrj.set("name", p.getName());
				vPrj.set("id", p.getProjectId());
			}
		}
		// List Risks tracker:
		vIfHasPrjs = v.setIf("has-projects-2", !list.isEmpty());
		if(!list.isEmpty()) {
			// Default project:
			Preference pref = Preferences.getInstance().get(c, u.getUserId(), Constants.PREF_DEFAULT_PROJ);
			if(pref == null) {
				vIfHasPrjs.set("id", list.get(0).getProjectId());
			}
			else {
				vIfHasPrjs.set("id", Long.valueOf(pref.getValue()));
			}
		}
		// Render response:
		response.setDisableCache();
		startPage.render(MimeTypes.TEXT_HTML, response);
	}
}
