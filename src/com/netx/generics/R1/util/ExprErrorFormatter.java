package com.netx.generics.R1.util;
import com.netx.generics.R1.translation.Message;
import com.netx.generics.R1.translation.MessageFormatter;


class ExprErrorFormatter extends MessageFormatter {
	
	public ExprErrorFormatter() {
	}

	public String format(Message m) {
		if(m.getPosition() == null) {
			return m.getMessage();
		}
		else {
			return m.getMessage()+" (char "+m.getPosition().getIndex()+")";
		}
	}
}
