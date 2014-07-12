package com.netx.bl.R1.core;
import com.netx.basic.R1.l10n.ContentID;


// TODO remove L10n.BL_MSG_DB_ALREADY_LOADED
public class L10n extends com.netx.basic.R1.l10n.L10n {

	// Internal constants:
	protected final static String MODULE_BL = "bl";
	// Content ID's:
	public final static ContentID BL_MSG_DB_DRIVER_NOT_FOUND_01 = new ContentID(MODULE_BL, TYPE_MSG, "db-driver-not-found-01");
	public final static ContentID BL_MSG_DB_DRIVER_NOT_FOUND_02 = new ContentID(MODULE_BL, TYPE_MSG, "db-driver-not-found-02");
	public final static ContentID BL_MSG_DB_ALREADY_LOADED = new ContentID(MODULE_BL, TYPE_MSG, "db-already-loaded");
	public final static ContentID BL_MSG_INSTANCE_DELETED = new ContentID(MODULE_BL, TYPE_MSG, "instance-deleted");
	public final static ContentID BL_MSG_LATE_UPDATE = new ContentID(MODULE_BL, TYPE_MSG, "late-update");
	public final static ContentID BL_MSG_CACHE_LOCK_TIMEOUT = new ContentID(MODULE_BL, TYPE_MSG, "cache-lock-timeout");
	public final static ContentID BL_MSG_VAL_NULL_MANDATORY_VALUE = new ContentID(MODULE_BL, TYPE_MSG, "val-null-mandatory-value");
	public final static ContentID BL_MSG_VAL_READ_ONLY_FIELD = new ContentID(MODULE_BL, TYPE_MSG, "val-read-only-field");
	public final static ContentID BL_MSG_VAL_SIZE_EXCEEDED = new ContentID(MODULE_BL, TYPE_MSG, "val-size-exceeded");
	public final static ContentID BL_MSG_VAL_SIZE_NOT_ENOUGH = new ContentID(MODULE_BL, TYPE_MSG, "val-size-not-enough");
	public final static ContentID BL_MSG_VAL_WRONG_FORMAT = new ContentID(MODULE_BL, TYPE_MSG, "val-wrong-format");
	public final static ContentID BL_MSG_VAL_VALUE_TRUNCATED = new ContentID(MODULE_BL, TYPE_MSG, "val-value-truncated");
	public final static ContentID BL_MSG_CFG_WRONG_CACHE_CONFIG_FORMAT = new ContentID(MODULE_BL, TYPE_MSG, "cfg-wrong-cache-config-format");
	public final static ContentID BL_MSG_CFG_CACHE_CONFIG_MISSING_ARG = new ContentID(MODULE_BL, TYPE_MSG, "cfg-cache-config-missing-arg");
	public final static ContentID BL_MSG_CFG_CACHE_CONFIG_WRONG_ARG = new ContentID(MODULE_BL, TYPE_MSG, "cfg-cache-config-wrong-arg");
}
