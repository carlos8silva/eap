package com.netx.eap.R1.core;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.Time;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.Strings;
import com.netx.generics.R1.collections.Property;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.eh.IllegalUsageException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.Results;
import com.netx.bl.R1.core.Row;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.bl.Preferences;
import com.netx.eap.R1.core.TabbedTable.Tab;


public class Table {

	// TYPE:
	public static enum DATA_TYPE {TEXT, NUMBER, DATE, TIME, DATETIME, LOV};
	public static enum COLOR {GREEN, BLUE};
	private static final int _CELL_PADDING = 7+5;
	private static final int _COL_WIDTH_ICON = 10+_CELL_PADDING;
	private static final int _COL_WIDTH_TBL = 16+_CELL_PADDING;
	private static final int _RESULTS_WIDTH = 80;
	private static final double _PX_PER_CHAR_NORMAL = 350D/57D;
	private static final double _PX_PER_CHAR_BOLD = 350D/55D;
	private static final String _BLANKS = "(blanks)";
	private static final Map<String,Table> _tables = new HashMap<String,Table>();
	
	public static Table create(String tableId, TableQuery query, TableSettings defaultTs) {
		Checker.checkEmpty(tableId, "tableId");
		Checker.checkNull(query, "query");
		if(_tables.get(tableId) != null) {
			throw new IllegalArgumentException("table with id '"+tableId+"' has already been registered");
		}
		Table t = new Table(tableId, query, defaultTs);
		_tables.put(tableId, t);
		return t;
	}

	public static Table create(String tableId, TableQuery query) {
		return create(tableId, query, null);
	}

	public static Table get(String tableId) {
		return _tables.get(tableId);
	}

	// INSTANCE:
	private final String _id;
	private final TableQuery _query;
	private final List<Column> _columns;
	private final List<Property> _params;
	private int _totalWidth = 0;
	private List<String> _scripts = null;
	private Map<String, Template> _snippets = null;
	// Configurable properties:
	private final TableSettings _defaultTs;
	private int _maxWidth = -1;
	// TODO items per page should be a preference
	private int _itemsPerPage = 15;
	private String _targetPage = "";
	private int _idColumn = 1;
	private boolean _createJsObjects = false;
	private boolean _showTitleSection = true;
	private boolean _showLastRow = true;
	private String _altCSS = null;
	private COLOR _color = COLOR.GREEN;
	private DateFormat _dateFormat = null;
	private DateFormat _timeFormat = null;
	private DateFormat _dateTimeFormat = null;
	private DisplayCriteria _dCriteria = null;

	private Table(String tableId, TableQuery query, TableSettings settings) {
		_id = tableId;
		_query = query;
		_columns = new ArrayList<Column>();
		_params = new ArrayList<Property>();
		_defaultTs = settings;
	}

	public void setIdColumn(int columnIndex) {
		_idColumn = columnIndex;
	}
	
	public void setTargetPage(String targetPage) {
		Checker.checkEmpty(targetPage, "targetPage");
		_targetPage = targetPage;
	}

	public void setWidth(int maxWidth) {
		Checker.checkMinValue(maxWidth, 100, "maxWidth");
		_maxWidth = maxWidth;
	}

	public void setItemsPerPage(int itemsPerPage) {
		Checker.checkMinValue(itemsPerPage, 10, "itemsPerPage");
		_itemsPerPage = itemsPerPage;
	}

	public void setCreateJsObjects(boolean createJsObjects) {
		_createJsObjects = createJsObjects;
	}

	public void setTitleSection(boolean showTitleSection) {
		_showTitleSection = showTitleSection;
	}

	public void setShowLastRow(boolean showLastRow) {
		_showLastRow = showLastRow;
	}

	public void setAlternativeCSS(String altCSS) {
		Checker.checkEmpty(altCSS, "altCSS");
		_altCSS = altCSS;
	}

