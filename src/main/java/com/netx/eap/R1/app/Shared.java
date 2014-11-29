package com.netx.eap.R1.app;
import com.netx.basic.R1.eh.Checker;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.IllegalParameterException;

public class Shared {

	public static String[] parseStringList(EapRequest request, String name, boolean mandatory) {
		Checker.checkNull(request, "request");
		Checker.checkEmpty(name, "name");
		String value = request.getParameter(name, mandatory);
		if(value == null) {
			return new String[0];
		}
		else {
			return value.split("[,]");
		}
	}

	public static Long[] parseLongList(EapRequest request, String name, boolean mandatory) {
		String[] parts = parseStringList(request, name, mandatory);
		Long[] ids = new Long[parts.length];
		try {
			for(int i=0; i<parts.length; i++) {
				ids[i] = new Long(parts[i]);
			}
			return ids;
		}
		catch(NumberFormatException nfe) {
			throw new IllegalParameterException(name, request.getParameter(name));
		}
	}
}
