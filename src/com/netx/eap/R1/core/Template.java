package com.netx.eap.R1.core;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.ListIterator;

import com.netx.generics.R1.translation.ParseException;
import com.netx.generics.R1.util.Document;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.io.File;
import com.netx.basic.R1.io.ExtendedReader;
import com.netx.basic.R1.io.ExtendedWriter;
import com.netx.basic.R1.io.Streams;
import com.netx.basic.R1.io.FileNotFoundException;
import com.netx.basic.R1.io.FileLockedException;
import com.netx.basic.R1.io.ReadWriteException;


// TODO move to generics.util?
// TODO force error when a list is not closed
// TODO use Checker for public methods
public class Template {

	// TYPE:
	private static enum CONTAINER {DOC, LIST, IF, ELSE};
	private static final String _LIST = "#list";
	private static final String _LAST = "#last";
	private static final String _ENDLIST = "#endlist";
	private static final String _IF = "#if";
	private static final String _ELSE = "#else";
	private static final String _ENDIF = "#endif";
	private static final String _VAR = "${";
	private static final String _SNIPPET = "#snippet";
	
	// INSTANCE:
	private final Values _values;
	private final boolean _ignoreWhitespace;
	private Map<String, Template> _snippets;
	
	public Template(File f, Map<String, Template> snippets, boolean ignoreWhitespace) throws FileNotFoundException, FileLockedException, ReadWriteException {
		Checker.checkNull(f, "f");
		_values = new Values();
		_ignoreWhitespace = ignoreWhitespace;
		_snippets = snippets;
		Document doc = new Document(new ExtendedReader(f.getInputStream()));
		_parse(doc.getLines().listIterator(), _values, null, CONTAINER.DOC, new Position());
	}

	public Template(File f) throws FileNotFoundException, FileLockedException, ReadWriteException {
		this(f, null, false);
	}

	// Copy-constructor:
	public Template(Template another) {
		_values = new Values(another._values);
		_ignoreWhitespace = another._ignoreWhitespace;
		_snippets = another._snippets == null ? null : new HashMap<String,Template>();
		if(_snippets != null) {
			for(Map.Entry<String, Template> entry : another._snippets.entrySet()) {
				_snippets.put(entry.getKey(), new Template(entry.getValue()));
			}
		}
	}

	public Values getValues() {
		return _values;
	}
	
	public void render(PrintWriter out) throws ReadWriteException, FileLockedException {
		Values v = getValues();
		_traverse(v, out);
	}

	public void render(String contentType, EapResponse response) throws ReadWriteException, FileLockedException {
		Checker.checkNull(response, "response");
		response.setContentType(contentType);
		PrintWriter out = response.getWriter();
		render(out);
		out.close();
	}

	public void render(EapResponse response) throws ReadWriteException, FileLockedException {
		render(MimeTypes.TEXT_HTML, response);
	}
	
	private void _traverse(Values v, PrintWriter out) throws ReadWriteException, FileLockedException {
		for(Segment s : v.segments) {
			if(s.isText() || s.isVariable()) {
				if(s.hasStream()) {
					Streams.copy(s.getStream(), new ExtendedWriter(new HtmlWriter(out), EapResponse.STREAM_NAME));
				}
				else {
					out.print(s.getText());
				}
			}
			else if(s.isList()) {
				ValueList vl = s.getList();
				for(Values listValues : vl.getValues()) {
					_traverse(listValues, out);
				}
			}
			else if(s.isIf()) {
				Values ifValues = s.getIf();
				if(ifValues != null) {
					_traverse(ifValues, out);
				}
			}
			else {
				throw new IntegrityException(s);
			}
			out.flush();
		}
	}
	
