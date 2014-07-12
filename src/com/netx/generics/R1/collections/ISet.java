package com.netx.generics.R1.collections;
import java.util.AbstractSet;
import java.util.Set;
import java.util.Iterator;
import com.netx.basic.R1.eh.Checker;


public class ISet<T> extends AbstractSet<T> {

	private final Set<T> _set;

	public ISet(Set<T> c) {
		Checker.checkNull(c, "c");
		_set = c;
	}

	public Iterator<T> iterator() {
		return new IIterator<T>(_set.iterator());
	}
	
	public int size() {
		return _set.size();
	}
}
