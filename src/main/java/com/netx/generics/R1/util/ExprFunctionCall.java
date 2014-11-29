package com.netx.generics.R1.util;
import java.util.List;
import java.util.ArrayList;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.Token;


// TODO i18n
class ExprFunctionCall extends Expression {

	public final Token name;
	public final List<Expression> args;

	public ExprFunctionCall(Token op) {
		name = op;
		args = new ArrayList<Expression>();
	}

	public Object evaluate(ErrorList el) {
		// Get the function call:
		FunctionCall call = Expr.getCall(name.toString());
		if(call == null) {
			el.addError(name, "unknown function call '"+name.toString()+"'");
			return null;
		}
		Class<?>[] parameters = null;
		for(Class<?>[] tmp : call.getParameters()) {
			if(tmp.length == args.size()) {
				parameters = tmp;
				break;
			}
		}
		if(parameters == null) {
			el.addError(name, "illegal number of arguments for function '"+name.toString()+"'");
			return null;
		}
		Object[] results = new Object[args.size()];
		int errorsBeforeEval = el.getErrors().size();
		for(int i=0; i<args.size(); i++) {
			results[i]= args.get(i).evaluate(el);
			if(results[i] != null) {
				if(!parameters[i].isAssignableFrom(results[i].getClass())) {
					Type expected = new Type(parameters[i]);
					Type found = new Type(results[i].getClass());
					el.addError(name, "illegal type for argument["+i+"] (expected "+expected.getClassName()+" but found "+found.getClassName()+")");
					return null;
				}
			}
		}
		// No point in calling the function if the args
		// haven't been successfully executed:
		if(errorsBeforeEval < el.getErrors().size()) {
			return null;
		}
		// Call the function:
		try {
			return call.call(results);
		}
		catch(Exception e) {
			el.addError(name, "error calling function '"+name.toString()+"': "+e.getMessage());
		}
		return null;
	}
}
