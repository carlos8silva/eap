package com.netx.generics.R1.util;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.shared.Constants;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.Token;


class ExprOperation extends Expression {

	public final Token operator;
	public Expression left;
	public Expression right;
	
	public ExprOperation(Token op) {
		operator = op;
	}

	public Object evaluate(ErrorList el) {
		Object oLeft = left.evaluate(el);
		Object oRight = right.evaluate(el);
		if(oLeft == null || oRight == null) {
			if(el.hasErrors()) {
				return null;
			}
			// We shouldn't have received a null object if
			// there are no errors in the sub-expressions:
			if(oLeft == null) {
				el.addError(operator, "left expression returned a null value");
				return null;
			}
			if(oRight == null) {
				el.addError(operator, "right expression returned a null value");
				return null;
			}
		}
		// Comparisons:
		if(ExprConstants.isComparisonOperator(operator)) {
			if(oLeft.getClass() == Boolean.class) {
				el.addError(operator, "left expression has wrong operand type for comparison operation "+operator.toString());
				return null;
			}
			if(oRight.getClass() == Boolean.class) {
				el.addError(operator, "left expression has wrong operand type for comparison operation "+operator.toString());
				return null;
			}
			if(oLeft.getClass() == String.class || oRight.getClass() == String.class) {
				if(operator.toString().equals("=")) {
					return oLeft.toString().equals(oRight.toString());
				}
				if(operator.toString().equals("<")) {
					return oLeft.toString().compareTo(oRight.toString()) < 0;
				}
				if(operator.toString().equals(">")) {
					return oLeft.toString().compareTo(oRight.toString()) > 0;
				}
				if(operator.toString().equals("<=")) {
					return oLeft.toString().compareTo(oRight.toString()) <= 0;
				}
				if(operator.toString().equals(">=")) {
					return oLeft.toString().compareTo(oRight.toString()) >= 0;
				}
				throw new IntegrityException(operator.toString());
			}
			else {
				double dLeft = ((Number)oLeft).doubleValue();
				double dRight = ((Number)oRight).doubleValue();
				if(operator.toString().equals("=")) {
					return dLeft == dRight;
				}
				if(operator.toString().equals("<")) {
					return dLeft < dRight;
				}
				if(operator.toString().equals(">")) {
					return dLeft > dRight;
				}
				if(operator.toString().equals("<=")) {
					return dLeft <= dRight;
				}
				if(operator.toString().equals(">=")) {
					return dLeft >= dRight;
				}
				throw new IntegrityException(operator.toString());
			}
		}
		// Arithmetic:
		if(operator.toString().equals("+")) {
			if(oLeft.getClass() == String.class || oRight.getClass() == String.class) {
				return oLeft.toString() + oRight.toString();
			}
			if(oLeft.getClass() == Double.class || oRight.getClass() == Double.class) {
				double dLeft = ((Number)oLeft).doubleValue();
				double dRight = ((Number)oRight).doubleValue();
				return dLeft + dRight;
			}
			else {
				int iLeft = ((Integer)oLeft).intValue();
				int iRight = ((Integer)oRight).intValue();
				return iLeft + iRight;
			}
		}
		if(operator.toString().equals("-")) {
			if(oLeft.getClass() == String.class || oRight.getClass() == String.class) {
				return Strings.replaceAll(oLeft.toString(), oRight.toString(), Constants.EMPTY);
			}
			if(oLeft.getClass() == Double.class || oRight.getClass() == Double.class) {
				double dLeft = ((Number)oLeft).doubleValue();
				double dRight = ((Number)oRight).doubleValue();
				return dLeft - dRight;
			}
			else {
				int iLeft = ((Integer)oLeft).intValue();
				int iRight = ((Integer)oRight).intValue();
				return iLeft - iRight;
			}
		}
		if(operator.toString().equals("*")) {
			if(oLeft.getClass() == String.class || oRight.getClass() == String.class) {
				el.addError(operator, "cannot multiply Strings");
				return null;
			}
			if(oLeft.getClass() == Double.class || oRight.getClass() == Double.class) {
				double dLeft = ((Number)oLeft).doubleValue();
				double dRight = ((Number)oRight).doubleValue();
				return dLeft * dRight;
			}
			else {
				int iLeft = ((Integer)oLeft).intValue();
				int iRight = ((Integer)oRight).intValue();
				return iLeft * iRight;
			}
		}
		if(operator.toString().equals("/")) {
			if(oLeft.getClass() == String.class || oRight.getClass() == String.class) {
				el.addError(operator, "cannot divide Strings");
				return null;
			}
			double dLeft = ((Number)oLeft).doubleValue();
			double dRight = ((Number)oRight).doubleValue();
			return dLeft / dRight;
		}
		// Power:
		if(operator.toString().equals("^")) {
			if(oLeft.getClass() == String.class) {
				if(oRight.getClass() != Integer.class) {
					el.addError(operator, "expected integer power for String argument");
					return null;
				}
				return Strings.repeat((String)oLeft, (Integer)oRight);
			}
			if(oLeft.getClass() == Double.class || oRight.getClass() == Double.class) {
				double dLeft = ((Number)oLeft).doubleValue();
				double dRight = ((Number)oRight).doubleValue();
				return Math.pow(dLeft, dRight);
			}
			else {
				int iLeft = ((Integer)oLeft).intValue();
				int iRight = ((Integer)oRight).intValue();
				return new Integer((int)Math.pow(iLeft, iRight));
			}
		}
		// We shouldn't ever get here:
		throw new IntegrityException(operator.toString());
	}
}
