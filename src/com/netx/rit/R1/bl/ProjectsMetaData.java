package com.netx.rit.R1.bl;
import com.netx.generics.R1.util.ByteValue;
import com.netx.generics.R1.util.ByteValue.MEASURE;
import com.netx.bl.R1.core.*;
import com.netx.eap.R1.bl.*;


public class ProjectsMetaData extends TimedMetaData {

	// Fields:
	public final Field projectId = new FieldLong(this, "projectId", "project_id", null, true, true, true, null, null);
	public final Field name = new FieldText(this, "name", "name", null, true, false, 0, 50, false, null, new Validators.ReadableText());
	public final Field description = new FieldText(this, "description", "description", null, true, false, 0, (long)ByteValue.convert(16, MEASURE.MEGABYTES, MEASURE.BYTES), false, null, new Validators.ReadableText());
	public final Field owner = new FieldForeignKey(this, "owner", "owner", null, true, false, RIT.getUsers().getMetaData().userId, FieldForeignKey.ON_DELETE_CONSTRAINT.RESTRICT);
	public final Field timeArchived = new FieldDateTime(this, "timeArchived", "time_archived", null, false, true);
	
	public ProjectsMetaData() {
		super("Projects", "rit_projects");
		addPrimaryKeyField(projectId);
		addField(name);
		addField(description);
		addField(owner);
		addField(timeArchived);
		addDefaultFields();
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.TRANSACTIONAL;
	}

	public Class<Project> getInstanceClass() {
		return Project.class;
	}
	
	public Field getAutonumberKeyField() {
		return projectId;
	}
}
