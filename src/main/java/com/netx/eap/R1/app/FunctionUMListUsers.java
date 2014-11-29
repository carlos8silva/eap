package com.netx.eap.R1.app;
import java.io.IOException;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.Table;
import com.netx.eap.R1.core.TableQuery;
import com.netx.eap.R1.core.Table.DATA_TYPE;


public class FunctionUMListUsers extends Function {

	private final Table _tbl1;
	
	public FunctionUMListUsers() {
		TableQuery query = new TableQuery("SELECT uv.user_id, uv.username, uv.full_name, uv.status, r.name, uv.time_created",
			"SELECT COUNT(uv.username)",
			"FROM eap_users_v uv, eap_user_roles ur, eap_roles r",
			"WHERE uv.username != \"su\" AND ur.user_id=uv.user_id AND ur.role_id=r.role_id AND ur.primary_role=\"T\""
		);
		_tbl1 = Table.create("um-list-users", query);
		_tbl1.setWidth(770);
		_tbl1.setCreateJsObjects(true);
		_tbl1.addScript("um-actions.js");
		_tbl1.addColumn("Username", "uv.username", 130, DATA_TYPE.TEXT, 2);
		_tbl1.addColumn("Full Name", "uv.full_name", 170, DATA_TYPE.TEXT, 3);
		_tbl1.addColumn("Primary Role", "r.name", 180, DATA_TYPE.TEXT, 5);
		_tbl1.addColumn("Status", "uv.status", 110, DATA_TYPE.LOV, 4, new String[] {"Active", "Disabled", "Locked"});
		_tbl1.addColumn("Date Created", "uv.time_created", 180, DATA_TYPE.DATE, 6);
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		_tbl1.setJsSnippet(request.getTemplate("templates/um-edit-user-actions.snp.js"));
		_tbl1.setFooterSnippet(request.getTemplate("templates/um-edit-user-buttons.snp.html"));
		// Build table:
		response.setDisableCache();
		_tbl1.build(request, "User Accounts", getAlias()).render(MimeTypes.TEXT_HTML, response);
	}
}
