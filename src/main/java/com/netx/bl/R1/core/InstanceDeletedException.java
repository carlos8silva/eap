package com.netx.bl.R1.core;


public final class InstanceDeletedException extends BLException {

	private final EntityInstance<?,?> _ei;

	InstanceDeletedException(EntityInstance<?,?> ei) {
		super(L10n.BL_MSG_INSTANCE_DELETED);
		_ei = ei;
	}

	public EntityInstance<?,?> getEntityInstance() {
		return _ei;
	}
}
