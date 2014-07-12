package com.netx.generics.R1.translation;
import java.util.List;
import java.util.ArrayList;

import com.netx.basic.R1.eh.Checker;


public abstract class MessageFormatter {

	public abstract String format(Message m);
	
	public List<String> format(List<Message> messages) {
		Checker.checkNull(messages, "messages");
		List<String> results = new ArrayList<String>();
		for(Message m : messages) {
			results.add(format(m));
		}
		return results;
	}
}
