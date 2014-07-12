package com.netx.generics.R1.util;
import com.netx.generics.R1.translation.ErrorList;


// This is the superclass to all types of expressions.
// Expr subclasses implement the evaluate method according to the type
// of expression. Please note that expressions are parsed in inverted
// order of operator precedence.
abstract class Expression {

	protected Expression() {
	}
	
	public abstract Object evaluate(ErrorList el);
}
