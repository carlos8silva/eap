package com.netx.generics.R1.collections;
import java.lang.Iterable;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class Chain<E extends Object> implements Iterable<E> {

	private int _size;
	private Node<E> _head;
	private Node<E> _tail;
	
	public Chain() {
		_size = 0;
		_head = _tail = null;
	}

	public int size() {
		return _size;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public Node<E> add(E element) {
		if(_head == null) {
			_head = new Node<E>(this, element, null, null);
			_tail = _head;
		}
		else {
			_head = new Node<E>(this, element, null, _head);
		}
		_size++;
		return _head;
	}

	public Node<E> getHead() {
		return _head;
	}

	public Node<E> getTail() {
		return _tail;
	}

	public Iterator<E> iterator() {
		return new ChainIterator(_head);
	}

	public void clear() {
		Iterator<E> it = iterator();
		while(it.hasNext()) {
			it.next();
			it.remove();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		Iterator<E> it = iterator();
		while(it.hasNext()) {
			sb.append(it.next().toString());
			if(it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	// For Node:
	void setHead(Node<E> newHead) {
		_head = newHead;
	}

	// For Node:
	void setTail(Node<E> newTail) {
		_tail = newTail;
	}

	// For Node:
	void incrementSize() {
		_size++;
	}

	// For Node:
	void decrementSize() {
		_size--;
	}

	private class ChainIterator implements Iterator<E> {

		private Node<E> _elem;
		private Node<E> _returned;
		
		public ChainIterator(Node<E> head) {
			_elem = head;
			_returned = null;
		}
		
		public boolean hasNext() {
			return _elem != null;
		}
		
		public E next() {
			if(_elem == null) {
				throw new NoSuchElementException();
			}
			E next = _elem.getElement();
			_returned = _elem;
			_elem = _elem.next();
			return next;
		}
		
		public void remove() {
			if(_returned == null) {
				throw new IllegalStateException("next() has not been called yet, or remove has already been called");
			}
			_returned.remove();
		}
	}
}
