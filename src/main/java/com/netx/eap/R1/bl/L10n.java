package com.netx.eap.R1.bl;
import com.netx.basic.R1.l10n.ContentID;


public class L10n extends com.netx.bl.R1.core.L10n {

	// Internal constants:
	protected final static String MODULE_EAP = "EAP";
	protected final static String TYPE_VAL = "Val";
	// Content ID's:
	public final static ContentID EAP_VAL_FIRST_CHAR_MUST_BE_LETTER = new ContentID(MODULE_EAP, TYPE_VAL, "first-char-must-be-letter");
	public final static ContentID EAP_VAL_ILLEGAL_CHARS = new ContentID(MODULE_EAP, TYPE_VAL, "illegal-chars");
	public final static ContentID EAP_VAL_NO_DIGITS_ALLOWED = new ContentID(MODULE_EAP, TYPE_VAL, "no-digits-allowed");
	public final static ContentID EAP_VAL_PASSWORD_NO_SPACES = new ContentID(MODULE_EAP, TYPE_VAL, "password-no-spaces");
	public final static ContentID EAP_VAL_ONE_ROLE_REQUIRED = new ContentID(MODULE_EAP, TYPE_VAL, "one-role-required");
	public final static ContentID EAP_VAL_ONE_PERMISSION_REQUIRED = new ContentID(MODULE_EAP, TYPE_VAL, "one-permission-required");
	public final static ContentID EAP_VAL_ONE_PRIMARY_ROLE = new ContentID(MODULE_EAP, TYPE_VAL, "one-primary-role");
	public final static ContentID EAP_VAL_DUPLICATE_USER_PERMISSION = new ContentID(MODULE_EAP, TYPE_VAL, "duplicate-user-permission");
	// TODO check if still used
	public final static ContentID EAP_VAL_ILLEGAL_USER_PERMISSION = new ContentID(MODULE_EAP, TYPE_VAL, "illegal-user-permission");
}
