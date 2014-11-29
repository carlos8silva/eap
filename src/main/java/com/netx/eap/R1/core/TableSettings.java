package com.netx.eap.R1.core;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import com.netx.basic.R1.eh.IntegrityException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;


// TODO checks
public class TableSettings {

	private final Map<String,ColFilter> _filters;
	private  final List<ColSorting> _sorting;
	private int _itemsPerPage = 20;
	// For Table:
	public int numRows = 0;
	
	public TableSettings() {
		_filters = new HashMap<String,ColFilter>();
		_sorting = new ArrayList<ColSorting>();
	}
	
	public void put(ColFilter col) {
		_filters.remove(col.field);
		_filters.put(col.field, col);
	}

	public void put(ColSorting col) {
		_sorting.remove(col);
		_sorting.add(col);
	}

	public int getItemsPerPage() {
		return _itemsPerPage;
	}

	public void setItemsPerPage(int value) {
		_itemsPerPage = value;
	}

	public Document toXML() {
		final Document document = DocumentHelper.createDocument();
		Element root = document.addElement("table-settings");
		root.addElement("items-per-page").addText(getItemsPerPage()+"");
		// Filters:
		Element filters = root.addElement("filters");
		for(ColFilter filter : getFilters()) {
			Element eFilter = filters.addElement("filter");
			eFilter.addAttribute("field", filter.field);
			eFilter.addAttribute("expr", filter.expr);
			eFilter.addAttribute("start", filter.start);
			eFilter.addAttribute("end", filter.end);
			if(filter.values != null) {
				StringBuilder sb = new StringBuilder();
				for(int i=0; i<filter.values.length; i++) {
					sb.append(filter.values[i]);
					if(i < filter.values.length-1) {
						sb.append("¦");
					}
				}
				eFilter.addAttribute("values", sb.toString());
			}
			eFilter.addAttribute("include-blanks", filter.includeBlanks+"");
		}
		// Sorting:
		Element sorting = root.addElement("sorting");
		for(ColSorting cs : getSorting()) {
			Element eSorting = sorting.addElement("sorting");
			eSorting.addAttribute("field", cs.field);
			eSorting.addAttribute("order", cs.order);
		}
		return document;
	}

	@SuppressWarnings("unchecked")
	public void apply(Document xml) {
		Element root = xml.getRootElement();
		Iterator<Element> it = root.elementIterator();
		Element itemsPerPage = it.next();
		setItemsPerPage(Integer.valueOf(itemsPerPage.getText()));
		Element filters = (Element)it.next();
		Iterator<Element> itFields = filters.elementIterator();
		while(itFields.hasNext()) {
			Element eField = itFields.next();
			String field = eField.attributeValue("field");
			String expr = eField.attributeValue("expr");
			String start = eField.attributeValue("start");
			String end = eField.attributeValue("end");
			String values = eField.attributeValue("values");
			Boolean includeBlanks = new Boolean(eField.attributeValue("include-blanks"));
			ColFilter filter = null;
			if(expr != null) {
				filter = new ColFilter(field, expr, includeBlanks);
			}
			else if(start != null) {
				filter = new ColFilter(field, start, end, includeBlanks);
			}
			else if(values != null) {
				String[] broken = values.split("[¦]");
				filter = new ColFilter(field, broken);
			}
			else {
				throw new IntegrityException();
			}
			put(filter);
		}
		Element sorting = (Element)it.next();
		Iterator<Element> itSorting = sorting.elementIterator();
		while(itSorting.hasNext()) {
			Element eSorting = itSorting.next();
			String field = eSorting.attributeValue("field");
			String order = eSorting.attributeValue("order");
			put(new ColSorting(field, order));
		}
	}

	// For Table:
	ColFilter getFilter(String col) {
		return _filters.get(col);
	}

	// For Table:
	ColSorting getSorting(String col) {
		for(ColSorting cs : _sorting) {
			if(cs.field.equals(col)) {
				return cs;
			}
		}
		return null;
	}

	// For Table:
	Collection<ColFilter> getFilters() {
		return _filters.values();
	}

	// For Table:
	Collection<ColSorting> getSorting() {
		return _sorting;
	}

	// For Table:
	int getSortingOrder(String col) {
		Iterator<ColSorting> it = _sorting.iterator();
		for(int i=1; it.hasNext(); i++) {
			ColSorting cs = it.next();
			if(cs.field.equals(col)) {
				return i;
			}
		}
		throw new IntegrityException();
	}

	// For Table:
	void removeFilter(String col) {
		_filters.remove(col);
	}

	// For Table:
	void removeSorting(String col) {
		_sorting.remove(new ColSorting(col, null));
	}
	
	// For Table:
	void apply(TableSettings settings) {
		Collection<ColFilter> filters = settings.getFilters();
		for(ColFilter filter : filters) {
			put(filter);
		}
		Collection<ColSorting> order = settings.getSorting();
		for(ColSorting sorting : order) {
			put(sorting);
		}
	}
}
