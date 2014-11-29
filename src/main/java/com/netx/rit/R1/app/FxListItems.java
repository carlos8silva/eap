package com.netx.rit.R1.app;
import java.io.IOException;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Preference;
import com.netx.eap.R1.bl.Preferences;
import com.netx.eap.R1.bl.User;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.IllegalParameterException;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.Table;
import com.netx.eap.R1.core.TableQuery;
import com.netx.eap.R1.core.Table.DATA_TYPE;
import com.netx.eap.R1.core.TableSettings;
import com.netx.eap.R1.core.ColFilter;
import com.netx.eap.R1.core.ColSorting;
import com.netx.eap.R1.core.Template;
import com.netx.eap.R1.core.Values;
import com.netx.rit.R1.bl.Projects;
import com.netx.rit.R1.bl.Project;


public class FxListItems extends ProjectFunction {

	private final TableQuery _query;
	
	public FxListItems() {
		String select = "SELECT r.item_id, r.title, r.description, r.status, r.priority, r.raised_by, r.assigned_to, r.time_created, r.date_due, r.mitigating_actions, u.time_updated, u.updated_by, u.description";
		String from = "FROM rit_items_v r LEFT JOIN rit_item_updates_v u ON r.item_id=u.item_id";
		String where = "WHERE (u.update_id IN (SELECT MAX(u2.update_id) FROM rit_item_updates u2 WHERE u2.item_id=r.item_id) OR u.update_id IS NULL)";
		_query = new TableQuery(select, "SELECT COUNT(r.item_id)", from, where);
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		// Check permissions:
		final Long projectId = request.getLongParameter("project_id", true);
		Connection c = request.getConnection();
		Project p = Projects.getInstance().get(c, projectId);
		checkPermission(request, p);
		// Prepare table, if necessary:
		final String tableId = getAlias()+"-"+projectId;
		Table t = Table.get(tableId);
		if(t == null) {
			TableSettings defaultTs = new TableSettings();
			defaultTs.put(new ColSorting("r.item_id", "asc"));
			defaultTs.put(new ColFilter("r.project_id", projectId.toString(), false));
			defaultTs.put(new ColFilter("r.status", new String[] {"Open"}));
			t = Table.create(tableId, _query, defaultTs);
			t.setTitleSection(false);
			t.setShowLastRow(false);
			t.setAlternativeCSS("rit-list.css");
			t.setHighlightColor(Table.COLOR.BLUE);
			t.setTargetPage("rit-edit-item.x?item_id=");
			t.addColumn("ID", "r.item_id", 30, DATA_TYPE.NUMBER, 1);
			t.addColumn("Title", "r.title", 130, DATA_TYPE.TEXT, 2);
			t.addColumn("Description", "r.description", 340, DATA_TYPE.TEXT, 3);
			t.addColumn("Status", "r.status", 70, DATA_TYPE.LOV, 4, new String[] {"Open", "Closed"});
			t.addColumn("Priority", "r.priority", 80, DATA_TYPE.TEXT, 5);
			t.addColumn("Date Raised", "r.time_created", 100, DATA_TYPE.DATE, 8);
			t.addColumn("Date Due", "r.date_due", 100, DATA_TYPE.DATE, 9);
			//t.addColumn("Raised By", "r.raised_by", 100, DATA_TYPE.TEXT, 6);
			t.addColumn("Assigned To", "r.assigned_to", 100, DATA_TYPE.TEXT, 7);
			t.addColumn("Mitigating Actions", "r.mitigating_actions", 190, DATA_TYPE.TEXT, 10);
			t.addColumn("Last Update", "u.description", 180, DATA_TYPE.TEXT, 13);
			t.addColumn("Updated By", "u.updated_by", 100, DATA_TYPE.TEXT, 12);
			t.addParameter("project_id", projectId.toString());
		}
		// Format response:
		Template header = request.getTemplate("templates/rit-list-items-header.snp.html");
		Values v = header.getValues();
		v.set("id", p.getProjectId());
		v.set("project-name", p.getName());
		String event = request.getParameter("event");
		if(event != null) {
			if(event.equals("save-default")) {
				Values vIfSaved = v.setIf("saved-default", true);
				vIfSaved.set("name", p.getName());
			}
			else {
				throw new IllegalParameterException("event", event);
			}
		}
		t.setHeaderSnippet(header);
		c.close();
		// Render table:
		response.setDisableCache();
		t.build(request, "Risks and Issues", getAlias()).render(MimeTypes.TEXT_HTML, response);
	}

	protected void doPost(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		final Long projectId = request.getLongParameter("project_id", true);
		final String action = request.getParameter("action", true);
		Connection c = request.getConnection();
		final User u = request.getUserSession().getUser(c);
		if(action.equals("save-default")) {
			Preference pref = new Preference(u.getUserId(), Constants.PREF_DEFAULT_PROJ);
			pref.setValue(projectId.toString());
			Preferences.getInstance().save(c, pref);
		}
		else {
			throw new IllegalParameterException("action", action);
		}
		response.sendRedirect(getAliasURL()+"?project_id="+projectId+"&event="+action);
	}
}
