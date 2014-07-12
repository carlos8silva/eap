package com.netx.generics.R1.translation;
import java.util.Iterator;

import com.netx.basic.R1.eh.Checker;


public class Translator {

	private final FirstStep _firstStep;
	private boolean _stopOnErrors;
	private int _stepsPerformed;
	
	public Translator(FirstStep firstStep) {
		Checker.checkNull(firstStep, "firstStep");
		_firstStep = firstStep;
		_stopOnErrors = true;
		_stepsPerformed = 0;
	}

	public boolean getStopOnErrors() {
		return _stopOnErrors;
	}

	public Translator setStopOnErrors(boolean stopOnErrors) {
		_stopOnErrors = stopOnErrors;
		return this;
	}
	
	public int getTotalStepCount() {
		Iterator<?> it = _firstStep.iterator();
		int i = 0;
		while(it.hasNext()) {
			it.next();
			i++;
		}
		return i;
	}
	
	public Results performWork(Object initialObject) {
		ErrorList el = new ErrorList();
		el.incStepNumber();
		Iterator<TranslationStep> it = _firstStep.iterator();
		Object result = initialObject;
		for(_stepsPerformed=0; it.hasNext(); _stepsPerformed++) {
			TranslationStep step = it.next();
			result = step.performWork(result, el);
			el.incStepNumber();
			if(result == null || (getStopOnErrors()==true && el.hasErrors())) {
				break;
			}
		}
		return new Results(result, el);
	}

	public FirstStep getFirstStep() {
		return _firstStep;
	}

	public int getPerformedStepCount() {
		return _stepsPerformed;
	}
}