	public void setHighlightColor(COLOR color) {
		Checker.checkNull(color, "color");
		_color = color;
	}

	public void setDateFormat(DateFormat df) {
		_dateFormat = df;
	}
	
	public void setTimeFormat(DateFormat df) {
		_timeFormat = df;
	}

	public void setDateTimeFormat(DateFormat df) {
		_dateTimeFormat = df;
	}

	public void setDisplayCriteria(DisplayCriteria fc) {
		_dCriteria= fc;
	}

	public void addScript(String filename) {
		Checker.checkEmpty(filename, "filename");
		if(_scripts == null) {
			_scripts = new ArrayList<String>();
		}
		_scripts.add(filename);
	}

	public void setJsSnippet(Template snippet) {
		Checker.checkNull(snippet, "snippet");
		_setSnippet("snp-js", snippet);
	}

	public void setHeaderSnippet(Template snippet) {
		Checker.checkNull(snippet, "snippet");
		_setSnippet("snp-header", snippet);
	}

	public void setFooterSnippet(Template snippet) {
		Checker.checkNull(snippet, "snippet");
		_setSnippet("snp-footer", snippet);
	}

	public void addColumn(String title, String columnId, int width, DATA_TYPE dt, Integer colIndex, boolean trimChars) {
		_addColumn(title, columnId, width, dt, colIndex, null, trimChars);
	}

	public void addColumn(String title, String columnId, int width, DATA_TYPE dt, Integer colIndex) {
		_addColumn(title, columnId, width, dt, colIndex, null, false);
	}

	public void addColumn(String title, String columnId, int width, DATA_TYPE dt, Integer colIndex, String[] values) {
		_addColumn(title, columnId, width, dt, colIndex, values, false);
	}

	public void addParameter(String name, String value) {
		_params.add(new Property(name, value));
	}

	public Template build(EapRequest request, String pageTitle, String formAlias) throws IOException, BLException {
		return build(request, pageTitle, formAlias, 1, null, null);
	}

	public Template build(EapRequest request, String pageTitle, String formAlias, TableSettings additional) throws IOException, BLException {
		return build(request, pageTitle, formAlias, 1, null, additional);
	}

	public Template build(EapRequest request, String pageTitle, String formAlias, Integer tab, List<Tab> tabs) throws IOException, BLException {
		return build(request, pageTitle, formAlias, tab, tabs, null);
	}

