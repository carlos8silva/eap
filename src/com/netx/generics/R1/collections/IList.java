package com.netx.generics.R1.collections;
import java.util.List;
import java.util.AbstractList;
import com.netx.basic.R1.eh.Checker;


public class IList<T> extends AbstractList<T> {

	private final List<T> _list;
	private final T[] _array;
	
	public IList(List<T> list) {
		Checker.checkNull(list, "list");
		_list = list;
		_array = null;
	}
	
	public IList(T[] array) {
		Checker.checkNull(array, "array");
		_list = null;
		_array = array;
	}

	public T get(int index) {
		return _list == null ? _array[index] : _list.get(index);
	}

	public int size() {
		return _list == null ? _array.length : _list.size();
	}
}
