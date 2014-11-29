package com.netx.generics.R1.translation;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.shared.Globals;


public class MessagePrinter {

	private final PrintWriter _out;
	
	public MessagePrinter(PrintWriter out) {
		Checker.checkNull(out, "out");
		_out = out;
	}

	public MessagePrinter(PrintStream out) {
		Checker.checkNull(out, "out");
		_out = new PrintWriter(out);
	}
	
	public MessagePrinter print(String message, int tabs) {
		Checker.checkEmpty(message, "message");
		Checker.checkMinValue(tabs, 0, "tabs");
		for(int i=0; i<tabs; i++) {
			_out.print(Strings.repeat(" ", Globals.getTabSize()));
		}
		_out.print(message);
		return this;
	}

	public MessagePrinter print(String message) {
		return print(message, 0);
	}

	public MessagePrinter println(String message, int tabs) {
		print(message, tabs);
		_out.println();
		_out.flush();
		return this;
	}
	
	public MessagePrinter println(String message) {
		return println(message, 0);
	}

	public MessagePrinter print(List<String> messages, int tabs) {
		Checker.checkEmpty(messages, "messages");
		for(String s : messages) {
			println(s, tabs);
		}
		return this;
	}
	
	public MessagePrinter print(List<String> messages) {
		return print(messages, 0);
	}

	public MessagePrinter printByStep(String[] errorHeaders, String[] warnHeaders,
		int headerTabs, ErrorList el, int messageTabs, MessageFormatter mf, boolean joinWarnings) {
		Checker.checkEmpty(errorHeaders, "errorHeaders");
		Checker.checkNullElements(errorHeaders, "errorHeaders");
		Checker.checkMinValue(headerTabs, 0, "headerTabs");
		Checker.checkEmpty(el, "el");
		Checker.checkMinValue(messageTabs, 0, "messageTabs");
		Checker.checkNull(mf, "mf");
		if(warnHeaders != null) {
			Checker.checkNullElements(warnHeaders, "warnHeaders");
			Checker.checkMatchingLength(errorHeaders, "errorHeaders", warnHeaders, "warnHeaders");
		}
		else {
			warnHeaders = errorHeaders;
		}
		// Print the error / warning messages:
		for(int i=0; i<errorHeaders.length; i++) {
			boolean hasErrors = false;
			List<String> msgs = el.getErrors(i, mf);
			if(!msgs.isEmpty()) {
				hasErrors = true;
				println(errorHeaders[i], headerTabs);
				print(msgs, messageTabs);
			}
			msgs = el.getWarnings(i, mf);
			if(!msgs.isEmpty()) {
				if(!hasErrors || (hasErrors && !joinWarnings)) {
					println(warnHeaders[i], headerTabs);
				}
				print(msgs, messageTabs);
			}
		}
		return this;
	}
	
	public MessagePrinter printByStep(String[] errorHeaders, String[] warnHeaders,
		ErrorList el, MessageFormatter mf, boolean joinWarnings) {
		// TODO
		return this;
	}

	public MessagePrinter printByStep(String[] errorHeaders, ErrorList el,
		MessageFormatter mf, boolean joinWarnings) {
		// TODO
		return this;
	}
}
