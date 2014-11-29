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


public class FunctionUMListRoles extends Function {

	private final Table _tbl1;

	public FunctionUMListRoles() {
		TableQuery query1 = new TableQuery("SELECT r.role_id, r.name, r.description, ui.name",
			"SELECT COUNT(r.role_id)",
			"FROM eap_roles r, eap_uinterfaces ui",
			"WHERE r.base_ui = ui.ui_id"
		);
		_tbl1 = Table.create("um-list-roles", query1);
		_tbl1.setWidth(770);
		_tbl1.addColumn("Name", "r.name", 150, DATA_TYPE.TEXT, 2);
		_tbl1.addColumn("Description", "r.description", 440, DATA_TYPE.TEXT, 3);
		_tbl1.addColumn("User Interface", "ui.name", 180, DATA_TYPE.TEXT, 4);
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		_tbl1.setJsSnippet(request.getTemplate("templates/um-edit-role-actions.snp.js"));
		_tbl1.setFooterSnippet(request.getTemplate("templates/um-edit-role-buttons.snp.html"));
		// Build table:
		response.setDisableCache();
		_tbl1.build(request, "Roles", getAlias()).render(MimeTypes.TEXT_HTML, response);
	}
}
