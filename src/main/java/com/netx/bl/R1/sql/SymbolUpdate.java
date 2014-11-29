package com.netx.bl.R1.sql;
import java.util.List;
import java.util.ArrayList;
import com.netx.bl.R1.core.Field;


public class SymbolUpdate extends SymbolQuery {

	public SymbolTable table = null;
	public final List<SymbolSet> set = new ArrayList<SymbolSet>();
	public final List<Field> updateParams = new ArrayList<Field>();
}
