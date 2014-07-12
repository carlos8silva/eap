package com.netx.bl.R1.sql;
import com.netx.generics.R1.translation.ParseException;
import com.netx.generics.R1.translation.Results;
import com.netx.generics.R1.translation.Translator;
import com.netx.generics.R1.translation.MessageFormatter;
import com.netx.bl.R1.core.MetaData;
import com.netx.bl.R1.core.Query;
import com.netx.bl.R1.core.Select;
import com.netx.bl.R1.core.Update;
import com.netx.bl.R1.core.Repository;


// For Entity and Repository:
public class Parser {

	// TYPE;
	private final static MessageFormatter _mf = new SqlErrorFormatter();
	
	public static Select parseSelect(String queryName, String sql, MetaData metaData, Repository rep) {
		return (Select)_parse(Query.TYPE.SELECT, queryName, sql, metaData, rep);
	}

	public static Update parseUpdate(String queryName, String sql, MetaData metaData, Repository rep) {
		return (Update)_parse(Query.TYPE.UPDATE, queryName, sql, metaData, rep);
	}
	
	public static Select parseGlobal(String queryName, String sql, Repository rep) {
		return (Select)_parse(null, queryName, sql, null, rep);
	}

	private static Query _parse(Query.TYPE type, String queryName, String sql, MetaData metaData, Repository r) {
		SqlScanner scanner = new SqlScanner();
		new SqlAnalyzer(new SqlParser(scanner, type, queryName), metaData, r);
		Translator translator = new Translator(scanner);
		translator.setStopOnErrors(true);
		Results results = translator.performWork(sql);
		if(results.getErrorList().hasErrors()) {
			// Throw an exception with the first error message properly formatted:
			throw new ParseException(Constants.ERROR_START, results.getErrorList(), _mf);
		}
		else {
			return (Query)results.getResult();
		}
	}

	// INSTANCE:
	private Parser() {
	}
}
