package com.netx.bl.R1.spi;
import java.sql.SQLException;
import com.netx.bl.R1.core.Argument;


public final class UniqueKeyConstraintException extends ConstraintException {

	// TYPE:
	private static String _getFieldNames(Argument[] args) {
		if(args.length == 1) {
			return args[0].getKey().getName();
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<args.length; i++) {
			sb.append(args[i].getKey().getName());
			if(i < args.length - 1) {
				sb.append("/");
			}
		}
		return sb.toString();
	}

	private static String _getFieldValues(Argument[] args) {
		if(args.length == 1) {
			return args[0].getValue().toString();
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<args.length; i++) {
			sb.append(args[i].getValue());
			if(i < args.length - 1) {
				sb.append("/");
			}
		}
		return sb.toString();
	}

	// INSTANCE:
	private final Argument[] _violatingArgs;

	// Used when there is argument information:
	UniqueKeyConstraintException(SQLException cause, String query, Argument[] violatingArgs) {
		super(cause, query, violatingArgs.length == 1 ? L10n.BL_MSG_CONSTRAINT_UNIQUE_1 : L10n.BL_MSG_CONSTRAINT_UNIQUE_2, _getFieldNames(violatingArgs), _getFieldValues(violatingArgs));
		_violatingArgs = violatingArgs;
	}

	// Used when there is NO argument information:
	UniqueKeyConstraintException(SQLException cause, String query) {
		super(cause, query, L10n.BL_MSG_CONSTRAINT_UNIQUE_3);
		_violatingArgs = null;
	}

	public Argument[] getViolatingArguments() {
		return _violatingArgs;
	}
}