	// For Self and TabbedTable:
	Template build(EapRequest request, String pageTitle, String formAlias, Integer tab, List<Tab> tabs, TableSettings additional) throws IOException, BLException {
		Checker.checkEmpty(pageTitle, "pageTitle");
		Checker.checkNull(request, "request");
		Checker.checkEmpty(formAlias, "formAlias");

		// Fail if column sizes are not consistent:
		if(_maxWidth > 0) {
			int tblWidth = 0;
			for(Column c : _columns) {
				tblWidth += c.width;
			}
			if(tblWidth != _maxWidth) {
				throw new IllegalUsageException("inconsistent table widths: column width sum is "+tblWidth+" but maximum table width is "+_maxWidth);
			}
		}

		// Get page parameter:
		Integer page = request.getIntParameter("page");
		if(page == null) {
			page = 1;
		}
		
		// Prepare filtering and sorting settings:
		UserSession s = request.getUserSession();
		String tableId = Constants.SATTR_TABLE_SETTINGS+"."+_id;
		TableSettings settings = (TableSettings)s.getAttribute(tableId);
		if(settings == null) {
			Connection c = request.getConnection();
			settings = Preferences.getInstance().getTableSettingsFor(c, s.getUserId(), tableId);
			c.close();
			if(settings == null) {
				settings = _defaultTs==null ? new TableSettings() : _defaultTs;
			}
		}
		if(additional != null) {
			settings.apply(additional);
		}
		String action = request.getParameter("action");
		if(action != null) {
			String field = request.getParameter("field", true);
			if(action.equals("add-filter")) {
				settings.numRows = 0;
				// Find column data type:
				DATA_TYPE dataType = null;
				for(Column c : _columns) {
					if(c.colAlias.equals(field)) {
						dataType = c.dataType;
						break;
					}
				}
				if(dataType == null) {
					throw new IllegalParameterException("field", field);
				}
				// Add the new filter:
				if(dataType == DATA_TYPE.TEXT) {
					boolean includeBlanks = request.getParameter("blanks") != null;
					String expr = request.getParameter("expr", false);
					if(expr != null || includeBlanks) {
						settings.put(new ColFilter(field, expr, includeBlanks));
					}
				}
				else if(dataType == DATA_TYPE.LOV) {
					String[] values = request.getParameterValues("lov");
					// Note: if values is null, it means that no options have been checked
					if(values != null) {
						settings.put(new ColFilter(field, values));
					}
				}
				else {
					boolean includeBlanks = request.getParameter("blanks") != null;
					String start = request.getParameter("start", false);
					String end = request.getParameter("end", false);
					if(start != null || end != null || includeBlanks) {
						settings.put(new ColFilter(field, start, end, includeBlanks));
					}
				}
			}
			else if(action.equals("remove-filter")) {
				settings.removeFilter(field);
				settings.numRows = 0;
			}
			else if(action.equals("add-sorting")) {
				ColSorting sorting = new ColSorting(field, request.getParameter("order", true));
				settings.put(sorting);
			}
			else if(action.equals("remove-sorting")) {
				settings.removeSorting(field);
			}
			else {
				throw new IllegalParameterException("action", action);
			}
			// Save settings:
			Connection c = request.getConnection();
			// TODO find better solution for performance; do we really need to
			// save settings all the time a user changes sorting or filters?
			Preferences.getInstance().save(c, tableId, s.getUserId(), settings);
			c.close();
		}
		s.setAttribute(tableId, settings);
		
		// Build query according to settings:
		StringBuilder dataSQL = new StringBuilder();
		dataSQL.append(_query.baseSQL);
		dataSQL.append(" ");
		dataSQL.append(_query.fromSQL);
		dataSQL.append(" ");
		StringBuilder whereSQL = new StringBuilder();
		if(_query.whereSQL != null) {
			whereSQL.append(_query.whereSQL);
		}
		else {
			whereSQL.append("WHERE 1=1");
		}
		whereSQL.append(" ");
		Collection<ColFilter> filters = settings.getFilters();
		if(!filters.isEmpty()) {
			whereSQL.append("AND ");
			Iterator<ColFilter> itFilters = settings.getFilters().iterator();
			while(itFilters.hasNext()) {
				ColFilter filter = itFilters.next();
				// TEXT:
				if(filter.expr != null) {
					if(filter.includeBlanks) {
						whereSQL.append(" (");
					}
					whereSQL.append(filter.field);
					whereSQL.append(" LIKE \"");
					whereSQL.append(Strings.replaceAll(filter.expr, "*", "%"));
					whereSQL.append("\" ");
					_addBlanksSQL(filter, whereSQL);
				}
				// LOV:
				else if(filter.values != null) {
					if(filter.includeBlanks) {
						whereSQL.append(" (");
					}
					whereSQL.append(filter.field);
					whereSQL.append(" IN (");
					for(int i=0; i<filter.values.length; i++) {
						whereSQL.append("\"");
						String value = filter.values[i];
						if(value.equals(_BLANKS)) {
							value = "NULL";
						}
						whereSQL.append(value);
						whereSQL.append("\"");
						if(i < filter.values.length-1) {
							whereSQL.append(", ");
						}
					}
					whereSQL.append(") ");
					_addBlanksSQL(filter, whereSQL);
				}
				// RANGE:
				else if(filter.start != null || filter.end != null) {
					if(filter.includeBlanks) {
						whereSQL.append(" (");
					}
					whereSQL.append(" (");
					whereSQL.append(filter.field);
					if(filter.start != null) {
						whereSQL.append(" >= \"");
						whereSQL.append(filter.start);
						whereSQL.append("\"");
					}
					if(filter.end != null) {
						if(filter.start != null) {
							whereSQL.append(" AND ");
							whereSQL.append(filter.field);
						}
						whereSQL.append(" <= \"");
						whereSQL.append(filter.end);
						whereSQL.append("\"");
					}
					whereSQL.append(") ");
					_addBlanksSQL(filter, whereSQL);
				}
				// NULL (blanks):
				else {
					if(filter.includeBlanks) {
						whereSQL.append(filter.field);
						whereSQL.append(" IS NULL ");
					}
				}
				if(itFilters.hasNext()) {
					whereSQL.append("AND ");
				}
			}
		}
		dataSQL.append(whereSQL);
		Collection<ColSorting> order = settings.getSorting();
		if(!order.isEmpty()) {
			dataSQL.append("ORDER BY ");
			Iterator<ColSorting> itSorting = order.iterator();
			while(itSorting.hasNext()) {
				ColSorting sorting = itSorting.next();
				dataSQL.append(sorting.field);
				dataSQL.append(" ");
				dataSQL.append(sorting.order.toUpperCase());
				if(itSorting.hasNext()) {
					dataSQL.append(", ");
				}
			}
		}
		
		// Retrieve the total result set size:
		Connection c = request.getConnection();
		if(settings.numRows == 0) {
			StringBuilder countSQL = new StringBuilder();
			countSQL.append(_query.countSQL);
			countSQL.append(" ");
			countSQL.append(_query.fromSQL);
			countSQL.append(" ");
			countSQL.append(whereSQL);
			Results count = c.select(countSQL.toString());
			if(count.getRowCount() > 1) {
				c.close();
				throw new IllegalUsageException("count query returned wrong number of rows: "+count.getRowCount());
			}
			settings.numRows = count.getRows().get(0).getInt(1);
		}

		// Pagination:
		int numPages = settings.numRows / _itemsPerPage;
		int itemsOnLastPage = settings.numRows % _itemsPerPage; 
		if(itemsOnLastPage > 0) {
			numPages++;
		}
		if(page > numPages) {
			page = numPages;
		}
		if(page <= 0) {
			page = 1;
		}
		int start = page == 1 ? 0 : (page-1) * _itemsPerPage;
		dataSQL.append(" LIMIT ");
		dataSQL.append(start);
		dataSQL.append(", ");
		dataSQL.append(_itemsPerPage);

		// Retrieve data:
		Results results = c.select(dataSQL.toString());
		results.setDateFormat(_dateFormat);
		results.setTimeFormat(_timeFormat);
		results.setDateTimeFormat(_dateTimeFormat);
		c.close();
		if(results.getColumnCount() < _columns.size()) {
			throw new IllegalUsageException("number of columns returned by query ("+results.getColumnCount()+") is smaller than number of expected columns ("+_columns.size()+")");
		}

		// Create template:
		Template tplt = request.getTemplate("templates/all-frm-table.html", _snippets);
		Values v = tplt.getValues();
		v.set("title", pageTitle);
		v.set("target", _targetPage);
		v.set("table-width", _totalWidth);
		v.setIf("color-green", _color==COLOR.GREEN);
		if(_showTitleSection) {
			Values ifHasTitle = v.setIf("has-title", true);
			ifHasTitle.set("title", pageTitle);
		}
		if(_altCSS != null) {
			Values ifAltCSS = v.setIf("alt-css", true);
			ifAltCSS.set("css", _altCSS);
		}
		
		// JS scripts:
		if(_scripts != null) {
			ValueList vlScripts = v.getList("scripts");
			for(String filename : _scripts) {
				Values vScript = vlScripts.next();
				vScript.set("path", filename);
			}
		}

		// Tabs:
		if(tabs != null) {
			Values vIfTabs = v.setIf("has-tabs", true);
			ValueList vlTabs = vIfTabs.getList("tabs");
			Iterator<Tab> itTabs = tabs.iterator();
			int totalWidth = 0;
			for(int i=1; itTabs.hasNext(); i++) {
				Tab t = itTabs.next();
				Values vTab = vlTabs.next();
				Values vIfSelected = vTab.setIf("selected", i == tab);
				vIfSelected.set("title", t.title);
				vIfSelected.set("width", t.width);
				totalWidth += (t.width + 1);
				if(i != tab) {
					vIfSelected.set("function", formAlias);
					vIfSelected.set("tab", i);
				}
			}
			vIfTabs.set("width", _totalWidth-totalWidth);
		}
		
		// Display columns:
		ValueList vlColumns = v.getList("columns");
		Iterator<Column> it = _columns.iterator();
		for(int i=1; it.hasNext(); i++) {
			Column col = it.next();
			int borderWidth = it.hasNext() ? 1 : 0;
			Values vColumn = vlColumns.next();
			Values vIf = vColumn.setIf("first", i==1);
			if(i != 1) {
				vIf = vIf.setIf("last", !it.hasNext());
			}
			vIf.set("i", i);
			vIf.set("width-1", col.width - _CELL_PADDING - borderWidth);
			vColumn.set("name", col.title);
			if(settings.getFilter(col.colAlias) != null) {
				Values vIfIcon = vColumn.setIf("icon", true);
				vIfIcon.set("icon", "icon-tbl-col-filter.png");
				vColumn.set("width-2", col.width - _COL_WIDTH_ICON - borderWidth);
			}
			else {
				ColSorting cs = settings.getSorting(col.colAlias);
				if(cs == null) {
					Values vIfIcon = vColumn.setIf("icon", true);
					vIfIcon.set("icon", "icon-tbl-col-none.png");
					vColumn.set("width-2", col.width - _COL_WIDTH_ICON - borderWidth);
				}
				else {
					if(settings.getFilter(col.colAlias) == null && order.size() == 1) {
						Values vIfIcon = vColumn.setIf("icon", true);
						vIfIcon.set("icon", cs.order.equals("asc") ? "icon-tbl-col-asc.png" : "icon-tbl-col-desc.png");
						vColumn.set("width-2", col.width - _COL_WIDTH_ICON - borderWidth);
					}
					else {
						Values vIfIcon = vColumn.setIf("icon", false);
						vIfIcon.set("order", settings.getSortingOrder(col.colAlias));
						vColumn.set("width-2", col.width - _COL_WIDTH_TBL - borderWidth);
					}
				}
			}
		}

		// Display data:
		ValueList vlData = v.getList("data");
		List<Row> rows = results.getRows();
		Iterator<Row> itRows = rows.iterator();
		for(int i=0; itRows.hasNext(); i++) {
			Row row = itRows.next();
			boolean makeBold = _dCriteria==null ? false : _dCriteria.makeBold(row);
			Values vData = vlData.next();
			vData.set("i", i+"");
			vData.set("id", row.getString(_idColumn));
			ValueList vlAttrs = vData.getList("attributes");
			it = _columns.iterator();
			for(int j=1; it.hasNext(); j++) {
				Column col = it.next();
				Values vAttr = vlAttrs.next();
				Values vIf = vAttr.setIf("first", j==1);
				if(j != 1) {
					vIf = vIf.setIf("last", !it.hasNext());
				}
				String value = null;
				if(col.dataType == DATA_TYPE.DATE) {
					Date date = row.getDate(col.colIndex);
					if(date != null) {
						value = date.format(results.getDateFormat());
					}
				}
				else if(col.dataType == DATA_TYPE.TIME) {
					Time time = row.getTime(col.colIndex);
					if(time != null) {
						value = time.format(results.getTimeFormat());
					}
				}
				else if(col.dataType == DATA_TYPE.DATETIME) {
					Timestamp ts = row.getTimestamp(col.colIndex);
					if(ts != null) {
						value = ts.format(results.getDateTimeFormat());
					}
				}
				else if(col.dataType == DATA_TYPE.NUMBER || col.dataType == DATA_TYPE.TEXT || col.dataType == DATA_TYPE.LOV) {
					value = col.trimChars(row.getString(col.colIndex), makeBold);
				}
				else {
					throw new IntegrityException(col.dataType);
				}
				vIf = vIf.setIf("bold", makeBold);
				vIf.set("value", value);
			}
		}
		
		// Display last row:
		Values vIfShowLastRow = v.setIf("show-last-row", _showLastRow);
		if(_showLastRow){
			ValueList vlLastRow = vIfShowLastRow.getList("last-row");
			for(int i=0; i<_columns.size(); i++) {
				Values vLastRow = vlLastRow.next();
				Values vIf = vLastRow.setIf("first", i==0);
				if(i != 0) {
					vIf = vIf.setIf("last", i==_columns.size()-1);
				}

			}
		}
		
		// Add menus:
		ValueList vlMenus = v.getList("menus");
		String params = _getParams();
		it = _columns.iterator();
		for(int i=0; it.hasNext(); i++) {
			Column col = it.next();
			Values vMenu = vlMenus.next();
			vMenu.set("i", i+1);
			vMenu.setIf("shift", i!=0);
			vMenu.set("title", col.title);
			boolean hasFilter = settings.getFilter(col.colAlias) != null;
			Values vIfFilter = vMenu.setIf("filter", hasFilter);
			vIfFilter.set("col-name", col.colAlias);
			if(col.dataType==DATA_TYPE.TEXT) {
				vIfFilter.set("form-type", "text");
			}
			else if(col.dataType==DATA_TYPE.LOV) {
				vIfFilter.set("form-type", "lov");
			}
			else {
				vIfFilter.set("form-type", "range");
			}
			if(hasFilter) {
				vIfFilter.set("function", formAlias);
				vIfFilter.set("tab", tab);
				vIfFilter.set("params", params);
			}
			ColSorting sorting = settings.getSorting(col.colAlias);
			Values vIfSort = null;
			if(sorting == null) {
				vIfSort = vMenu.setIf("sort-asc", true);
				vIfSort.set("function", formAlias);
				vIfSort.set("col-name", col.colAlias);
				vIfSort.set("tab", tab);
				vIfSort.set("params", params);
				vIfSort = vMenu.setIf("sort-desc", true);
				vIfSort.set("function", formAlias);
				vIfSort.set("col-name", col.colAlias);
				vIfSort.set("tab", tab);
				vIfSort.set("params", params);
			}
			else {
				if(sorting.order.equals("asc")) {
					vIfSort = vMenu.setIf("sort-desc", true);
				}
				else {
					vIfSort = vMenu.setIf("sort-asc", true);
				}
				vIfSort.set("function", formAlias);
				vIfSort.set("col-name", col.colAlias);
				vIfSort.set("tab", tab);
				vIfSort.set("params", params);
				vIfSort = vMenu.setIf("sort-rem", true);
				vIfSort.set("function", formAlias);
				vIfSort.set("col-name", col.colAlias);
				vIfSort.set("tab", tab);
				vIfSort.set("params", params);
			}
		}

		// Add forms:
		ValueList vlForms = v.getList("forms");
		it = _columns.iterator();
		while(it.hasNext()) {
			Column col = it.next();
			Values vForm = vlForms.next();
			vForm.set("function", formAlias);
			vForm.set("col-name", col.colAlias);
			vForm.set("tab", tab);
			if(!_params.isEmpty()) {
				ValueList vlParams = vForm.getList("params");
				for(Property p : _params) {
					Values vParam = vlParams.next();
					vParam.set("name", p.getKey());
					vParam.set("value", p.getValue());
				}
			}
			ColFilter f = settings.getFilter(col.colAlias);
			boolean isExpr = col.dataType == DATA_TYPE.TEXT;
			Values vIfExpr = vForm.setIf("expr", isExpr);
			if(isExpr) {
				vIfExpr.set("col-name", col.colAlias);
				vIfExpr.set("filter", f == null ? "" : f.expr);
				vIfExpr.set("checked", f == null ? "" : (f.includeBlanks ? "checked" : ""));
			}
			else {
				boolean isRange = col.dataType != DATA_TYPE.LOV;
				Values vIfRange = vIfExpr.setIf("range", isRange);
				if(isRange) {
					vIfRange.set("col-name", col.colAlias);
					vIfRange.set("start", f == null ? "" : f.start);
					vIfRange.set("end", f == null ? "" : f.end);
					vIfRange.set("checked", f == null ? "" : (f.includeBlanks ? "checked" : ""));
				}
				else {
					ValueList vlValues = vIfRange.getList("values");
					for(String value : col.values) {
						Values tmp = vlValues.next();
						tmp.set("value", value);
						tmp.set("checked", f == null ? "" : (f.hasValue(value)? "checked" : ""));
					}
				}
			}
		}

		// Pagination:
		boolean hasPrevious = page > 1;
		boolean hasNext = page < numPages;
		Values vIfPrev = v.setIf("has-previous", hasPrevious);
		if(hasPrevious) {
			vIfPrev.set("items-per-page", _itemsPerPage);
			vIfPrev.set("function", formAlias);
			vIfPrev.set("page", page-1);
		}
		Values vIfNoResults = v.setIf("no-results", results.getRowCount() == 0);
		int resultsWidth = _totalWidth - _RESULTS_WIDTH*2;
		if(results.getRowCount() > 0) {
			Values vIfOnePage = vIfNoResults.setIf("one-page", numPages == 1);
			vIfOnePage.set("items", settings.numRows);
			vIfOnePage.set("results-width", resultsWidth);
			if(numPages > 1) {
				vIfOnePage.set("page", page);
				vIfOnePage.set("num-pages", numPages);
			}
		}
		else {
			vIfNoResults.set("results-width", resultsWidth);
		}
		Values vIfNext = v.setIf("has-next", hasNext);
		if(hasNext) {
			vIfNext.set("items-per-page", _itemsPerPage);
			vIfNext.set("function", formAlias);
			vIfNext.set("page", page+1);
		}

		// Page selector:
		v.set("page", page);
		int numSections = numPages / 10;
		if(numPages % 10 > 0) {
			numSections++;
		}
		ValueList vlPageSelect = v.getList("page-select");
		int pageToSelect = 0;
		for(int i=0; i<numSections; i++) {
			Values vSection = vlPageSelect.next();
			ValueList vlSection = vSection.getList("page-select-pages");
			for(int j=0; j<10; j++) {
				boolean isCurrentPage = pageToSelect+1 == page; 
				boolean allowSelect = (pageToSelect < numPages) && !isCurrentPage;
				Values vIfAllowSelect = vlSection.next().setIf("allow-select", allowSelect);
				if(allowSelect) {
					vIfAllowSelect.set("page", ++pageToSelect);
					vIfAllowSelect.set("function", formAlias);
					vIfAllowSelect.set("tab", tab);
				}
				else {
					vIfAllowSelect.set("page", isCurrentPage ? ++pageToSelect : "");
				}
			}
		}

		// JavaScript objects:
		Values vIfJS = v.setIf("create-js-objects", _createJsObjects);
		if(vIfJS != null) {
			ValueList vlObjects = vIfJS.getList("js-objects");
			List<String> cols = results.getColumns();
			for(Row row : rows) {
				Values vObj = vlObjects.next();
				ValueList vlObjColumns = vObj.getList("js-columns");
				for(int i=0; i<cols.size(); i++) {
					Values vObjCol = vlObjColumns.next();
					vObjCol.set("column", cols.get(i));
					String value = row.getString(i+1);
					vObjCol.set("value", Strings.isEmpty(value) ? "" : Strings.addSlashes(value));
				}
			}
		}

		// Return the table template for display:
		return tplt;
	}