	private void _parse(ListIterator<String> it, Values v, Values last, CONTAINER c, Position p) {
		while(it.hasNext()) {
			String line = it.next();
			if(_ignoreWhitespace) {
				line = line.trim();
			}
			p.lineNum++;
			try {
				// TODO
				// We cant do this because HTML segments will be picked up as illegal: colors,
				// flash versions, etc. Need to find another way to detect when people use "#end-list"
				/*
				// Find illegal directives:
				int index = line.indexOf('#');
				if(index != -1) {
					StringBuilder sb = new StringBuilder();
					sb.append('#');
					index++;
					while(index < line.length()) {
						if(Character.isWhitespace(line.charAt(index)) || line.charAt(index) == '(') {
							break;
						}
						sb.append(line.charAt(index));
						index++;
					}
					String directive = sb.toString();
					if(!directive.equals(_LIST) && !directive.equals(_ENDLIST) && !directive.equals(_IF) && !directive.equals(_ELSE) && !directive.equals(_ENDIF)) {
						throw new ParseException(p.lineNum, "illegal directive: "+directive);
					}
				}
				*/
				// Snippet:
				int index = line.indexOf(_SNIPPET);
				if(index != -1) {
					// Extract list name:
					line = line.substring(index+_SNIPPET.length());
					String snippetName = _extractName(line, p);
					// Note: if we dont find the snippet, we just skip it:
					if(_snippets != null) {
						Template snippet = _snippets.get(snippetName);
						if(snippet != null) {
							for(Segment s : snippet._values.segments) {
								v.segments.add(s);
							}
						}
					}
					continue;
				}
				// List:
				index = line.indexOf(_LIST);
				if(index != -1) {
					// Extract list name:
					line = line.substring(index+_LIST.length());
					String listName = _extractName(line, p);
					// Parse list contents:
					ValueList vl = new ValueList();
					_parse(it, vl.structure, vl.last, CONTAINER.LIST, p);
					v.addList(listName, new Segment(listName, null, vl, null, null, p.lineNum));
					continue;
				}
				// Endlist:
				index = line.indexOf(_ENDLIST);
				if(index != -1) {
					if(c != CONTAINER.LIST) {
						throw new ParseException(p.lineNum, "found "+_ENDLIST+" while not inside a list");
					}
					return;
				}
				// Last:
				index = line.indexOf(_LAST);
				if(index != -1) {
					if(c != CONTAINER.LIST) {
						throw new ParseException(p.lineNum, "found "+_LAST+" while not inside a list");
					}
					// Force the code section below where we parse the line(s) to
					// add the segments to the "last" object rather than "structure"
					v = last;
					continue;
				}
				// If:
				index = line.indexOf(_IF);
				if(index != -1) {
					// Extract if name:
					line = line.substring(index+_IF.length());
					String ifName = _extractName(line, p);
					// Parse if contents:
					Values ifTrue = new Values();
					Values ifFalse = null;
					_parse(it, ifTrue, null, CONTAINER.IF, p);
					// Check if we have an 'else':
					index = it.previous().indexOf(_ELSE);
					it.next();
					if(index != -1) {
						ifFalse = new Values();
						_parse(it, ifFalse, null, CONTAINER.IF, p);
					}
					v.addIf(ifName, new Segment(ifName, null, null, ifTrue, ifFalse, p.lineNum));
					continue;
				}
				// Else:
				index = line.indexOf(_ELSE);
				if(index != -1) {
					if(c != CONTAINER.IF) {
						throw new ParseException(p.lineNum, "found "+_ELSE+" while not inside an 'if'");
					}
					return;
				}
				// Endif:
				index = line.indexOf(_ENDIF);
				if(index != -1) {
					if(c != CONTAINER.IF) {
						throw new ParseException(p.lineNum, "found "+_ENDIF+" while not inside an 'if'");
					}
					return;
				}
				// Variable(s):
				while(true) {
					index = line.indexOf(_VAR);
					if(index == -1) {
						break;
					}
					// Add text segment:
					String text = line.substring(0, index);
					v.segments.add(new Segment(null, text, null, null, null, p.lineNum));
					// Add variable segment:
					line = line.substring(index+_VAR.length());
					index = line.indexOf('}');
					if(index == -1) {
						throw new ParseException(p.lineNum, "expected '}'");
					}
					String varName = line.substring(0, index);
					line = line.substring(index+1);
					// For variables, we need to check whether they have already been read so that
					// we reuse the same object and allow the same variable to be used multiple times:
					Segment variable = v.variables.get(varName);
					if(variable == null) {
						variable = new Segment(varName, null, null, null, null, p.lineNum);
						v.variables.put(varName, variable);
					}
					v.segments.add(variable);
				}
				// The remaining line is a text segment:
				v.segments.add(new Segment(null, line+"\n", null, null, null, p.lineNum));
			}
			catch(StringIndexOutOfBoundsException siobe) {
				throw new ParseException(p.lineNum, "unexpected end of line");
			}
		}
	}
	
	private String _extractName(String s, Position p) {
		if(s.charAt(0) != '(') {
			throw new ParseException(p.lineNum, "expected '('");
		}
		int index = s.indexOf(")");
		if(index == -1) {
			throw new ParseException(p.lineNum, "expected ')'");
		}
		return s.substring(1, index);
	}

	private class Position {
		public int lineNum = 0;
	}
}
