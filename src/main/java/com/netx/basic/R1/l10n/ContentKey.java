package com.netx.basic.R1.l10n;

class ContentKey {

	public final String module;
	public final String type;
	public final String ID;
	private Integer _hashCode;
	
	public ContentKey(String module, String type, String ID) {
		this.module = module;
		this.type = type;
		this.ID = ID;
		_hashCode = null;
	}
	
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null) {
			return false;
		}
		if(o instanceof ContentKey) {
			ContentKey key = (ContentKey)o;
			return module.equals(key.module) && type.equals(key.type) && ID.equals(key.ID);
		}
		return false;
	}
	
	public int hashCode() {
		if(_hashCode != null) {
			return _hashCode;
		}
		int hash = 7;
		hash = 31 * hash + module.hashCode();
		hash = 31 * hash + type.hashCode();
		hash = 31 * hash + ID.hashCode();
		_hashCode = hash;
		return hash;
	}
	
	public String toString() {
		return module + '/' + type + '/' + ID;
	}
}
