package com.netx.bl.R1.core;


// This class is used to tag holder entities so that correct parameters are enforced in the Association constructor.
public abstract class HolderEntity<M extends TimedMetaData,HI extends HolderInstance<M,?>> extends Entity<M,HI> {

	protected HolderEntity(M metaData) {
		super(metaData);
	}
}
