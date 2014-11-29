package com.netx.generics.R1.util;
import java.util.ArrayList;
import java.util.List;
import com.netx.generics.R1.collections.IList;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.io.ExtendedReader;
import com.netx.basic.R1.io.ReadWriteException;
import com.netx.basic.R1.io.FileLockedException;


public class Document {

	private final List<String> _lines;

	public Document(ExtendedReader in) throws ReadWriteException, FileLockedException {
		Checker.checkNull(in, "in");
		_lines = new ArrayList<String>();
		String line = in.readLine();
		while(line != null) {
			_lines.add(line);
			line = in.readLine();
		}
		in.close();
	}
	
	public int countLines() {
		return _lines.size();
	}

	public String getLine(int index) {
		Checker.checkIndex(index, "index");
		return (String)_lines.get(index);
	}
	
	public IList<String> getLines() {
		return new IList<String>(_lines);
	}
}
