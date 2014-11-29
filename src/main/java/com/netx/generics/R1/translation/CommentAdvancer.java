package com.netx.generics.R1.translation;
import com.netx.basic.R1.eh.Checker;


public class CommentAdvancer {

	private final String _begin;
	private final String _end;
	private boolean _insideComment;
	private int _commentIndex;
	
	public CommentAdvancer(String begin, String end) {
		Checker.checkEmpty(begin, "begin");
		Checker.checkEmpty(end, "end");
		_begin = begin;
		_end = end;
		_insideComment = false;
		_commentIndex = -1;
	}

	public CommentAdvancer(String begin) {
		_begin = begin;
		_end = null;
		_insideComment = false;
		_commentIndex = -1;
	}
	
	// for GenericScanner:
	String checkLine(String line) {
		if(_insideComment) {
			int index = line.indexOf(_end);
			if(index == -1) {
				return null;
			}
			else {
				_insideComment = false;
				return line.substring(index+_end.length());
			}
		}
		else {
			int index = line.indexOf(_begin);
			if(index == -1) {
				return line;
			}
			else {
				String before = line.substring(0, index);
				if(!isMultiline()) {
					return before;
				}
				else {
					_commentIndex = index;
					int endIndex = line.indexOf(_end);
					if(endIndex == -1) {
						_insideComment = true;
						return before;
					}
					else {
						return before + line.substring(endIndex+_end.length());
					}
				}
			}
		}
	}

	// For GenericScanner:
	boolean isMultiline() {
		return _end != null;
	}

	// For GenericScanner:
	boolean isInsideComment() {
		return _insideComment;
	}

	// For GenericScanner:
	int getCommentIndex() {
		return _commentIndex;
	}
}
