package com.netx.generics.R1.collections;
import com.netx.basic.R1.eh.Checker;


// TODO document in the API
// Or, just move to BL because it is only ever used there
public class Entry<K,V> {

	private final K _key;
	private V _value;
	
	public Entry(K key, V value) {
		Checker.checkNull(key, "key");
		_key = key;
		_value = value;
	}
	
	public K getKey() {
		return _key;
	}
	
	public V getValue() {
		return _value;
	}
	
	public Entry<?,?> setValue(V value) {
		_value = value;
		return this;
	}
}
