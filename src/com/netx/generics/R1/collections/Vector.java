package com.netx.generics.R1.collections;


public class Vector<T> {

	private T[] _array;
	
	public Vector() {
		_array = null;
	}

	@SuppressWarnings("unchecked")
	public void append(T elem) {
		if(_array == null) {
			_array = (T[])new Object[1];
		}
		else {
			T[] temp = (T[])new Object[_array.length + 1];
			System.arraycopy(_array, 0, temp, 0, _array.length);
			_array = temp;
		}
		_array[_array.length-1] = elem;
	}

	public T get(int pos) {
		return _array[pos];
	}

	public int size() {
		return _array == null ? 0 : _array.length;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
}
