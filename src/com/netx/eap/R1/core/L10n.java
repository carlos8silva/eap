package com.netx.eap.R1.core;
import com.netx.basic.R1.l10n.ContentID;


class L10n extends com.netx.basic.R1.l10n.L10n {

	// Internal constants:
	private final static String _M_EAP = "EAP";
	
	// Content ID's:
	public static final ContentID EAP_MSG_METHOD_NOT_ALLOWED = new ContentID(_M_EAP, TYPE_MSG, "method-not-allowed");
	public static final ContentID EAP_MSG_ILLEGAL_PARAMETER = new ContentID(_M_EAP, TYPE_MSG, "illegal-parameter");
	public static final ContentID EAP_MSG_ILLEGAL_REQUEST = new ContentID(_M_EAP, TYPE_MSG, "illegal-request");

}
