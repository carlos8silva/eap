package com.netx.eap.R1.core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IllegalUsageException;
import com.netx.bl.R1.core.BLException;


public class TabbedTable {

	private final List<Tab> _tabs;

	public TabbedTable() {
		_tabs = new ArrayList<Tab>();
	}

	public void addTab(String title, int width, Table table) {
		Checker.checkEmpty(title, "title");
		Checker.checkMinValue(width, 10, "width");
		Checker.checkNull(table, "table");
		_tabs.add(new Tab(title, width, table));
	}

	public Template build(EapRequest request, String pageTitle, String formAlias) throws IOException, BLException {
		if(_tabs.size() < 2) {
			throw new IllegalUsageException("at least two tabs required for tabbed tables");
		}
		// Get tab parameter:
		Integer tab = request.getIntParameter("tab");
		if(tab == null) {
			tab = 1;
		}
		if(tab > _tabs.size()) {
			tab = _tabs.size();
		}
		// Render table:
		return _tabs.get(tab-1).table.build(request, pageTitle, formAlias, tab, _tabs);
	}
	
	// For Self and Table:
	class Tab {
		
		public final String title;
		public final int width;
		public final Table table;
		
		public Tab(String title, int width, Table table) {
			this.title = title;
			this.width = width;
			this.table = table;
		}
	}
}
