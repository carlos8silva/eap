package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class SecurityQuestion extends EntityInstance<SecurityQuestionsMetaData,SecurityQuestions> {

	public SecurityQuestion(Integer seqQuestionId) throws ValidationException {
		setPrimaryKey(getMetaData().seqQuestionId, seqQuestionId);
	}

	public SecurityQuestions getEntity() {
		return SecurityQuestions.getInstance();
	}

	public Integer getSeqQuestionId() {
		return (Integer)getValue(getMetaData().seqQuestionId);
	}

	public String getQuestion() {
		return (String)getValue(getMetaData().question);
	}
}
