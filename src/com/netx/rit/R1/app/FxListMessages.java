package com.netx.rit.R1.app;
import java.text.SimpleDateFormat;
import java.io.IOException;
import com.netx.bl.R1.core.Row;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.ColSorting;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.Function;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.eap.R1.core.Table;
import com.netx.eap.R1.core.TableQuery;
import com.netx.eap.R1.core.TabbedTable;
import com.netx.eap.R1.core.TableSettings;
import com.netx.eap.R1.core.Table.DATA_TYPE;


public class FxListMessages extends Function {

	private final String _select = "SELECT m.conversation_id, m.time_sent, m.time_read, m.subject, "
		+"CASE WHEN u.username=m.from_user THEN \"me\" ELSE from_user_name END AS from_user, "
		+"CASE WHEN u.username=m.to_users THEN \"me\" ELSE to_users_name END AS to_users, "
		+"up.time_accepted, up.time_rejected";
	private final String _count = "SELECT COUNT(m.conversation_id)";
	private final String _from = "FROM eap_messages_v m"
		+" INNER JOIN eap_users u ON u.user_id=m.user_id"
		+" INNER JOIN (SELECT MAX(message_id) AS message_id FROM eap_messages_v GROUP BY conversation_id) AS x ON m.message_id=x.message_id"
		+" LEFT JOIN rit_user_projects up ON up.conversation_id=m.conversation_id";
	private final String _where = "WHERE m.user_id=";
	private final SimpleDateFormat _df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public FxListMessages() {
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		Long myId = request.getUserSession().getUserId();
		final String inboxTableId = getAlias()+"-"+myId+"-current";
		final String sentTableId = getAlias()+"-"+myId+"-sent";
		final String unreadTableId = getAlias()+"-"+myId+"-unread";
		final String archivedTableId = getAlias()+"-"+myId+"-archived";
		Table t1 = Table.get(inboxTableId);
		Table t2 = Table.get(sentTableId);
		Table t3 = Table.get(unreadTableId);
		Table t4 = Table.get(archivedTableId);
		if(t1 == null) {
			TableSettings defaultTs = new TableSettings();
			defaultTs.put(new ColSorting("time_sent", "desc"));
			t1 = Table.create(inboxTableId, new TableQuery(_select, _count, _from, _where+myId+" AND time_archived IS NULL"), defaultTs);
			t2 = Table.create(sentTableId, new TableQuery(_select, _count, _from, _where+myId+" AND time_archived IS NULL AND c.user_from=u.username"), defaultTs);
			t3 = Table.create(unreadTableId, new TableQuery(_select, _count, _from, _where+myId+" AND c.time_read IS NULL"), defaultTs);
			t4 = Table.create(archivedTableId, new TableQuery(_select, _count, _from, _where+myId+" AND time_archived IS NOT NULL"), defaultTs);
			t1.setWidth(770);
			t2.setWidth(770);
			t3.setWidth(770);
			t4.setWidth(770);
			t1.setJsSnippet(request.getTemplate("templates/rit-list-messages.snp.js"));
			t1.setFooterSnippet(request.getTemplate("templates/rit-list-messages.snp.html"));
			t1.setCreateJsObjects(true);
			//t1.setHighlightColor(Table.COLOR.BLUE);
			//t2.setHighlightColor(Table.COLOR.BLUE);
			t1.setDateTimeFormat(_df);
			t2.setDateTimeFormat(_df);
			t3.setDateTimeFormat(_df);
			t4.setDateTimeFormat(_df);
			Table.DisplayCriteria dCriteria = new Table.DisplayCriteria() {
				public boolean makeBold(Row r) {
					return r.getString("time_read") == null;
				}
			};
			t1.setDisplayCriteria(dCriteria);
			t1.addColumn("From", "from_user", 150, DATA_TYPE.TEXT, 5);
			t3.addColumn("From", "from_user", 150, DATA_TYPE.TEXT, 5);
			t4.addColumn("From", "from_user", 150, DATA_TYPE.TEXT, 5);
			t1.addColumn("To", "to_users", 150, DATA_TYPE.TEXT, 6);
			t2.addColumn("To", "to_users", 150, DATA_TYPE.TEXT, 6);
			t3.addColumn("To", "to_users", 150, DATA_TYPE.TEXT, 6);
			t4.addColumn("To", "to_users", 150, DATA_TYPE.TEXT, 6);
			t1.addColumn("Subject", "subject", 350, DATA_TYPE.TEXT, 4, true);
			t2.addColumn("Subject", "subject", 500, DATA_TYPE.TEXT, 4, true);
			t3.addColumn("Subject", "subject", 350, DATA_TYPE.TEXT, 4, true);
			t4.addColumn("Subject", "subject", 350, DATA_TYPE.TEXT, 4, true);
			t1.addColumn("Sent", "time_sent", 120, DATA_TYPE.DATETIME, 2);
			t2.addColumn("Sent", "time_sent", 120, DATA_TYPE.DATETIME, 2);
			t3.addColumn("Sent", "time_sent", 120, DATA_TYPE.DATETIME, 2);
			t4.addColumn("Sent", "time_sent", 120, DATA_TYPE.DATETIME, 2);
		}
		// Build table:
		TabbedTable t = new TabbedTable();
		t.addTab("Inbox", 80, t1);
		t.addTab("Sent", 80, t2);
		t.addTab("Unread", 80, t3);
		t.addTab("Archived", 80, t4);
		response.setDisableCache();
		t.build(request, "Messages", getAlias()).render(MimeTypes.TEXT_HTML, response);
	}
}