	private void _addBlanksSQL(ColFilter filter, StringBuilder whereSQL) {
		if(filter.includeBlanks) {
			whereSQL.append(" OR ");
			whereSQL.append(filter.field);
			whereSQL.append(" IS NULL) ");
		}
		else {
			whereSQL.append(" AND ");
			whereSQL.append(filter.field);
			whereSQL.append(" IS NOT NULL ");
		}
	}

	private void _setSnippet(String name, Template snippet) {
		if(_snippets == null) {
			_snippets = new HashMap<String, Template>();
		}
		_snippets.put(name, snippet);
	}

	private void _addColumn(String title, String columnId, int width, DATA_TYPE dt, Integer colIndex, String[] values, boolean trimChars) {
		Checker.checkEmpty(title, "title");
		Checker.checkEmpty(columnId, "columnId");
		Checker.checkMinValue(width, 10, "width");
		if(_maxWidth > 0) {
			if(_totalWidth + width > _maxWidth) {
				throw new IllegalArgumentException("column width ("+width+") exceeds maximum table width");
			}
		}
		Checker.checkNull(dt, "dt");
		if(values != null) {
			Checker.checkEmptyElements(values, "values");
			if(dt != DATA_TYPE.LOV) {
				throw new IllegalArgumentException("can only use DATA_TYPE.LOV when specifying list of values");
			}
		}
		_columns.add(new Column(title, columnId, width, dt, colIndex, values, trimChars));
		_totalWidth += width;
	}

