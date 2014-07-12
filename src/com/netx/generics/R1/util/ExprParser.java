package com.netx.generics.R1.util;
import java.util.List;
import java.util.ArrayList;

import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.TYPE;
import com.netx.generics.R1.translation.Token;
import com.netx.generics.R1.translation.TranslationStep;


// TODO i18n
class ExprParser extends TranslationStep {

	public ExprParser(ExprScanner scanner) {
		super(scanner);
	}

	@SuppressWarnings("unchecked")
	public Object performWork(Object o, ErrorList el) {
		List<? extends Token> tokens = (List<? extends Token>)o;
		List<Object> results = new ArrayList<Object>();
		try {
			for(int i=0; i<tokens.size(); i++) {
				Token t = tokens.get(i);
				if(t.getType() == EXPR_TYPE.TEXT) {
					results.add(t);
				}
				else if(t.getType() == TYPE.SEPARATOR) {
					try {
						int start = ++i;
						// Search for enclosing ']':
						while(true) {
							t = tokens.get(i);
							if(t.toString().equals("]")) {
								break;
							}
							else {
								i++;
							}
						}
						// Ok, this is the built-in expression part that needs to be parsed:
						Expression expr = _parseExpr(tokens.subList(start, i), el);
						if(expr != null) {
							results.add(expr);
						}
					}
					catch(IndexOutOfBoundsException ioobe) {
						el.addError(tokens.get(tokens.size()-1 ), "could not find enclosing ']'");
					}
				}
			}
			return results;
		}
		catch(IndexOutOfBoundsException ioobe) {
			el.addError(tokens.get(tokens.size()-1), "unexpected end of expression");
			return null;
		}
	}

	private Expression _parseExpr(List<? extends Token> tokens, ErrorList el) {
		// One value expression:
		if(tokens.size() == 1) {
			return _getValue(tokens.get(0), el);
		}
		// Expression in parenthesis:
		if(tokens.get(0).toString().equals("(") && tokens.get(tokens.size()-1).toString().equals(")")) {
			return _parseExpr(tokens.subList(1, tokens.size()-1), el);
		}
		// Parse error when there are missing or extra separators:
		if(tokens.size() == 2) {
			Token error = null;
			if(tokens.get(0).getType() == TYPE.SEPARATOR || tokens.get(0).getType() == TYPE.OPERATOR) {
				error = tokens.get(0);
			}
			else {
				error = tokens.get(1);
			}
			el.addError(error, "unexpected token '"+error.toString()+"'");
			return null;
		}
		int current = 0;
		// Start with breaking the expr by comparison operators:
		while(current < tokens.size()) {
			Token t = tokens.get(current);
			if(t.getType() == TYPE.SEPARATOR && t.toString().equals("(")) {
				List<? extends Token> sub = _advanceParenthesis(tokens, current+1, el);
				if(sub == null) {
					return null;
				}
				current += sub.size()+1;
			}
			else if(ExprConstants.isComparisonOperator(t)) {
				ExprOperation expr = new ExprOperation(t);
				expr.left  = _parseExpr(tokens.subList(0, current), el);
				expr.right = _parseExpr(tokens.subList(current+1, tokens.size()), el);
				return expr;
			}
			else {
				current++;
			}
		}
		// Try to break the expr by sum / subtract operators:
		current = 0;
		while(current < tokens.size()) {
			Token t = tokens.get(current);
			if(t.getType() == TYPE.SEPARATOR && t.toString().equals("(")) {
				List<? extends Token> sub = _advanceParenthesis(tokens, current+1, el);
				if(sub == null) {
					return null;
				}
				current += sub.size()+1;
			}
			else if(ExprConstants.isSumSubtractOperator(t)) {
				ExprOperation expr = new ExprOperation(t);
				expr.left  = _parseExpr(tokens.subList(0, current), el);
				expr.right = _parseExpr(tokens.subList(current+1, tokens.size()), el);
				return expr;
			}
			else {
				current++;
			}
		}
		// Try to break the expr by multiply / divide operators:
		current = 0;
		while(current < tokens.size()) {
			Token t = tokens.get(current);
			if(t.getType() == TYPE.SEPARATOR && t.toString().equals("(")) {
				List<? extends Token> sub = _advanceParenthesis(tokens, current+1, el);
				if(sub == null) {
					return null;
				}
				current += sub.size()+1;
			}
			else if(ExprConstants.isMultiplyDivideOperator(t)) {
				ExprOperation expr = new ExprOperation(t);
				expr.left  = _parseExpr(tokens.subList(0, current), el);
				expr.right = _parseExpr(tokens.subList(current+1, tokens.size()), el);
				return expr;
			}
			else {
				current++;
			}
		}
		// Try to break the expr by the power operator:
		current = 0;
		while(current < tokens.size()) {
			Token t = tokens.get(current);
			if(t.getType() == TYPE.SEPARATOR && t.toString().equals("(")) {
				List<? extends Token> sub = _advanceParenthesis(tokens, current+1, el);
				if(sub == null) {
					return null;
				}
				current += sub.size()+1;
			}
			else if(t.getType() == TYPE.OPERATOR && t.toString().equals("^")) {
				ExprOperation expr = new ExprOperation(t);
				expr.left  = _parseExpr(tokens.subList(0, current), el);
				expr.right = _parseExpr(tokens.subList(current+1, tokens.size()), el);
				return expr;
			}
			else {
				current++;
			}
		}
		// If we got here, this MUST be a function call:
		return _parseFunctionCall(tokens, el);
	}

