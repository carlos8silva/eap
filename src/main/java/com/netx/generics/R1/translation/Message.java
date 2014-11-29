package com.netx.generics.R1.translation;


public class Message {
	
	public static enum TYPE {
		ERROR, WARNING
	}

	private final TYPE _type;
	private final String _message;
	private final Position _position;
	private final int _stepNumber;
	
	// for TranslationStep:
	Message(TYPE type, Position p, String message, int stepNumber) {
		_type = type;
		_position = p;
		_message = message;
		_stepNumber = stepNumber;
	}

	public TYPE getType() {
		return _type;
	}

	public Position getPosition() {
		return _position;
	}

	public String getMessage() {
		return _message;
	}

	public int getStepNumber() {
		return _stepNumber;
	}

	// Format:
	// ERROR: line 1, index 10: step 0: message
	// ERROR: at 'elem1': step -1: message
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getType());
		sb.append(": ");
		if(_position != null) {
			sb.append(_position.toString());
			sb.append(": ");
		}
		sb.append("step "+_stepNumber);
		sb.append(": ");
		sb.append(_message);
		return sb.toString();
	}
}
