package com.netx.generics.R1.translation;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.l10n.L10n;


// TODO needs to cope with messages not having a position 
// (e.g. fatal errors while reading the file)
// TODO step names must come from L10n
public class BasicMessageFormatter extends MessageFormatter {

	private final String[] _stepNames;
	private final boolean _idErrors;
	private final boolean _idWarnings;
	private String _tab = "";
	
	public BasicMessageFormatter() {
		this(null);
	}

	public BasicMessageFormatter(String[] stepNames) {
		if(stepNames != null) {
			Checker.checkEmptyElements(stepNames, "stepNames");
		}
		_stepNames = stepNames;
		_idErrors = _idWarnings = true;
	}

	public BasicMessageFormatter(boolean identifyErrors, boolean identifyWarnings) {
		_stepNames = null;
		_idErrors = identifyErrors;
		_idWarnings = identifyWarnings;
	}

	public String format(Message m) {
		StringBuilder result = new StringBuilder(_tab);
		// Message:
		Position p = m.getPosition(); 
		if(p != null) {
			if(p.getLocation() != null) {
				result.append(L10n.getContent(L10n.GENERICS_MSG_ERROR_AT_LOCATION, p.getLocation()));
				result.append(' ');
			}
			else {
				result.append(L10n.getContent(L10n.GENERICS_MSG_ERROR_AT_LINE, p.getLine()+1));
				result.append(' ');
			}
		}
		result.append(m.getMessage());
		// Step name:
		if(_stepNames != null) {
			String stepName = _stepNames[m.getStepNumber()].trim();
			result.insert(0, ": ");
			result.insert(0, " "+m.getType().toString().toLowerCase());
			result.insert(0, stepName);
		}
		else {
			if(m.getType() == Message.TYPE.ERROR && _idErrors) {
				result.insert(0, ": ");
				result.insert(0, L10n.getContent(L10n.GLOBAL_WORD_ERROR));
			}
			if(m.getType() == Message.TYPE.WARNING && _idWarnings) {
				result.insert(0, ": ");
				result.insert(0, L10n.getContent(L10n.GLOBAL_WORD_WARNING));
			}
		}
		return result.toString();
	}

	public void setTab(String tab) {
		_tab = tab;
	}
}
