package com.netx.generics.R1.collections;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;
import com.netx.basic.R1.eh.Checker;


public class ICollection<T> extends AbstractCollection<T> {

	private final Collection<T> _collection;
	
	public ICollection(Collection<T> c) {
		Checker.checkNull(c, "c");
		_collection = c;
	}
	
	public int size() {
		return _collection.size();
	}

	public Iterator<T> iterator() {
		return new IIterator<T>(_collection.iterator());
	}
}
