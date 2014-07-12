package com.netx.generics.R1.util;
import java.util.List;

import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.Token;
import com.netx.generics.R1.translation.TranslationStep;


class ExprAnalyzer extends TranslationStep {

	public ExprAnalyzer(ExprParser parser) {
		super(parser);
	}

	@SuppressWarnings("unchecked")
	public Object performWork(Object o, ErrorList el) {
		List<? extends Object> segments = (List<? extends Object>)o;
		// If it is a single function call, we want to return the
		// results directly instead of translating to String:
		if(segments.size() == 1) {
			Object tmp = segments.get(0);
			if(tmp instanceof Expression) {
				return ((Expression)tmp).evaluate(el);
			}
		}
		// Not single function call, we need to translate to String:
		StringBuilder sb = new StringBuilder();
		for(Object tmp : segments) {
			if(tmp.getClass() == Token.class) {
				sb.append(Strings.replaceAll(tmp.toString(), "\\[", "["));
			}
			else {
				sb.append(((Expression)tmp).evaluate(el));
			}
		}
		return sb.toString();
	}
}
