package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;


public class Item extends TimedInstance<ItemsMetaData,Items> {
	
	public Item(Long itemId) throws ValidationException {
		setPrimaryKey(getMetaData().itemId, itemId);
	}
	
	public Items getEntity() {
		return Items.getInstance();
	}

	public Long getItemId() {
		return (Long)getValue(getMetaData().itemId);
	}

	public String getTitle() {
		return (String)getValue(getMetaData().title);
	}

	public Item setTitle(String value) {
		this.safelySetValue(getMetaData().title, value);
		return this;
	}

	public Long getProjectId() {
		return (Long)getValue(getMetaData().projectId);
	}

	public Project getProject(Connection c) throws BLException {
		Long projectId = getProjectId();
		return projectId == null ? null : RIT.getProjects().get(c, projectId);
	}

	public Item setProjectId(Long value) {
		safelySetValue(getMetaData().projectId, value);
		return this;
	}

	public Item setProject(Project value) {
		return setProjectId(value == null ? null : value.getProjectId());
	}
}
