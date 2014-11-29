package com.netx.eap.R1.bl;
import com.netx.bl.R1.core.*;


public class SecurityQuestionsMetaData extends MetaData {

	// Fields:
	public final Field seqQuestionId = new FieldInt(this, "seqQuestionId", "seq_question_id", null, true, true, null, null);
	public final Field question = new FieldText(this, "question", "question", null, true, true, 0, 100, true, null, new Validators.ReadableText());

	public SecurityQuestionsMetaData() {
		super("SecurityQuestions", "eap_seq_questions");
		addPrimaryKeyField(seqQuestionId);
		addField(question);
	}
	
	public final DATA_TYPE getDataType() {
		return DATA_TYPE.REFERENCE;
	}

	public Class<SecurityQuestion> getInstanceClass() {
		return SecurityQuestion.class;
	}

	public Field getAutonumberKeyField() {
		return null;
	}
}
