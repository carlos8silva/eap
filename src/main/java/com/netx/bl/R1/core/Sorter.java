package com.netx.bl.R1.core;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.Comparator;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.Field.TYPE;


class Sorter {

	private final static byte _EI = 1;
	private final static byte _ROW = 2;
	
	// TODO consider the way that the database sorts NULL values when sorting
	// Note that this is the reason why we include JdbcPropertyCache in the method's args
	public static void sort(List<? extends Object> list, OrderBy[] sortingOrder, JdbcPropertyCache driverCache) {
		// Find out the type of objects that the list contains:
		byte type = 0;
		if(list.isEmpty()) {
			return;
		}
		else {
			if(list.get(0) instanceof Row) {
				type = _ROW;
			}
			else {
				type = _EI;
			}
		}
		_sort(list, type, sortingOrder, 0, driverCache);
	}

	private static void _sort(List<? extends Object> list, byte type, OrderBy[] sortingOrder, int pos, JdbcPropertyCache driverCache) {
		Comparator<Object> comparator = null;
		if(type == _EI) {
			comparator = new EiComparator(sortingOrder[pos], driverCache);
		}
		else if(type == _ROW) {
			comparator = new RowComparator(sortingOrder[pos], driverCache);
		}
		else {
			throw new IntegrityException(type);
		}
		Collections.sort(list, comparator);
		if(pos < sortingOrder.length-1) {
			// Find places where there are elements which are equal:
			Iterator<? extends Object> it = list.iterator();
			Object previous = null;
			for(int i=0; it.hasNext(); i++) {
				Object current = it.next();
				if(previous != null) {
					// This section will only happen on the second cycle run:
					if(comparator.compare(previous, current) == 0) {
						int startIndex = i-1;
						i++;
						while(it.hasNext()) {
							current = it.next();
							if(comparator.compare(previous, current) != 0) {
								break;
							}
							i++;
						}
						_sort(list.subList(startIndex, i), type, sortingOrder, pos+1, driverCache);
					}
				}
				previous = current;
			}
		}
	}

	private static class EiComparator implements Comparator<Object> {

		private final Field _field;
		private final int _order;
		
		public EiComparator(OrderBy sortingOrder, JdbcPropertyCache driverCache) {
			_field = sortingOrder.field;
			if(sortingOrder.order.equals(OrderBy.ORDER.ASC)) {
				_order = 1;
			}
			else if(sortingOrder.order.equals(OrderBy.ORDER.DESC)) {
				_order = -1;
			}
			else {
				throw new IntegrityException();
			}
		}
		
		@SuppressWarnings({"unchecked", "rawtypes"})
		// Note: this comparison can ignore the fact that fields
		// may be case insensitive, since we are just sorting.
		public int compare(Object o1, Object o2) {
			EntityInstance<?,?> ei1 = (EntityInstance<?,?>)o1;
			EntityInstance<?,?> ei2 = (EntityInstance<?,?>)o2;
			Comparable c1 = ei1.getValue(_field);
			Comparable c2 = ei2.getValue(_field);
			// For Strings, we need to convert to lower case otherwise
			// Java String comparison is different depending on case:
			if(_field.getType() == TYPE.TEXT) {
				c1 = ((String)c1).toLowerCase();
				c2 = ((String)c2).toLowerCase();
			}
			return c1.compareTo(c2) * _order;
		}
	}

	private static class RowComparator implements Comparator<Object> {

		private final OrderBy _sortingOrder;
		private final int _order;
		
		public RowComparator(OrderBy sortingOrder, JdbcPropertyCache driverCache) {
			_sortingOrder = sortingOrder;
			if(sortingOrder.order.equals(OrderBy.ORDER.ASC)) {
				_order = 1;
			}
			else if(sortingOrder.order.equals(OrderBy.ORDER.DESC)) {
				_order = -1;
			}
			else {
				throw new IntegrityException();
			}
		}
		
		// Note: this comparison can ignore the fact that fields
		// may be case insensitive, since we are just sorting.
		public int compare(Object o1, Object o2) {
			Row r1 = (Row)o1;
			Row r2 = (Row)o2;
			try {
				Double d1 = r1.getDouble(_sortingOrder.column);
				Double d2 = r2.getDouble(_sortingOrder.column);
				return d1.compareTo(d2) * _order;
			}
			catch(NumberFormatException nfe) {
				// We need to convert to lower case otherwise Java String comparison is different depending on case:
				String s1 = r1.getString(_sortingOrder.column).toLowerCase();
				String s2 = r2.getString(_sortingOrder.column).toLowerCase();
				return s1.compareTo(s2) * _order;
			}
		}
	}
}
