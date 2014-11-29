package com.netx.basic.R1.l10n;

import com.netx.basic.R1.eh.Checker;


public class ContentID {

	private final ContentKey _key;
	private ContentSegment _segment;
	
	public ContentID(String module, String type, String id) {
		Checker.checkEmpty(module, "module");
		Checker.checkEmpty(type, "type");
		Checker.checkEmpty(id, "id");
		_key = new ContentKey(module.toLowerCase(), type.toLowerCase(), id.toLowerCase());
		_segment = null;
	}
	
	public String getModule() {
		return _key.module;
	}

	public String getType() {
		return _key.type;
	}

	public String getID() {
		return _key.ID;
	}
	
	public String toString() {
		return getModule()+'.'+getType()+'.'+getID();
	}
	
	// getSegment and setSegment are used by L10n to cache the segment
	// that is related to this ContentID
	ContentSegment getSegment() {
		return _segment;
	}
	
	void setSegment(ContentSegment segment) {
		_segment = segment;
	}
	
	// For L10n.getContent:
	ContentKey getKey() {
		return _key;
	}
}