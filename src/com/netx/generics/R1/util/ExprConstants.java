package com.netx.generics.R1.util;
import com.netx.generics.R1.translation.Token;


class ExprConstants {

	public static final String[] OPERATORS = new String[] {"^", "*", "/", "+", "-", "=", "<", ">", "<=", ">="};
	public static final String[] OP_COMPARISON = new String[] {"=", "<", ">", "<=", ">="};
	public static final String[] OP_SUM_SUBTRACT = new String[] {"+", "-"};
	public static final String[] OP_MULTIPLY_DIVIDE = new String[] {"*", "/"};
	// TODO i18n
	public static final String ERROR_START = "error in expression: ";
	
	private ExprConstants() {
	}
	
	public static boolean isComparisonOperator(Token t) {
		return Strings.find(t.toString(), ExprConstants.OP_COMPARISON) != -1;
	}

	public static boolean isSumSubtractOperator(Token t) {
		return Strings.find(t.toString(), ExprConstants.OP_SUM_SUBTRACT) != -1;
	}

	public static boolean isMultiplyDivideOperator(Token t) {
		return Strings.find(t.toString(), ExprConstants.OP_MULTIPLY_DIVIDE) != -1;
	}
}
