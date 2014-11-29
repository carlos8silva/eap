package com.netx.generics.R1.translation;
import java.util.Iterator;


public abstract class FirstStep extends TranslationStep {

	private int _chainLength;
	
	protected FirstStep() {
		super();
		_chainLength = 1;
	}
	
	public int getChainLength() {
		return _chainLength;
	}

	// for TranslationStep:
	void incrementChainLength() {
		_chainLength++;
	}

	// for Translator:
	Iterator<TranslationStep> iterator() {
		return new StepIterator(this);
	}

	private class StepIterator implements Iterator<TranslationStep> {
		
		private TranslationStep _current;
		
		public StepIterator(FirstStep firstStep) {
			_current = firstStep;
		}
		
		public boolean hasNext() {
			return _current != null;
		}
		
		public TranslationStep next() {
			TranslationStep tmp = _current;
			_current = _current.getNextStep();
			return tmp;
		}
		
		public void remove() {
			throw new UnsupportedOperationException("remove");
		}
	}
}
