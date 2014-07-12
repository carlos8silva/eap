package com.netx.eap.R1.bl;
import java.util.List;
import com.netx.bl.R1.core.*;


public class Functions extends Entity<FunctionsMetaData,FunctionInstance> {

	// TYPE:
	public static Functions getInstance() {
		return EAP.getFunctions();
	}

	// INSTANCE:
	Functions() {
		super(new FunctionsMetaData());
	}
	
	public List<FunctionInstance> listAll(Connection c) throws BLException {
		return selectAll(c);
	}
}
