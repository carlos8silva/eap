package com.netx.eap.R1.app;
import java.io.IOException;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.TabbedTable;
import com.netx.eap.R1.core.Table;
import com.netx.eap.R1.core.TableQuery;
import com.netx.eap.R1.core.Table.DATA_TYPE;


public class FunctionSAListSessions extends Function {

	private final TabbedTable _tabbedTbl;
	
	public FunctionSAListSessions() {
		_tabbedTbl = new TabbedTable();
		// Tab1:
		TableQuery query1 = new TableQuery(
			"SELECT s.session_id AS session_id, u.username AS username, s.ip_address AS ip_address, s.start_time AS start_time, s.end_time AS end_time, s.end_reason AS end_reason",
			"SELECT COUNT(s.session_id)",
			"FROM eap_sessions s, eap_users u",
			"WHERE s.user_id=u.user_id AND u.time_disabled IS NULL"
		);
		Table tbl1 = Table.create("sa-list-sessions-1", query1);
		tbl1.setWidth(770);
		tbl1.setTargetPage("sa.user-sessions.x?id=");
		tbl1.addColumn("Session ID", "session_id", 130, DATA_TYPE.TEXT, 1);
		tbl1.addColumn("Username", "username", 130, DATA_TYPE.TEXT, 2);
		tbl1.addColumn("IP Address", "ip_address", 110, DATA_TYPE.TEXT, 3);
		tbl1.addColumn("Start Date", "start_time", 150, DATA_TYPE.DATETIME, 4);
		tbl1.addColumn("End Date", "end_time", 150, DATA_TYPE.DATETIME, 5);
		tbl1.addColumn("End Reason", "end_reason", 100, DATA_TYPE.TEXT, 6);
		_tabbedTbl.addTab("Current users", 100, tbl1);
		// Tab 2:
		TableQuery query2 = new TableQuery(
			"SELECT s.session_id AS session_id, u.old_username AS username, s.ip_address AS ip_address, s.start_time AS start_time, s.end_time AS end_time",
			"SELECT COUNT(s.session_id)",
			"FROM eap_sessions s, eap_users u",
			"WHERE s.user_id=u.user_id AND u.time_disabled IS NOT NULL"
		);
		Table tbl2 = Table.create("sa-list-sessions-2", query2);
		tbl2.setWidth(770);
		tbl2.setTargetPage("sa.user-sessions.x?id=");
		tbl2.addColumn("Session ID", "session_id", 150, DATA_TYPE.TEXT, 1);
		tbl2.addColumn("Username", "username", 130, DATA_TYPE.TEXT, 2);
		tbl2.addColumn("IP Address", "ip_address", 130, DATA_TYPE.TEXT, 3);
		tbl2.addColumn("Start Date", "start_time", 180, DATA_TYPE.DATETIME, 4);
		tbl2.addColumn("End Date", "end_time", 180, DATA_TYPE.DATETIME, 5);
		_tabbedTbl.addTab("Disabled users", 110, tbl2);
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		response.setDisableCache();
		// Build table:
		_tabbedTbl.build(request, "User Sessions", getAlias()).render(MimeTypes.TEXT_HTML, response);
	}
}
