package com.netx.generics.R1.translation;
import com.netx.basic.R1.eh.Checker;


public abstract class TranslationStep {

	private TranslationStep _nextStep;
	private final TranslationStep _previousStep;
	
	protected TranslationStep(TranslationStep previousStep) {
		Checker.checkNull(previousStep, "previousStep");
		_nextStep = null;
		_previousStep = previousStep;
		_previousStep.setNextStep(this);
		incrementChainLength();
	}
	
	// for FirstStep:
	TranslationStep() {
		_nextStep = null;
		_previousStep = null;
	}

	public abstract Object performWork(Object o, ErrorList el);
	
	// for FirstStep.StepIterator:
	TranslationStep getNextStep() {
		return _nextStep;
	}

	// for FirstStep.StepIterator:
	void setNextStep(TranslationStep step) {
		_nextStep = step;
	}
	
	// for FirstStep.getChainLength:
	void incrementChainLength() {
		_previousStep.incrementChainLength();
	}
}
