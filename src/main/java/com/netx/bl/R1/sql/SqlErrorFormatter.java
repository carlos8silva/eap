package com.netx.bl.R1.sql;
import com.netx.generics.R1.translation.Message;
import com.netx.generics.R1.translation.MessageFormatter;


class SqlErrorFormatter extends MessageFormatter {
	
	public SqlErrorFormatter() {
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
