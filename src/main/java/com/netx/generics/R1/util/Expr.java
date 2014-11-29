package com.netx.generics.R1.util;
import java.util.Map;
import java.util.HashMap;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.ObjectAlreadyExistsException;
import com.netx.basic.R1.l10n.L10n;
import com.netx.generics.R1.translation.MessageFormatter;
import com.netx.generics.R1.translation.Results;
import com.netx.generics.R1.translation.Translator;
import com.netx.generics.R1.translation.ParseException;


public class Expr {

	private static final ExprScanner _scanner = new ExprScanner();
	private static final ExprParser _parser = new ExprParser(_scanner);
	private static final ExprAnalyzer _analyzer = new ExprAnalyzer(_parser);
	private static final MessageFormatter _mf = new ExprErrorFormatter();
	private static final Map<String,FunctionCall> _calls;
	
	static {
		_calls = new HashMap<String,FunctionCall>();
		registerCall(new Builtins.NowCall());
		registerCall(new Builtins.DateCall());
		registerCall(new Builtins.TimeCall());
		registerCall(new Builtins.EnvCall());
	}

	public static void registerCall(FunctionCall call) throws ObjectAlreadyExistsException {
		Checker.checkNull(call, "call");
		if(_calls.get(call.getFunctionName()) != null) {
			throw new ObjectAlreadyExistsException(call.getFunctionName(), L10n.GLOBAL_WORD_FUNCTION);
		}
		// Check whether any of the signatures has wrong classes:
		Class<?>[][] parameters = call.getParameters();
		for(Class<?>[] classes : parameters) {
			for(Class<?> c : classes) {
				if(c != Object.class && c != Integer.class && c != Double.class && c != String.class && c != Boolean.class) {
					throw new IllegalArgumentException("illegal call parameter type: "+c.getName());
				}
			}
		}
		// Check whether any of the signatures has the same number of paramaters:
		for(int i=0; i<parameters.length; i++) {
			for(int j=0; j<parameters.length; j++) {
				if(i != j) {
					if(parameters[i].length == parameters[j].length) {
						throw new IllegalArgumentException("cannot register different call parameters with the same length");
					}
				}
			}
		}
		// All fine:
		_calls.put(call.getFunctionName(), call);
	}

	private Expr() {
		// Fool compiler:
		_analyzer.toString();
	}

	public static Object evaluate(String expression) {
		Checker.checkEmpty(expression, "expression");
		Translator translator = new Translator(_scanner);
		Results results = translator.performWork(expression);
		if(results.getErrorList().hasErrors()) {
			// Throw an exception with the first error message properly formatted:
			throw new ParseException(ExprConstants.ERROR_START, results.getErrorList(), _mf);
		}
		else {
			return results.getResult();
		}
	}

	public static boolean hasExpression(String value) {
		value = Strings.replaceAll(value, "\\[", "");
		return value.contains("[");
	}

	// For ExprFunctionCall:
	static FunctionCall getCall(String name) {
		return _calls.get(name);
	}
}
