package com.netx.generics.R1.collections;
import java.util.HashMap;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import com.netx.basic.R1.eh.Checker;


// TODO document in the API
public class Collections {

	// TODO Should we move this to BL? (it is only ever used there)
	public static <K,V> Map<K,V> toMap(Collection<Entry<K,V>> c) {
		Checker.checkNull(c, "c");
		Map<K,V> map = new HashMap<K,V>();
		for(Entry<K,V> e : c) {
			map.put(e.getKey(), e.getValue());
		}
		return map;
	}

	// TODO Should we move this to BL? (it is only ever used there)
	public static <K,V> Map<K,V> toMap(Entry<K,V>[] array) {
		Checker.checkNullElements(array, "array");
		Map<K,V> map = new HashMap<K,V>();
		for(Entry<K,V> e : array) {
			map.put(e.getKey(), e.getValue());
		}
		return map;
	}

	public static Object find(List<?> list, Object o) {
		Checker.checkNull(list, "list");
		Checker.checkNull(o, "o");
		for(Object elem : list) {
			if(o.equals(elem)) {
				return elem;
			}
		}
		return null;
	}
}
