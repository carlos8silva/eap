package com.netx.generics.R1.translation;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.netx.generics.R1.util.Document;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.io.ExtendedReader;


public abstract class GenericScanner extends FirstStep {

	protected final Recognizer[] _recognizers;
	protected final CommentAdvancer[] _advancers;
	private final boolean _skipWhitespace;
	
	public GenericScanner(Recognizer[] recognizers, boolean skipWhitespace, CommentAdvancer[] advancers) {
		super();
		Checker.checkEmpty(recognizers, "recognizers");
		Checker.checkNullElements(recognizers, "recognizers");
		_recognizers = recognizers;
		if(advancers == null) {
			_advancers = new CommentAdvancer[0];
		}
		else {
			Checker.checkNullElements(advancers, "advancers");
			_advancers = advancers;
		}
		_skipWhitespace = skipWhitespace;
	}

	public GenericScanner(Recognizer[] recognizers, boolean skipWhitespace) {
		this(recognizers, skipWhitespace, null);
	}

	public final Object performWork(Object o, ErrorList el) {
		Document text = null;
		try {
			text = new Document((ExtendedReader)o);
		}
		catch(IOException io) {
			el.addError(io.getMessage());
			return null;
		}
		// Perform any initial tests that subclasses may specify:
		int initialPosition = performInitialTests(text, el);
		if(initialPosition < 0) {
			return null;
		}
		Iterator<String> itLines = text.getLines().iterator();
		List<Token> tokens = new ArrayList<Token>();
		CommentAdvancer multilineComment = null;
		Position commentPosition = null;
		main:
		for(int lineNum=initialPosition; itLines.hasNext(); lineNum++) {
			String line = itLines.next();
			if(_skipWhitespace) {
				line = line.trim();
			}
			// Eliminate comment portions:
			advancers:
			while(true) {
				// Check whether we're passing a multiline comment:
				if(multilineComment != null) {
					line = multilineComment.checkLine(line);
					if(line == null) {
						// No end of multiline comment found; go to next line:
						continue main;
					}
					else {
						multilineComment = null;
						// Let the cycle go on, to check for additional
						// comments in this line.
					}
				}
				// Run comment advancers:
				for(CommentAdvancer advancer : _advancers) {
					String checkedLine = advancer.checkLine(line);
					if(checkedLine != line) {
						line = checkedLine;
						if(advancer.isInsideComment()) {
							multilineComment = advancer;
							commentPosition = new Position(lineNum+1, multilineComment.getCommentIndex());
						}
						continue advancers;
					}
				}
				// No more comments found, ok to recognize Tokens:
				break advancers;
			}
			// Comment passed, recognize Tokens:
			_recognizeTokens(el, line, lineNum, tokens);
		}
		if(multilineComment != null) {
			el.addError(commentPosition.getLine(), commentPosition.getIndex(), "unterminated multiline comment");
		}
		return tokens;
	}

	protected int performInitialTests(Document doc, ErrorList el) {
		return 0;
	}

	private void _recognizeTokens(ErrorList el, String line, int lineNum, List<Token> tokens) {
		int currPos = 0;
		run_recognizers:
		while(currPos<line.length()) {
			// Skip whitespace if required:
			if(_skipWhitespace) {
				while(currPos<line.length() && Character.isWhitespace(line.charAt(currPos))) {
					currPos++;
				}
			}
			if(currPos == line.length()) {
				return;
			}
			// Recognize tokens:
			int mark = currPos;
			for(Recognizer recognizer : _recognizers) {
				try {
					Token token = recognizer.recognize(line, lineNum, currPos, el);
					if(token != null) {
						tokens.add(token);
						currPos += token.getLength();
						break;
					}
				}
				catch(RecognizeException re) {
					el.addError(lineNum, re.errorPosition, re.getMessage());
					currPos = re.resumePosition;
					// Check whether we're not out of bounds:
					if(currPos >= line.length()) {
						continue run_recognizers;
					}
				}
			}
			// No token identified:
			if(mark == currPos) {
				el.addError(lineNum, currPos, "illegal token: '"+line.charAt(currPos)+"'");
				currPos++;
			}
		}
	}
}
