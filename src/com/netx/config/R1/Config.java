package com.netx.config.R1;
import com.netx.basic.R1.io.ExtendedInputStream;
import com.netx.basic.R1.io.ExtendedReader;
import com.netx.generics.R1.translation.Translator;
import com.netx.generics.R1.translation.Results;


public class Config {

	// TYPE:
	public static Results loadConfig(ExtendedInputStream in) {
		final ConfigScanner scanner = new ConfigScanner();
		final ConfigParser parser = new ConfigParser(scanner);
		final ConfigAnalyzer analyzer = new ConfigAnalyzer(parser);
		// fool the compiler:
		analyzer.hashCode();
		return new Translator(scanner).performWork(new ExtendedReader(in));
	}
	
	public static ContextDef getRootDef() {
		return Dictionary.getRootDef();
	}
}
