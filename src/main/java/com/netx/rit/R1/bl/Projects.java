package com.netx.rit.R1.bl;
import java.util.List;
import com.netx.bl.R1.core.*;


public class Projects extends Entity<ProjectsMetaData,Project> {

	// TYPE:
	public static Projects getInstance() {
		return RIT.getProjects();
	}

	// INSTANCE:
	Projects() {
		super(new ProjectsMetaData());
	}
	
	public List<Project> listAll(Connection c) throws BLException {
		return selectAll(c);
	}
}
