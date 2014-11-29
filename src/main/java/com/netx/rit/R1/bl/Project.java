package com.netx.rit.R1.bl;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.bl.*;


public class Project extends TimedInstance<ProjectsMetaData,Projects> {
	
	public Project(Long projectId) throws ValidationException {
		setPrimaryKey(getMetaData().projectId, projectId);
	}
	
	public Projects getEntity() {
		return Projects.getInstance();
	}

	public Long getProjectId() {
		return (Long)getValue(getMetaData().projectId);
	}

	public String getName() {
		return (String)getValue(getMetaData().name);
	}

	public Project setName(String value) {
		this.safelySetValue(getMetaData().name, value);
		return this;
	}

	public String getDescription() {
		return (String)getValue(getMetaData().description);
	}

	public Project setDescription(String value) {
		this.safelySetValue(getMetaData().description, value);
		return this;
	}

	public Long getOwnerId() {
		return (Long)getValue(getMetaData().owner);
	}

	public User getOwner(Connection c) throws BLException {
		Long ownerId = getOwnerId();
		if(ownerId == null) {
			return null;
		}
		return Users.getInstance().get(c, ownerId);
	}

	public Project setOwner(Long value) {
		safelySetValue(getMetaData().owner, value);
		return this;
	}
}
