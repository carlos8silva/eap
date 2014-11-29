package com.netx.eap.R1.bl;
import java.util.List;
import com.netx.bl.R1.core.*;


public class SecurityQuestions extends Entity<SecurityQuestionsMetaData,SecurityQuestion> {

	// TYPE:
	public static SecurityQuestions getInstance() {
		return EAP.getSecurityQuestions();
	}

	// INSTANCE:
	SecurityQuestions() {
		super(new SecurityQuestionsMetaData());
	}
	
	public List<SecurityQuestion> listAll(Connection c) throws BLException {
		return selectAll(c);
	}
}
