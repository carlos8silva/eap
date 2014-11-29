package com.netx.generics.R1.collections;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import com.netx.basic.R1.eh.Checker;


public class IMap<K,V> extends AbstractMap<K,V> {

	private final Map<K,V> _map;
	
	public IMap(Map<K,V> map) {
		Checker.checkNull(map, "map");
		_map = map;
	}

	public Set<Map.Entry<K,V>> entrySet() {
		return new ISet<Map.Entry<K,V>>(_map.entrySet());
	}

}
