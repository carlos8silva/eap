package com.netx.bl.R1.core;
import java.util.List;
import java.util.ArrayList;
import com.netx.generics.R1.collections.ICollection;


public class UniqueConstraint {

	private final List<Field> _fields;
	
	UniqueConstraint(Field ... fields) {
		_fields = new ArrayList<Field>(fields.length);
		for(Field f : fields) {
			_fields.add(f);
		}
	}
	
	public ICollection<Field> getFields() {
		return new ICollection<Field>(_fields);
	}
}