	private String _getParams() {
		if(_params.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(Property p : _params) {
			sb.append("&");
			sb.append(p.getKey());
			sb.append("=");
			sb.append(p.getValue());
		}
		return sb.toString();
	}

	private class Column {

		public final String title;
		public final String colAlias;
		public final int width;
		public final DATA_TYPE dataType;
		public final Integer colIndex;
		public final String[] values;
		public final boolean trimChars;
		
		public Column(String title, String colAlias, int width, DATA_TYPE dt, Integer colIndex, String[] values, boolean trimChars) {
			this.title = title;
			this.colAlias = colAlias;
			this.width = width;
			this.dataType = dt;
			this.colIndex = colIndex;
			if(values == null) {
				this.values = null;
			}
			else {
				this.values = new String[values.length+1];
				for(int i=0; i<values.length; i++) {
					this.values[i] = values[i];
				}
				this.values[values.length] = _BLANKS;
			}
			this.trimChars = trimChars;
		}
		
		public String trimChars(String value, boolean bold) {
			if(Strings.isEmpty(value)) {
				return value;
			}
			if(trimChars) {
				int maxChars = (int)(width / (bold ? _PX_PER_CHAR_BOLD : _PX_PER_CHAR_NORMAL));
				// TODO remove
				System.out.println("PX_PER_CHAR_BOLD: "+_PX_PER_CHAR_BOLD+"; _PX_PER_CHAR_NORMAL: "+_PX_PER_CHAR_NORMAL);
				System.out.println("bold: "+bold+"; value: "+value+"; length: "+value.length()+"; maxChars: "+maxChars+"; width: "+width);
				if(value.length() > maxChars) {
					return value.substring(0, maxChars - 2) + "...";
				}
				else {
					return value;
				}
			}
			else {
				return value;
			}
		}
	}
	
	public interface DisplayCriteria {
		public boolean makeBold(Row row);
	}
}
