package com.netx.generics.R1.translation;
import java.util.List;
import java.util.ArrayList;

import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IllegalUsageException;
import com.netx.generics.R1.collections.IList;


public class ErrorList {

	private final List<Message> _errors;
	private final List<Message> _warns;
	private final List<Message> _all;
	private IList<Message> _ie;
	private IList<Message> _iw;
	private IList<Message> _ia;
	private int _stepNumber;
	
	public ErrorList() {
		_errors = new ArrayList<Message>();
		_warns = new ArrayList<Message>();
		_all = new ArrayList<Message>();
		_ie = _iw = _ia = null;
		_stepNumber = -1;
	}

	public boolean hasErrors() {
		return !_errors.isEmpty();
	}

	public boolean hasWarnings() {
		return !_warns.isEmpty();
	}

	public boolean isEmpty() {
		return !hasErrors() && !hasWarnings();
	}

	public ErrorList addError(Position p, String message) {
		Checker.checkNull(p, "p");
		Checker.checkEmpty(message, "message");
		return _add(_errors, new Message(Message.TYPE.ERROR, p, message, _stepNumber));
	}

	public ErrorList addWarning(Position p, String message) {
		Checker.checkNull(p, "p");
		Checker.checkEmpty(message, "message");
		return _add(_warns, new Message(Message.TYPE.WARNING, p, message, _stepNumber));
	}

	public ErrorList addError(String location, String message) {
		Checker.checkEmpty(location, "location");
		Checker.checkEmpty(message, "message");
		return _add(_errors, new Message(Message.TYPE.ERROR, new Position(location), message, _stepNumber));
	}

	public ErrorList addWarning(String location, String message) {
		Checker.checkEmpty(location, "location");
		Checker.checkEmpty(message, "message");
		return _add(_warns, new Message(Message.TYPE.WARNING, new Position(location), message, _stepNumber));
	}

	public ErrorList addError(int line, int index, String message) {
		Checker.checkEmpty(message, "message");
		return _add(_errors, new Message(Message.TYPE.ERROR, new Position(line, index), message, _stepNumber));
	}

	public ErrorList addWarning(int line, int index, String message) {
		Checker.checkEmpty(message, "message");
		return _add(_warns, new Message(Message.TYPE.WARNING, new Position(line, index), message, _stepNumber));
	}
	
	public ErrorList addError(String message) {
		Checker.checkEmpty(message, "message");
		return _add(_errors, new Message(Message.TYPE.ERROR, null, message, _stepNumber));
	}

	public ErrorList addWarning(String message) {
		Checker.checkEmpty(message, "message");
		return _add(_warns, new Message(Message.TYPE.WARNING, null, message, _stepNumber));
	}
	
	public IList<Message> getErrors() {
		if(_ie == null) {
			_ie = new IList<Message>(_errors);
		}
		return _ie;
	}

	public IList<Message> getWarnings() {
		if(_iw == null) {
			_iw = new IList<Message>(_warns);
		}
		return _iw;
	}

	public IList<Message> getAll() {
		if(_ia == null) {
			_ia = new IList<Message>(_all);
		}
		return _ia;
	}

	public IList<Message> getErrors(int stepNumber) {
		Checker.checkIndex(stepNumber, "stepNumber");
		_checkTranslatorUse();
		return _getMessages(_errors, stepNumber);
	}

	public IList<Message> getWarnings(int stepNumber) {
		Checker.checkIndex(stepNumber, "stepNumber");
		_checkTranslatorUse();
		return _getMessages(_warns, stepNumber);
	}

	public IList<Message> getAll(int stepNumber) {
		Checker.checkIndex(stepNumber, "stepNumber");
		_checkTranslatorUse();
		return _getMessages(_all, stepNumber);
	}

	public List<String> getErrors(MessageFormatter mf) {
		Checker.checkNull(mf, "mf");
		return mf.format(getErrors());
	}

	public List<String> getWarnings(MessageFormatter mf) {
		Checker.checkNull(mf, "mf");
		return mf.format(getWarnings());
	}

	public List<String> getAll(MessageFormatter mf) {
		Checker.checkNull(mf, "mf");
		return mf.format(getAll());
	}

	public List<String> getErrors(int stepNumber, MessageFormatter mf) {
		Checker.checkNull(mf, "mf");
		return mf.format(getErrors(stepNumber));
	}

	public List<String> getWarnings(int stepNumber, MessageFormatter mf) {
		Checker.checkNull(mf, "mf");
		return mf.format(getWarnings(stepNumber));
	}

	public List<String> getAll(int stepNumber, MessageFormatter mf) {
		Checker.checkNull(mf, "mf");
		return mf.format(getAll(stepNumber));
	}

	// for Translator:
	void incStepNumber() {
		_stepNumber++;
	}

	private ErrorList _add(List<Message> to, Message m) {
		// reset the immutable lists:
		_ie = _iw = _ia = null;
		to.add(m);
		_all.add(m);
		return this;
	}

	private void _checkTranslatorUse() {
		if(_stepNumber == -1) {
			throw new IllegalUsageException("error list was not used in a translation process");
		}
	}

	private IList<Message> _getMessages(List<Message> from, int stepIndex) {
		List<Message> results = new ArrayList<Message>();
		for(Message m : from) {
			if(m.getStepNumber() == stepIndex) {
				results.add(m);
			}
		}
		return new IList<Message>(results);
	}
}
