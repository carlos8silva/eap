package com.netx.generics.R1.collections;
import java.util.Iterator;

import com.netx.basic.R1.eh.Checker;


public class IIterator<T> implements Iterator<T> {

	private final Iterator<T> _it;

	public IIterator(Iterator<T> it) {
		Checker.checkNull(it, "it");
		this._it = it;
	}

	public boolean hasNext() {
		return _it.hasNext();
	}

	public T next() {
		return _it.next();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