	private ExprFunctionCall _parseFunctionCall(List<? extends Token> tokens, ErrorList el) {
		int current = 0;
		Token tCall = tokens.get(current++);
		if(tCall.getType() != TYPE.IDENTIFIER) {
			el.addError(tCall, "unexpected '"+tCall+"'");
			return null;
		}
		ExprFunctionCall expr = new ExprFunctionCall(tCall);
		Token t = tokens.get(current);
		if(!t.toString().equals("(")) {
			el.addError(t, "expected '(', found '"+t.toString()+"'");
			return null;
		}
		// By calling _advanceParenthesis, we guarantee that the function
		// arguments have a good format (no conclusion on commas though):
		List<? extends Token> sub = _advanceParenthesis(tokens, current+1, el);
		if(sub == null) {
			return null;
		}
		List<Expression> args = _getFunctionArguments(sub.subList(0, sub.size()-1), expr.args, el);
		current += sub.size()+1;
		if(args == null) {
			return null;
		}
		else {
			// We shouldn't have any more tokens:
			if(current < tokens.size()) {
				t = tokens.get(current);
				el.addError(t, "unexpected token '"+t.toString()+"'");
			}
			return expr;
		}
	}

	private List<Expression> _getFunctionArguments(List<? extends Token> tokens, List<Expression> args, ErrorList el) {
		// If the arguments are empty, we can simply exit:
		if(tokens.size() == 0) {
			return args;
		}
		int start = 0;
		for(int i=0; i < tokens.size(); i++) {
			Token t = tokens.get(i);
			if(t.toString().equals(",")) {
				Expression expr = _parseExpr(tokens.subList(start, i), el);
				if(expr == null) {
					return null;
				}
				args.add(expr);
				start = i+1;
			}
			else if(t.toString().equals("(")) {
				// Advance parenthesis:
				List<? extends Token> sub = _advanceParenthesis(tokens, i+1, el);
				if(sub == null) {
					return null;
				}
				i += sub.size();
			}
		}
		// Parse the last arg:
		Expression expr = _parseExpr(tokens.subList(start, tokens.size()), el);
		if(expr == null) {
			return null;
		}
		args.add(expr);
		return args;
	}
	
	private List<? extends Token> _advanceParenthesis(List<? extends Token> tokens, int start, ErrorList el) {
		int parenthesis = 1;
		int i = start;
		for( ; parenthesis > 0; i++) {
			if(i >= tokens.size()) {
				el.addError(tokens.get(i-1), "missing enclosing ')'");
			}
			Token t = tokens.get(i);
			if(t.getType() == TYPE.SEPARATOR) {
				if(t.toString().equals("(")) {
					parenthesis++;
				}
				else if(t.toString().equals(")")) {
					parenthesis--;
				}
			}
		}
		return tokens.subList(start, i);
	}

	private ExprValue _getValue(Token t, ErrorList el) {
		if(t.getType() == TYPE.INTEGER_CONSTANT || t.getType() == TYPE.FLOAT_CONSTANT || t.getType() == TYPE.STRING_CONSTANT) {
			return new ExprValue(t);
		}
		else {
			el.addError(t, "unexpected token '"+t.toString()+"'");
			return null;
		}
	}
}
