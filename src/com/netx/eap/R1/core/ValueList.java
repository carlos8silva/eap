package com.netx.eap.R1.core;
import java.util.List;
import java.util.ArrayList;


public class ValueList {

	final Values structure;
	final Values last;
	private final List<Values> _list;
	
	ValueList() {
		structure = new Values();
		last = new Values();
		_list = new ArrayList<Values>();
	}

	ValueList(ValueList another) {
		structure = another.structure;
		last = another.last;
		_list = new ArrayList<Values>();
	}

	public Values next() {
		Values v = new Values(structure);
		_list.add(v);
		return v;
	}
	
	// For Template:
	List<Values> getValues() {
		// If our list has a "last" value, we need to ensure that the
		// "structure" field on the last Values object is replaced with "last":
		if(last.size() > 0 && _list.size() > 0) {
			Values lastValues = _list.remove(_list.size()-1);
			Values newLastValues = new Values(last);
			// Initialize "last"'s variables to the previous last:
			newLastValues.setValuesFrom(lastValues);
			_list.add(newLastValues);
		}
		return _list;
	}
}
