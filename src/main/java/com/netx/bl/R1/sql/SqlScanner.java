package com.netx.bl.R1.sql;
import java.io.StringReader;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.FirstStep;
import com.netx.generics.R1.translation.GenericScanner;
import com.netx.generics.R1.translation.Recognizer;
import com.netx.generics.R1.translation.Recognizers;
import com.netx.basic.R1.io.ExtendedReader;


class SqlScanner extends FirstStep {

	public Object performWork(Object o, ErrorList el) {
		return new InternalScanner().performWork(new ExtendedReader(new StringReader((String)o), "<string>"), el);
	}

	private class InternalScanner extends GenericScanner {
		public InternalScanner() {
			super(new Recognizer[] {
				new Recognizers.StringRecognizer("'\""),
				new Recognizers.NumberRecognizer(true),
				new Recognizers.SeparatorRecognizer(".,()[]"),
				new Recognizers.OperatorRecognizer(Constants.OPERATORS),
				new Recognizers.IdentifierRecognizer(true, "_", Constants.KEYWORDS, true)
			}, true, null);
		}
	}
}