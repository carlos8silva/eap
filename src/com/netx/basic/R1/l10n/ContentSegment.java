package com.netx.basic.R1.l10n;
import com.netx.generics.R1.util.Strings;


class ContentSegment {

	public int parameterCount;
	public String content;
	
	// TODO make parameters String?
	public String getContent(String id, Object ... parameters) {
		if(parameters.length != parameterCount) {
			throw new IllegalArgumentException("error getting i18n content segment '"+id+"': expected "+parameterCount+" parameters but found "+parameters.length);
		}
		if(parameters.length == 0) {
			return content;
		}
		String s = content;
		for(int i=0; i<parameters.length; i++) {
			s = Strings.replaceAll(s, "{"+(i+1)+"}", parameters[i] == null ? null : parameters[i].toString());
		}
		return s;
	}
}
