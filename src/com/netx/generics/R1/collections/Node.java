package com.netx.generics.R1.collections;
import com.netx.basic.R1.eh.Checker;


public class Node<E extends Object> {

	private Chain<E> _chain;
	private E _element;
	private Node<E> _previous;
	private Node<E> _next;
	
	// For Chain:
	Node(Chain<E> chain, E element, Node<E> previous, Node<E> next) {
		_chain = chain;
		_element = element;
		_previous = previous;
		_next = next;
		if(_previous != null) {
			_previous._next = this;
		}
		if(_next != null) {
			_next._previous = this;
		}
	}

	public Chain<E> getChain() {
		return _chain;
	}

	public E getElement() {
		return _element;
	}
	
	public void setElement(E element) {
		_checkStatus();
		Checker.checkNull(element, "element");
		_element = element;
	}
	
	public Node<E> next() {
		_checkStatus();
		return _next;
	}

	public Node<E> previous() {
		_checkStatus();
		return _previous;
	}

	public Node<E> addBefore(E element) {
		_checkStatus();
		boolean wasHead = _previous == null;
		Node<E> newNode = new Node<E>(_chain, element, _previous, this);
		if(wasHead) {
			_chain.setHead(newNode);
		}
		_chain.incrementSize();
		return newNode;
	}

	public Node<E> addAfter(E element) {
		_checkStatus();
		boolean wasTail = _next == null;
		Node<E> newNode = new Node<E>(_chain, element, this, _next);
		if(wasTail) {
			_chain.setTail(newNode);
		}
		_chain.incrementSize();
		return newNode;
	}

	public E remove() {
		_checkStatus();
		// This is the head node:
		if(_previous == null) {
			// When this is the last node:
			if(_next == null) {
				_chain.setHead(null);
			}
			else {
				_next._previous = null;
				_chain.setHead(_next);
			}
		}
		// This is the tail node:
		else if(_next == null) {
			_previous._next = null;
			_chain.setTail(_previous);
		}
		else {
			_previous._next = _next;
			_next._previous = _previous;
		}
		_chain.decrementSize();
		_chain = null;
		return _element;
	}
	
	private void _checkStatus() {
		if(_chain == null) {
			throw new IllegalStateException("this node has been removed from its chain");
		}
	}
}
